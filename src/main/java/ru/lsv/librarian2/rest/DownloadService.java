package ru.lsv.librarian2.rest;

import io.vertx.core.Vertx;
import io.vertx.core.file.FileSystem;
import io.vertx.core.http.HttpHeaders;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import ru.lsv.librarian2.models.Book;
import ru.lsv.librarian2.util.CommonUtils;
import ru.lsv.librarian2.util.DownloadPreparationService;
import ru.lsv.librarian2.util.DownloadUtils;
import ru.lsv.librarian2.util.DownloadUtils.DownloadPreparationException;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.RestPath;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.RestResponse.ResponseBuilder;

import io.quarkiverse.renarde.Controller;
import io.quarkus.security.Authenticated;
import io.vertx.core.file.OpenOptions;

@Authenticated
@SuppressWarnings("rawtypes")
@ApplicationScoped
public class DownloadService extends Controller {

    private final Logger LOG = Logger.getLogger(DownloadService.class);

    @Inject
    Vertx vertx;

    @Inject
    JsonWebToken principal;

    @Inject
    DownloadPreparationService preparationService;

    @Inject
    DownloadUtils downloadUtils;

    @Path("/download/todownload")
    public List<Integer> getPreparedToDownload() {
        return Book.searchToDownload(principal.getName());
    }

    @Path("/download/numbertodownload")
    public Long getNumberPreparedToRead() {
        return Book.countToDownload(principal.getName());
    }

    /**
     * Will send file to prepration for download
     * 
     * @param bookId       Book id
     * @param downloadType 0 - fb2, 1 - fb2.zip
     * @return 200 without body if file was successfully sent to processing <br/>
     *         200 with int body in case of: <br/>
     *         <ul>
     *         <li>1 - file still in processing</li>
     *         <li>2 - file was already prepared and can be downloaded</li>
     *         </ul>
     *         error status (400/500) in case of errors
     */
    @Path("/download/preparetodownload")
    public RestResponse prepareToDownload(@RestPath Integer bookId, @RestPath Integer downloadType) {
        Pair<Book, RestResponse> checkResult = checkParameters(bookId, downloadType, principal.getName());
        if (checkResult.getRight() != null) {
            return checkResult.getRight();
        }
        Pair<File, File> downloadFiles = downloadUtils.getBookFiles(checkResult.getLeft(), downloadType);
        if (downloadFiles == null) {
            return ResponseBuilder.create(400, "Cannot create temp storage").build();
        } else if (downloadFiles.getLeft().exists() && !downloadFiles.getRight().exists()) {
            return ResponseBuilder.ok(1).build();
        } else if (downloadFiles.getLeft().exists() && downloadFiles.getRight().exists()) {
            return ResponseBuilder.ok(2).build();
        } else {
            LOG.infof("[bookId: %d] Download service got a request for download", checkResult.getLeft().bookId);
            preparationService.prepareForDownload(checkResult.getLeft(), downloadType);
            return ResponseBuilder.ok(0).build();
        }
    }

    @Path("/download/preparedbook")
    public RestResponse downloadPrepared(@RestPath Integer bookId, @RestPath Integer downloadType) {
        Pair<Book, RestResponse> checkResult = checkParameters(bookId, downloadType, principal.getName());
        if (checkResult.getRight() != null) {
            return checkResult.getRight();
        }
        Pair<File, File> bookFiles = downloadUtils.getBookFiles(checkResult.getLeft(), downloadType);
        if (bookFiles.getLeft().exists() && bookFiles.getRight().exists()) {
            return sendFileToDownload(downloadType, checkResult.getLeft(), principal.getName(),
                    bookFiles.getLeft());
        } else {
            return ResponseBuilder.create(400, "Book was not prepared yet").build();
        }
    }

    @Path("/download/book")
    public RestResponse downloadBook(@RestPath Integer bookId, @RestPath Integer downloadType) {
        Pair<Book, RestResponse> checkResult = checkParameters(bookId, downloadType, principal.getName());
        if (checkResult.getRight() != null) {
            return checkResult.getRight();
        }
        File tempOutputFile;
        try {
            tempOutputFile = downloadUtils.prepareForDownload(checkResult.getLeft(), downloadType);
        } catch (DownloadPreparationException e) {
            return ResponseBuilder.create(400, e.getMessage()).build();
        }

        return sendFileToDownload(downloadType, checkResult.getLeft(), principal.getName(), tempOutputFile);
    }

    private Pair<Book, RestResponse> checkParameters(Integer bookId, Integer downloadType,
            String userName) {
        Book book = Book.findById(bookId);
        if (book == null) {
            LOG.errorf("[bookId: %d] Cannot find specified book", bookId);
            return Pair.of(null, ResponseBuilder.create(400, "Cannot find specified book").build());
        }
        if (book.library == null) {
            LOG.errorf("[bookId: %d] Book does not have library", bookId);
            return Pair.of(null, ResponseBuilder.create(400, "Book does not have library").build());
        }
        if (downloadType != 1 && downloadType != 2) {
            LOG.errorf("[bookId: %d] Bad downloadType was provided: %d", bookId, downloadType);
            return Pair.of(book, ResponseBuilder.create(400, "Bad downloadType was provided").build());
        }
        return Pair.of(book, null);
    }

    private RestResponse sendFileToDownload(Integer downloadType, Book book, String userName,
            File fileToDownload) {
        if (fileToDownload != null) {
            FileSystem fileSystem = vertx.fileSystem();
            fileSystem.open(fileToDownload.getAbsolutePath(), new OpenOptions());
            CommonUtils.updateDownloadedBook(book.bookId, userName);
            LOG.infof("[bookId: %d] Sending file to customer", book.bookId, book.id);
            return ResponseBuilder
                    .ok(fileSystem.openBlocking(fileToDownload.getAbsolutePath(), new OpenOptions()),
                            MediaType.APPLICATION_OCTET_STREAM)
                    .encoding(StandardCharsets.UTF_8.toString())
                    .header("filename",
                            fileToDownload.getName())
                    .build();
        } else {
            return ResponseBuilder.create(400, "Cannot find book in archive").build();
        }
    }

}

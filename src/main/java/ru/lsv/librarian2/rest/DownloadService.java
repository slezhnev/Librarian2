package ru.lsv.librarian2.rest;

import io.vertx.core.Vertx;
import io.vertx.core.file.FileSystem;
import io.vertx.core.http.HttpHeaders;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import ru.homyakin.iuliia.Schemas;
import ru.homyakin.iuliia.Translator;
import ru.lsv.librarian2.models.Book;
import ru.lsv.librarian2.models.LibUser;
import ru.lsv.librarian2.util.CommonUtils;
import ru.lsv.librarian2.util.DownloadUtils;
import ru.lsv.librarian2.util.DownloadUtils.DownloadPreparationException;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.RestPath;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.RestResponse.ResponseBuilder;

import io.quarkiverse.renarde.Controller;
import io.vertx.core.file.OpenOptions;

public class DownloadService extends Controller {

    private final Logger LOG = Logger.getLogger(DownloadService.class);

    @Inject
    Vertx vertx;

    @Path("/download/todownload")
    public List<Integer> getPreparedToDownload(@RestPath Integer userId) {
        return Book.searchToDownload(userId);
    }

    @Path("/download/numbertodownload")
    public Long getNumberPreparedToRead(@RestPath Integer userId) {
        return Book.countToDownload(userId);
    }

    @Path("/download/book")
    public RestResponse downloadBook(@RestPath Integer bookId, @RestPath Integer downloadType,
            @RestPath Integer userId) {
        Book book = Book.findById(bookId);
        if (book == null) {
            LOG.errorf("[bookId: %d] Cannot find specified book", bookId);
            return ResponseBuilder.create(400, "Cannot find specified book").build();
        }
        if (downloadType != 1 && downloadType != 2) {
            LOG.errorf("[bookId: %d] Bad downloadType was provided: %d", bookId, downloadType);
            return ResponseBuilder.create(400, "Bad downloadType was provided").build();
        }
        LibUser user = LibUser.findById(userId);
        if (user == null) {
            LOG.errorf("[bookId: %d] Cannot find specified user: %d", bookId, userId);
            return ResponseBuilder.create(400, "Cannot find specified user").build();
        }

        File tempOutputFile;
        try {
            tempOutputFile = DownloadUtils.prepareFotDownload(book, downloadType);
        } catch (DownloadPreparationException e) {
            return ResponseBuilder.create(400, e.getMessage()).build();            
        }

        if (tempOutputFile != null) {
            FileSystem fileSystem = vertx.fileSystem();
            fileSystem.open(tempOutputFile.getAbsolutePath(), new OpenOptions());
            CommonUtils.updateDownloadedBook(book.bookId, user.userId);
            LOG.infof("[bookId: %d] Sending file to customer", bookId, book.id);
            return ResponseBuilder
                    .ok(fileSystem.openBlocking(tempOutputFile.getAbsolutePath(), new OpenOptions()),
                            MediaType.APPLICATION_OCTET_STREAM)
                    .encoding(StandardCharsets.UTF_8.toString())
                    .header(HttpHeaders.CONTENT_DISPOSITION.toString(),
                            "attachment; filename=\"" + tempOutputFile.getName() + "\"")
                    .build();
        } else { 
            return ResponseBuilder.create(400, "Cannot find book in archive").build();
        }
    }

}

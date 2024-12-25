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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.RestPath;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.RestResponse.ResponseBuilder;

import io.quarkiverse.renarde.Controller;
import io.vertx.core.file.OpenOptions;

public class DownloadBook extends Controller {

    private static final Logger LOG = Logger.getLogger(DownloadBook.class);

    @Inject
    Vertx vertx;

    private final Translator translator = new Translator(Schemas.YANDEX_MAPS);

    @Path("/download")
    public RestResponse downloadBook(@RestPath Integer bookId, @RestPath Integer downloadType,
            @RestPath Integer userId) {
        String zipFile = "C:/Users/sele0915/temp/cm-core-api-2023.2.5.zip";

        Book book = Book.findById(bookId);
        if (book == null) {
            LOG.errorf("Cannot find specified book: %d", bookId);
            return ResponseBuilder.create(400, "Cannot find specified book").build();
        }
        if (downloadType != 1 && downloadType != 2) {
            LOG.errorf("Bad downloadType was provided: %d", downloadType);
            return ResponseBuilder.create(400, "Bad downloadType was provided").build();
        }
        LibUser user = LibUser.findById(userId);
        if (user == null) {
            LOG.errorf("Cannot find specified user: %d", userId);
            return ResponseBuilder.create(400, "Cannot find specified user").build();
        }
        // File arcFile = new File(book.library.storagePath
        // + File.separator + book.zipFileName);
        File arcFile = new File(zipFile);
        if (!arcFile.exists()) {
            LOG.errorf("Cannot find arc file '%s' for book: %d", arcFile.getAbsolutePath(), bookId);
            return ResponseBuilder.create(400, "Cannot find library archive file").build();
        }
        String outputFileName = translator.translate(cleanFileName(book.titleWithSerie()));
        switch (downloadType) {
            case 1 -> {
                outputFileName = outputFileName + ".fb2";
            }
            default -> {
                outputFileName = outputFileName + ".fb2.zip";
            }
        }
        File tempOutputFile;
        try {
            tempOutputFile = File.createTempFile("librarian2", outputFileName);
        } catch (IOException ex) {
            LOG.errorf(ex, "Cannot create temp file with prefix: '%s' and suffix: %s", "librarian2", outputFileName);
            return ResponseBuilder.create(500, "Cannot create temp file").build();
        }

        boolean found = false;
        try (OutputStream mainOut = new FileOutputStream(tempOutputFile)) {
            OutputStream out;
            switch (downloadType) {
                case 1 -> out = mainOut;
                default -> out = new ZipArchiveOutputStream(mainOut);
            }
            try (ZipArchiveInputStream arcInput = new ZipArchiveInputStream(new FileInputStream(arcFile))) {
                ArchiveEntry entry;
                while ((entry = arcInput.getNextEntry()) != null) {
                    String id = FilenameUtils.getBaseName(entry.getName());
                    if (id.equals(book.id)) {
                        if (out instanceof ZipArchiveOutputStream zipArchiveOutputStream) {
                            zipArchiveOutputStream.setLevel(9);
                            zipArchiveOutputStream
                                    .putArchiveEntry(new ZipArchiveEntry(book.id + ".fb2"));
                        }
                        IOUtils.copy(arcInput, out);
                        if (out instanceof ZipArchiveOutputStream zipArchiveOutputStream) {
                            zipArchiveOutputStream.closeArchiveEntry();
                            zipArchiveOutputStream.finish();
                        }                        
                        found = true;
                        break;
                    }
                }
            }
        } catch (IOException ex) {
            LOG.errorf(ex, "Got an IOException while preparing file for download for '%s' for book: %d",
                    arcFile.getAbsolutePath(), bookId);
            return ResponseBuilder.create(500, "Exception while forming file to download").build();
        }

        if (found) {
            FileSystem fileSystem = vertx.fileSystem();
            fileSystem.open(tempOutputFile.getAbsolutePath(), new OpenOptions());
            return ResponseBuilder
                    .ok(fileSystem.openBlocking(tempOutputFile.getAbsolutePath(), new OpenOptions()),
                            MediaType.APPLICATION_OCTET_STREAM)
                    .encoding(StandardCharsets.UTF_8.toString())
                    .header(HttpHeaders.CONTENT_DISPOSITION.toString(),
                            "attachment; filename=\"" + outputFileName + "\"")
                    .build();
        } else {
            LOG.errorf("Cannot find book %d (file id: %d) in '%s'",
                    book.bookId, book.id, arcFile.getAbsolutePath());
            return ResponseBuilder.create(400, "Cannot find book in archive").build();
        }
    }

    /**
     * Remove from file some "bad" special symbols <br/>
     * Code from
     * http://stackoverflow.com/questions/1155107/is-there-a-cross-platform-java-method-to-remove-filename-special-chars
     */
    final static int[] illegalChars = { 34, 60, 62, 124, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17,
            18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 58, 42, 63, 92, 47 };
    static {
        Arrays.sort(illegalChars);
    }

    /**
     * Remove from filename special syumbols
     * 
     * @param badFileName Filename to process
     * @return Valid filename
     */
    public static String cleanFileName(String badFileName) {
        StringBuilder cleanName = new StringBuilder();
        for (int i = 0; i < badFileName.length(); i++) {
            int c = (int) badFileName.charAt(i);
            if (Arrays.binarySearch(illegalChars, c) < 0) {
                cleanName.append((char) c);
            }
        }
        // Дополнительно проверим на '..'
        while (cleanName.indexOf("..") > -1) {
            cleanName.replace(cleanName.indexOf(".."), cleanName.indexOf("..") + 2, "__");
        }
        return cleanName.toString();
    }

}

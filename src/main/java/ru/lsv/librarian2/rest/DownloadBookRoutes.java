package ru.lsv.librarian2.rest;

import io.quarkus.vertx.web.Param;
import io.quarkus.vertx.web.Route;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import ru.lsv.librarian2.models.Book;
import ru.lsv.librarian2.models.LibUser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.io.IOUtils;
import org.jboss.logging.Logger;

@ApplicationScoped
public class DownloadBookRoutes {

    private static final Logger LOG = Logger.getLogger(DownloadBookRoutes.class);

    @Inject
    Vertx vertx;

    @Route(path = "/downloadRoutes", methods = Route.HttpMethod.GET, type = Route.HandlerType.BLOCKING)
    public void downloadBook(@Param String bookId, @Param String downloadType, @Param String userId,
            RoutingContext context) {
        String zipFile = "C:/Users/sele0915/temp/cm-core-api-2023.2.5.zip";
        String fileToExtract = "cm-core-api";

        Optional<Integer> bId = toInt(bookId);
        if (bId.isEmpty()) {
            context.response().setStatusCode(400).end("Bad bookId was provided");
            return;
        }
        Book book = Book.findById(bId.get());
        if (book == null) {
            context.response().setStatusCode(400).end("Cannot find specified book");
            return;
        }
        Optional<Integer> dType = toInt(downloadType);
        if (dType.isEmpty() || dType.get() != 1 || dType.get() != 2) {
            context.response().setStatusCode(400).end("Bad downloadType was provided");
            return;
        }
        Optional<Integer> uId = toInt(userId);
        LibUser user = LibUser.findById(uId.get());
        if (user == null) {
            context.response().setStatusCode(400).end("Cannot find specified user");
            return;
        }
        // File arcFile = new File(book.library.storagePath
        // + File.separator + book.zipFileName);
        File arcFile = new File(zipFile);
        if (!arcFile.exists()) {
            LOG.errorf("Cannot find arc file '%s' for book: %d", arcFile.getAbsolutePath(), bId.get());
            context.response().setStatusCode(400).end("Cannot find library archive file");
            return;
        }
        String outputFileName = cleanFileName(book.titleWithSerie());
        switch (dType.get()) {
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
            context.response().setStatusCode(500).end("Cannot create temp file");
            return;
        }
        context.response()
                .putHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8\"" + outputFileName + "\"")
                .putHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM)
                .setStatusCode(200);

        try (OutputStream mainOut = new FileOutputStream(tempOutputFile)) {
            OutputStream out;
            switch (dType.get()) {
                case 1 -> out = mainOut;
                default -> out = new ZipArchiveOutputStream(mainOut);
            }
            try (ZipArchiveInputStream arcInput = new ZipArchiveInputStream(new FileInputStream(arcFile))) {
                ArchiveEntry entry;
                while ((entry = arcInput.getNextEntry()) != null) {
                    String name = entry.getName();
                    String id = name.substring(0, name.indexOf("."));
                    // if (id.equals(book.id)) {
                    if (id.equals(fileToExtract)) {
                        if (out instanceof ZipArchiveOutputStream zipArchiveOutputStream) {
                            zipArchiveOutputStream.setLevel(9);
                            zipArchiveOutputStream
                                    .putArchiveEntry(new ZipArchiveEntry(book.id + ".fb2"));
                        }
                        IOUtils.copy(arcInput, out);
                        if (out instanceof ZipArchiveOutputStream zipArchiveOutputStream) {
                            zipArchiveOutputStream.closeArchiveEntry();
                        }
                        break;
                    }
                }
            }
        } catch (IOException ex) {
            LOG.errorf(ex, "Got an IOException while preparing file for download for '%s' for book: %d",
                    arcFile.getAbsolutePath(), bId.get());
            context.response().setStatusCode(500).end("Exception while forming file to download");
            return;
        }

        context.response().sendFile(tempOutputFile.getAbsolutePath()).onComplete(ar -> {
            // Then transfer is finished - we should delete temp file
            tempOutputFile.deleteOnExit(); 
            tempOutputFile.delete();
            if (ar.succeeded()) {
                // We should also remove mustRead mark and place readed
            }
        });
    }

    private Optional<Integer> toInt(String intVal) {
        try {
            return Optional.of(Integer.valueOf(intVal));
        } catch (NumberFormatException ex) {
            return Optional.empty();
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

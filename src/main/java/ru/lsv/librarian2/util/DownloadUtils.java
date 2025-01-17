package ru.lsv.librarian2.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.RestResponse.ResponseBuilder;

import ru.homyakin.iuliia.Schemas;
import ru.homyakin.iuliia.Translator;
import ru.lsv.librarian2.models.Book;
import ru.lsv.librarian2.rest.DownloadService;
import ru.lsv.librarian2.util.CommonUtils.StoragePathOverrideException;

public class DownloadUtils {

    private static final Logger LOG = Logger.getLogger(DownloadService.class);

    private static final Translator translator = new Translator(Schemas.YANDEX_MAPS);

    private static final String dirName = "librarian2";

    public static File getBookFile(Book book, int downloadType, boolean finishMark) {
        File tmpDir = new File(System.getProperty("java.io.tmpdir"), dirName);
        if (tmpDir.mkdirs()) {
            String name = translator.translate(cleanFileName(book.titleWithSerieAndAuthor()));
            switch (downloadType) {
                case 1 -> {
                    name = name + ".fb2";
                }
                default -> {
                    name = name + ".fb2.zip";
                }
            }
            if (finishMark)
                name = name + ".prepared";
            File outputFile = new File(tmpDir, name);
            outputFile.deleteOnExit();
            return outputFile;
        } else {
            return null;
        }
    }

    public static class DownloadPreparationException extends Exception {
        DownloadPreparationException(String message) {
            super(message);
        }
    };

    public static File prepareFotDownload(Book book, int downloadType) throws DownloadPreparationException {
        if (book.library == null) {
            LOG.errorf("[bookId: %d] Book does not have a link to library", book.bookId);
            throw new DownloadPreparationException("Book does not have a link to library");
        }
        String storagePath;
        try {
            storagePath = CommonUtils.overrideStoragePathIfNeeded(book.library.storagePath, book.library.libraryId);
        } catch (StoragePathOverrideException e) {
            throw new DownloadPreparationException(
                    "Got an exception while trying to override library storage location");
        }
        File arcFile = new File(storagePath
                + File.separator + book.zipFileName);
        if (!arcFile.exists()) {
            LOG.errorf("[bookId: %d] Cannot find arc file '%s' for book", book.bookId, arcFile.getAbsolutePath());
            throw new DownloadPreparationException("Cannot find library archive file");
        }
        LOG.infof("[bookId: %d] Found archive file in library - '%s'", book.bookId, storagePath);

        String tempDir = System.getProperty("java.io.tmpdir");

        // String outputFileName = getBookFileName(book, downloadType);
        File tempOutputFile = getBookFile(book, downloadType, false);
        File tempOutputFilePrepared = getBookFile(book, downloadType, true);
        try {
            if (!tempOutputFile.createNewFile() || tempOutputFilePrepared.exists()) {
                LOG.errorf("[bookId: %d] Cannot create output file - it or '.prepared' file already exists",
                        book.bookId,
                        tempOutputFile.getAbsolutePath());
                throw new DownloadPreparationException(
                        "Cannot create output file - it or '.prepared' file already exists");
            }
        } catch (IOException ex) {
            LOG.errorf(ex, "[bookId: %d] Exception while creation of output file", book.bookId,
                    tempOutputFile.getAbsolutePath());
            throw new DownloadPreparationException("Exception while creation of output file");
        }

        tempOutputFile.deleteOnExit();
        tempOutputFilePrepared.deleteOnExit();

        LOG.infof("[bookId: %d] Temp file '%s' was created. Starting to search a book inside archive", book.bookId,
                tempOutputFile.getAbsolutePath());
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
                        LOG.infof("[bookId: %d] Book found as  '%s'. Starting to copy it to temp file", book.bookId,
                                book.id);
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
                        LOG.infof("[bookId: %d] Book found as '%s'. File copied", book.bookId, book.id);
                        found = true;
                        break;
                    }
                }
            }
        } catch (IOException ex) {
            tempOutputFile.delete();
            tempOutputFilePrepared.delete();
            LOG.errorf(ex, "[bookId: %d] Got an IOException while preparing file for download for '%s' for book",
                    book.bookId,
                    arcFile.getAbsolutePath());
            throw new DownloadPreparationException("Exception while forming file to download");
        }
        if (found) {
            try {
                if (tempOutputFilePrepared.createNewFile()) {
                    return tempOutputFile;
                } else {
                    tempOutputFile.delete();
                    tempOutputFilePrepared.delete();
                    LOG.errorf("[bookId: %d] Cannot create .prepared file for '%s'",
                            book.bookId,
                            arcFile.getAbsolutePath());
                    throw new DownloadPreparationException(
                            "Cannot create file for marking book export process as done");
                }
            } catch (IOException e) {
                tempOutputFile.delete();
                tempOutputFilePrepared.delete();
                LOG.errorf(e, "[bookId: %d] Got an IOException while trying to create .prepared file for '%s'",
                        book.bookId,
                        arcFile.getAbsolutePath());
                throw new DownloadPreparationException("Exception while marking book export process as done");
            }
        } else {
            tempOutputFile.delete();
            tempOutputFilePrepared.delete();
            LOG.errorf("[bookId: %d] Cannot find book (file id: %d) in '%s'",
                    book.bookId, book.id, arcFile.getAbsolutePath());
            return null;
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
        // Check additionally for '..'
        while (cleanName.indexOf("..") > -1) {
            cleanName.replace(cleanName.indexOf(".."), cleanName.indexOf("..") + 2, "__");
        }
        return cleanName.toString();
    }

}

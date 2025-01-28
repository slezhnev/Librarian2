package ru.lsv.librarian2.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jboss.logging.Logger;

import ru.homyakin.iuliia.Schemas;
import ru.homyakin.iuliia.Translator;
import ru.lsv.librarian2.models.Book;
import ru.lsv.librarian2.rest.DownloadService;
import ru.lsv.librarian2.util.CommonUtils.StoragePathOverrideException;

public class DownloadUtils {

    private static final Logger LOG = Logger.getLogger(DownloadService.class);

    private static final Translator translator = new Translator(Schemas.YANDEX_MAPS);

    private static final String dirName = "librarian2";

    static {
        new File(System.getProperty("java.io.tmpdir"), dirName).mkdirs();
    }

    public static File getTempStorage() {
        File tmpDir = new File(System.getProperty("java.io.tmpdir"), dirName);
        tmpDir.deleteOnExit();
        return tmpDir;
    }

    public static enum DownloadTypes {
        FB2("fb2"),
        FB2ZIP("fb2.zip"),
        PREPARED("prepared");

        private String ext;

        DownloadTypes(String ext) {
            this.ext = ext;
        }

        public String getExt() {
            return ext;
        }
    }

    public static Pair<File, File> getBookFiles(Book book, int downloadType) {
        File tmpDir = getTempStorage();
        if (tmpDir.exists()) {
            String name = translator.translate(cleanFileName(book.titleWithSerieAndAuthor()));
            switch (downloadType) {
                case 1 -> {
                    name = name + "." + DownloadTypes.FB2.getExt();
                }
                default -> {
                    name = name + "." + DownloadTypes.FB2ZIP.getExt();
                }
            }
            String finishMarkName = name + "." + DownloadTypes.PREPARED.getExt();
            File outputFile = new File(tmpDir, name);
            File outputMarkFile = new File(tmpDir, finishMarkName);
            outputFile.deleteOnExit();
            outputMarkFile.deleteOnExit();
            return Pair.of(outputFile, outputMarkFile);
        } else {
            return null;
        }
    }

    public static class DownloadPreparationException extends Exception {
        DownloadPreparationException(String message) {
            super(message);
        }
    };

    /**
     * Extract file from archive to temp location <br/>
     * Method will create a two files (in temp location) with fixed names (@see
     * ru.lsv.librarian2.util.DownloadUtils#getBookFile)
     * 
     * @param book         Book
     * @param downloadType Download type: 1 - fb2.zip, otherwise - fb2
     * @return Two files: book file and technical file, which indicates what book
     *         file was fully extracted and prepared
     * @throws DownloadPreparationException
     *                                      Where a set of cases then it will be
     *                                      thrown: <br/>
     *                                      <ul>
     *                                      <li>If book are null or does not have
     *                                      library link</li>
     *                                      <li>If we've got an error while trying
     *                                      to overwrite the storage path (uncommon
     *                                      case)</li>
     *                                      <li>If arc library file does not
     *                                      exists</li>
     *                                      <li>If book file or techical "extracted"
     *                                      in temp location already exists - this
     *                                      case should be tested BEFORE call of
     *                                      this method via getBookFile()</li>
     *                                      <li>If we've got an exception while
     *                                      extracting file from archive or while
     *                                      creating book file or techical
     *                                      "extracted" file</li>
     *                                      </ul>
     */
    public static File prepareForDownload(Book book, int downloadType) throws DownloadPreparationException {
        if (book == null || book.library == null) {
            LOG.errorf("[bookId: %d] Book are null or does not have a link to library", book.bookId);
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

        Pair<File, File> tempFiles = getBookFiles(book, downloadType);
        File tempOutputFile = tempFiles.getLeft();
        File tempOutputFilePrepared = tempFiles.getRight();
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
            tempOutputFile.delete();
            tempOutputFilePrepared.delete();
            throw new DownloadPreparationException("Exception while creation of output file");
        }

        tempOutputFile.deleteOnExit();
        tempOutputFilePrepared.deleteOnExit();

        String bookNameInsideArchive = book.id + ".fb2";
        LOG.infof("[bookId: %d] Temp file '%s' was created. Starting to search a book inside archive as %s",
                book.bookId,
                tempOutputFile.getAbsolutePath(), bookNameInsideArchive);
        boolean found = false;
        try (OutputStream mainOut = new FileOutputStream(tempOutputFile)) {
            OutputStream out;
            switch (downloadType) {
                case 1 -> out = mainOut;
                default -> out = new ZipArchiveOutputStream(mainOut);
            }
            try (ZipFile zip = new ZipFile(arcFile)) {
                ZipEntry entry = zip.getEntry(bookNameInsideArchive);
                if (entry != null) {
                    found = true;
                    LOG.infof("[bookId: %d] Book found. Starting to copy it to temp file", book.bookId,
                            book.id);
                    if (out instanceof ZipArchiveOutputStream zipArchiveOutputStream) {
                        zipArchiveOutputStream.setLevel(9);
                        zipArchiveOutputStream
                                .putArchiveEntry(new ZipArchiveEntry(book.id + ".fb2"));
                    }
                    IOUtils.copy(zip.getInputStream(entry), out);
                    if (out instanceof ZipArchiveOutputStream zipArchiveOutputStream) {
                        zipArchiveOutputStream.closeArchiveEntry();
                        zipArchiveOutputStream.finish();
                    }
                    LOG.infof("[bookId: %d] File copied", book.bookId);
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

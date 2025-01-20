package ru.lsv.librarian2.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jboss.logging.Logger;

import ru.lsv.librarian2.models.Book;
import ru.lsv.librarian2.util.DownloadUtils.DownloadPreparationException;

public class DownloadPreparationService {

    private static volatile DownloadPreparationService instance = null;

    public static synchronized DownloadPreparationService getInstance() {
        if (instance == null)
            instance = new DownloadPreparationService();
        return instance;
    }

    private ExecutorService service = Executors.newFixedThreadPool(4);

    private final Logger LOG = Logger.getLogger(DownloadPreparationService.class);

    public void prepareForDownload(Book book, int downloadType) {
        service.submit(() -> {
            try {
                DownloadUtils.prepareForDownload(book, downloadType);
            } catch (DownloadPreparationException e) {
                LOG.errorf(e, "Got an exception while trying to prepare a book: %d", book.bookId);
            }
        });
    }

}

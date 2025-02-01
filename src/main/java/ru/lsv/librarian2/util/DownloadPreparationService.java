package ru.lsv.librarian2.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jboss.logging.Logger;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import ru.lsv.librarian2.models.Book;
import ru.lsv.librarian2.util.DownloadUtils.DownloadPreparationException;

@Singleton
public class DownloadPreparationService {

    @Inject
    private DownloadUtils downloadUtils;

    private ExecutorService service = Executors.newFixedThreadPool(4);

    private final Logger LOG = Logger.getLogger(DownloadPreparationService.class);

    @Inject
    private Tracer tracer;

    public void prepareForDownload(Book book, int downloadType) {
        LOG.infof("[bookId: %d] Sending to executor service for preparation. DownloadType: %d", book.bookId,
                downloadType);
        Span span = tracer.spanBuilder("prepareBookForDownload").startSpan().setAttribute("bookId", book.bookId)
                .setAttribute("downloadType", downloadType);
        try (var scope = span.makeCurrent()) {
            Context context = Context.current();
            service.submit(context.wrap(() -> {
                try {
                    downloadUtils.prepareForDownload(book, downloadType);
                } catch (DownloadPreparationException e) {
                    LOG.errorf(e, "Got an exception while trying to prepare a book: %d", book.bookId);
                }
            }));
        } finally {
            span.end();
        }
    }

}

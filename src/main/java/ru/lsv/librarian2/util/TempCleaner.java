package ru.lsv.librarian2.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.stream.Stream;

import org.apache.commons.io.FilenameUtils;
import org.jboss.logging.Logger;

import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class TempCleaner {

    @Inject
    private DownloadUtils downloadUtils;

    private final Logger log = Logger.getLogger(TempCleaner.class);

    @Scheduled(cron = "0 0 0 * * ?")
    public void cleanOldTempFiles() {
        log.info(("Starting to cleanup an old temp files formed for book download"));
        File tempStorage = downloadUtils.getTempStorage();
        if (tempStorage.exists()) {
            try (Stream<Path> files = Files.list(tempStorage.toPath())) {
                files.filter(el -> {
                    String ext = FilenameUtils.getExtension(el.getFileName().toString());
                    try {
                        BasicFileAttributes attr = Files.readAttributes(el, BasicFileAttributes.class);
                        // We'll filter files older than day with needed extentions
                        return Duration.between(attr.lastModifiedTime().toInstant(), LocalDateTime.now()).toDays() > 1
                                &&
                                ext != null && !ext.isBlank() && (ext.equals(DownloadUtils.DownloadTypes.FB2.getExt())
                                        || ext.equals(DownloadUtils.DownloadTypes.FB2.getExt()));
                    } catch (IOException e) {
                        // If we cannot get information about file - let's simple skip it
                        return false;
                    }
                }).forEach(file -> {
                    try {
                        Files.deleteIfExists(file);
                        Files.deleteIfExists(file.resolveSibling(
                                file.getFileName() + "." + DownloadUtils.DownloadTypes.PREPARED.getExt()));
                    } catch (IOException e) {
                        log.errorf(e, "Cannot delete file '%s' or it's prepared mark '%s'",
                                file.toAbsolutePath().toString(),
                                file.resolveSibling(
                                        file.getFileName() + "." + DownloadUtils.DownloadTypes.PREPARED.getExt()));
                    }

                });
            } catch (IOException ex) {
                log.errorf(ex, "Got an exception while trying to cleanup old files in '%s'",
                        tempStorage.getAbsolutePath());
            }
        } else {
            log.infof("Temp cleanup was not started - temp storage '%s' does not exists",
                    tempStorage.getAbsolutePath());
        }
        log.info(("Finished cleanup of old temp files formed for book download"));
    }

}

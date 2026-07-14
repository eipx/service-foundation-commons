package io.github.eipx.servicefoundation.commons.observability.metrics.reporter;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;

import static java.nio.file.FileVisitResult.*;

import static org.slf4j.LoggerFactory.*;

public final class ReportCleaner {

    private static final Logger LOGGER = getLogger(ReportCleaner.class);
    private final Path rootMetricsDir;
    private volatile AtomicBoolean cleanUpOngoing = new AtomicBoolean(false);

    public ReportCleaner(Path rootMetricsDir) {
        this.rootMetricsDir = rootMetricsDir;
    }

    public void deleteDirectoriesStrictlyOlderThan(LocalDate limitDate) throws IOException {

        // Be sure that the is only one cleanup running at a time
        if (cleanUpOngoing.compareAndSet(false, true)) {
            try {
                LOGGER.trace("Deleting subdirectories of {} older than {}", rootMetricsDir, limitDate);
                Files.walkFileTree(rootMetricsDir, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                        if (rootMetricsDir.equals(dir)) {
                            return CONTINUE;
                        }
                        String dirName = dir.getFileName().toString();
                        try {
                            LocalDate dirDate = LocalDate.parse(dirName);
                            return dirDate.isBefore(limitDate) ? CONTINUE : SKIP_SUBTREE;
                        } catch (DateTimeParseException e) {
                            LOGGER.trace("Skipping directory without date pattern {}", dir, e);
                            return SKIP_SUBTREE;
                        }
                    }

                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        if (!rootMetricsDir.equals(file.getParent())) {
                            Files.delete(file);
                        }
                        return CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                        if (rootMetricsDir.equals(dir)) {
                            return CONTINUE;
                        }
                        Files.delete(dir);
                        return CONTINUE;
                    }
                });
            } finally {
                cleanUpOngoing.compareAndSet(true, false);
            }
        } else {
            LOGGER.trace("Cleanup of subdirectories of {} already ongoing", rootMetricsDir);
        }

    }

}

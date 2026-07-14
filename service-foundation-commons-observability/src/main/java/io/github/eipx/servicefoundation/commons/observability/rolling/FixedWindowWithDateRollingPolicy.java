package io.github.eipx.servicefoundation.commons.observability.rolling;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;

import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.rolling.RollingPolicyBase;
import ch.qos.logback.core.rolling.helper.CompressionMode;
import ch.qos.logback.core.rolling.helper.DateTokenConverter;
import ch.qos.logback.core.rolling.helper.FileNamePattern;
import ch.qos.logback.core.rolling.helper.RenameUtil;

/**
 * Rolling windows between min index and max index based on file creation date, deleting oldest files first.
 * If you want a max size for the files you need to use it with a SizeBasedTriggeringPolicy
 */
public class FixedWindowWithDateRollingPolicy extends RollingPolicyBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(FixedWindowWithDateRollingPolicy.class);
    protected SimpleDateFormat dateFormat;
    protected RenameUtil util = new RenameUtil();
    protected FileNamePattern fileNamePattern;
    protected String logFolder;
    protected static TimeZone UTC = TimeZone.getTimeZone("UTC");
    protected int maxHistory;
    protected String currentFileName;

    protected Future<?> cleanUpFuture;
    protected boolean cleanHistoryOnStart = false;

    public FixedWindowWithDateRollingPolicy() {
        setMaxHistory(10);
    }

    @Override
    public void rollover() {
        this.cleanUpFuture = runRolloverCleanAsynchronously();
        currentFileName = fileNamePattern.convert(Calendar.getInstance(UTC).getTime());
    }

    @Override
    public String getActiveFileName() {
        return currentFileName;
    }

    /**
     * When starting it will get all the previously created file and get the last one as the active file.
     * if none exist it will create a new file with a name based on the current date.
     */
    @Override
    public void start() {

        addInfo("Starting " + FixedWindowWithDateRollingPolicy.class.getName());
        util.setContext(this.context);

        if (fileNamePatternStr != null) {
            fileNamePattern = new FileNamePattern(fileNamePatternStr, this.context);
            dateFormat = new SimpleDateFormat(fileNamePattern.getPrimaryDateTokenConverter().getDatePattern());
            logFolder = getParentDirectory(fileNamePattern.convert(Calendar.getInstance(UTC).getTime()));
            // Get last file created or create a new one if none exist.
            List<Path> existingSortedFiles = getFilesSortedByDateDescending(logFolder, fileNamePattern);
            setupCurrentFileName(existingSortedFiles);
            compressionMode = CompressionMode.NONE;
        } else {
            throw new IllegalStateException(CoreConstants.SEE_FNP_NOT_SET);
        }

        if (isParentPrudent()) {
            addError("Prudent mode is not supported with FixedWindowWithDateRollingPolicy.");
            throw new IllegalStateException("Prudent mode is not supported.");
        }

        DateTokenConverter<Object> dtc = fileNamePattern.getPrimaryDateTokenConverter();
        if (dtc == null) {
            throw new IllegalStateException("FileNamePattern [" + fileNamePattern.getPattern() + "] does not contain a valid DateToken");
        }

        if (maxHistory < 1) {
            addError("MaxHistory (" + maxHistory + ") cannot be smaller than 1");
            throw new IllegalStateException("MaxHistory (" + maxHistory + ") cannot be smaller than 1");
        }

        addInfo("Will use the pattern " + fileNamePattern);

        if (cleanHistoryOnStart) {
            this.cleanUpFuture = runStartupCleanAsynchronously();
        }

        startRollingPolicy();
    }

    protected void startRollingPolicy() {
        super.start();
    }

    protected void setupCurrentFileName(List<Path> existingSortedFiles) {
        if (existingSortedFiles.isEmpty()) {
            currentFileName = fileNamePattern.convert(Calendar.getInstance(UTC).getTime());
        } else {
            currentFileName = existingSortedFiles.get(0).toString();
        }
    }

    @Override
    public void stop() {
        if (!isStarted()) {
            return;
        }
        waitForAsynchronousJobToStop(cleanUpFuture);
        super.stop();
    }

    public int getMaxHistory() {
        return maxHistory;
    }

    public void setMaxHistory(int maxHistory) {
        this.maxHistory = maxHistory;
    }

    public boolean isCleanHistoryOnStart() {
        return cleanHistoryOnStart;
    }

    public void setCleanHistoryOnStart(boolean cleanHistoryOnStart) {
        this.cleanHistoryOnStart = cleanHistoryOnStart;
    }

    protected String getParentDirectory(String currentFileName) {
        File currentFile = new File(currentFileName);
        return currentFile.getParentFile().getAbsolutePath();
    }

    protected Future<?> runStartupCleanAsynchronously() {
        return runCleanAsynchronously(new AsynchronousFileCleaner(logFolder, fileNamePattern, true));
    }

    protected Future<?> runRolloverCleanAsynchronously() {
        return runCleanAsynchronously(new AsynchronousFileCleaner(logFolder, fileNamePattern, false));
    }

    protected Future<?> runCleanAsynchronously(Runnable runner) {
        ExecutorService executorService = context.getScheduledExecutorService();
        return executorService.submit(runner);
    }

    protected List<Path> getFilesSortedByDateDescending(String parentDirectory, FileNamePattern pattern) {
        return getFiles(parentDirectory, pattern).stream().sorted((Path p1, Path p2) -> {
            try {
                Date date2 = extractDateFromFileName(p2.getFileName().toString());
                Date date1 = extractDateFromFileName(p1.getFileName().toString());
                // This should never happen because we filter them before with a regex
                if (date2 == null) {
                    return 1;
                }
                if (date1 == null) {
                    return -1;
                }
                return date2.compareTo(date1);
            } catch (ParseException e) {
                return -1;
            }
        }).collect(Collectors.toList());
    }

    private List<Path> getFiles(String parentDirectory, FileNamePattern pattern) {
        try (Stream<Path> files = Files.list(Paths.get(parentDirectory))) {
            Pattern regexPattern = Pattern.compile(pattern.toRegex());
            return files.filter(p -> regexPattern.matcher(p.toString().replace("\\", "/")).find()).collect(Collectors.toList());
        } catch (IOException e) {
            LOGGER.error("Could not retrieve file list from directory {}", parentDirectory, e);
            return new ArrayList<>();
        }
    }

    @VisibleForTesting
    Date extractDateFromFileName(String fileName) throws ParseException {
        Pattern pattern = Pattern.compile(fileNamePattern.getPrimaryDateTokenConverter().toRegex());
        Matcher matcher = pattern.matcher(fileName);
        if (matcher.find()) {
            String dateString = matcher.group(0);
            return dateFormat.parse(dateString);
        }
        return null;
    }

    private void waitForAsynchronousJobToStop(Future<?> aFuture) {
        if (aFuture != null) {
            try {
                aFuture.get(CoreConstants.SECONDS_TO_WAIT_FOR_COMPRESSION_JOBS, TimeUnit.SECONDS);
            } catch (TimeoutException e) {
                addError("Timeout while waiting for clean-up job to finish", e);
            } catch (Exception e) {
                addError("Unexpected exception while waiting for clean-up job to finish", e);
            }
        }
    }

    private class AsynchronousFileCleaner implements Runnable {

        private List<Path> files;

        /**
         * @param considerCurrentFileInHistory during a rollover there will be a new file created, only max history -1 of the exiting files should then be kept
         */
        private List<Path> filesToDelete(String parentDirectory, FileNamePattern pattern, boolean considerCurrentFileInHistory) {
            long filesToKeep = considerCurrentFileInHistory ? maxHistory : maxHistory - 1L;
            return getFilesSortedByDateDescending(parentDirectory, pattern)
                    .stream()
                    .skip(filesToKeep)
                    .collect(Collectors.toList());
        }

        AsynchronousFileCleaner(String dir, FileNamePattern fileNamePattern, boolean considerCurrentFileInHistory) {
            files = filesToDelete(dir, fileNamePattern, considerCurrentFileInHistory);
        }

        @Override
        public void run() {
            files.forEach(f -> {
                try {
                    Files.deleteIfExists(f);
                } catch (IOException e) {
                    LOGGER.error("Failed to remove file: {}", f, e);
                }
            });
        }
    }
}

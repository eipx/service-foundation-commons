package io.github.eipx.servicefoundation.commons.observability.rolling;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.zip.ZipOutputStream;

import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.rolling.RolloverFailure;
import ch.qos.logback.core.rolling.helper.CompressionMode;
import ch.qos.logback.core.rolling.helper.DateTokenConverter;
import ch.qos.logback.core.rolling.helper.FileNamePattern;
import ch.qos.logback.core.util.FileSize;

public class ArchivingFixedWindowWithDateRollingPolicy extends FixedWindowWithDateRollingPolicy {

    private int maxArchiveHistory;
    private FileSize maxArchiveSize;
    private String archiveFolder;
    private FileNamePattern archiveNamePatternInternal;
    private String archiveNamePattern;

    public ArchivingFixedWindowWithDateRollingPolicy() {
        compressionMode = CompressionMode.ZIP;
    }

    @Override
    public void start() {
        addInfo("Starting " + ArchivingFixedWindowWithDateRollingPolicy.class.getName());
        util.setContext(this.context);

        if (fileNamePatternStr != null && !fileNamePatternStr.isEmpty()) {
            fileNamePattern = new FileNamePattern(fileNamePatternStr, this.context);
            DateTokenConverter<Object> dtc = fileNamePattern.getPrimaryDateTokenConverter();
            if (dtc == null) {
                throw new IllegalStateException("FileNamePattern [" + fileNamePattern.getPattern() + "] does not contain a valid DateToken");
            }

            addInfo(ArchivingFixedWindowWithDateRollingPolicy.class.getName() + " will use the fileNamePattern " + fileNamePattern);
            dateFormat = new SimpleDateFormat(dtc.getDatePattern());
            logFolder = getParentDirectory(fileNamePattern.convert(Calendar.getInstance(UTC).getTime()));
            setupArchiveFolder();
            // Get last file created or create a new one if none exist.
            List<Path> existingSortedFiles = getFilesSortedByDateDescending(logFolder, fileNamePattern);
            setupCurrentFileName(existingSortedFiles);
        } else {
            throw new IllegalStateException(CoreConstants.SEE_FNP_NOT_SET);
        }

        checkConfigurationBoundaries();
        if (cleanHistoryOnStart) {
            this.cleanUpFuture = runStartupCleanAsynchronously();
        }
        startRollingPolicy();
    }

    private void setupArchiveFolder() {
        if (archiveNamePattern == null || archiveNamePattern.isEmpty()) {
            throw new IllegalStateException("archiveNamePattern cannot be null or empty");
        }
        archiveNamePatternInternal = new FileNamePattern(archiveNamePattern, this.context);
        archiveFolder = getParentDirectory(archiveNamePatternInternal.convert(Calendar.getInstance(UTC).getTime()));
        if (!Files.exists(Paths.get(archiveFolder))) {
            try {
                Files.createDirectory(Paths.get(archiveFolder));
            } catch (IOException e) {
                addError("Cannot create archive folder: [" + archiveFolder + "]", e);
                throw new IllegalStateException("Unable to create archiveFolder");
            }
        }
    }

    private void checkConfigurationBoundaries() {
        if (isParentPrudent()) {
            addError("Prudent mode is not supported with ArchivingFixedWindowWithDateRollingPolicy.");
            throw new IllegalStateException("Prudent mode is not supported.");
        }

        if (maxHistory < 1) {
            addError("MaxHistory (" + maxHistory + ") cannot be smaller than 1");
            throw new IllegalStateException("MaxHistory (" + maxHistory + ") cannot be smaller than 1");
        }

        if (maxArchiveHistory < 1) {
            addError("maxArchiveHistory (" + maxArchiveHistory + ") cannot be smaller than 1");
            throw new IllegalStateException("maxArchiveHistory (" + maxArchiveHistory + ") cannot be smaller than 1");
        }
    }

    @Override
    public void rollover() throws RolloverFailure {
        runRolloverCleanAsynchronously();
        currentFileName = fileNamePattern.convert(Calendar.getInstance(UTC).getTime());
    }

    @Override
    protected Future<?> runRolloverCleanAsynchronously() {
        return runCleanAsynchronously(new AsynchronousArchivingCleaner(logFolder, archiveFolder, fileNamePattern, false));
    }

    @Override
    protected Future<?> runStartupCleanAsynchronously() {
        return runCleanAsynchronously(new AsynchronousArchivingCleaner(logFolder, archiveFolder, fileNamePattern, true));
    }

    public int getMaxArchiveHistory() {
        return this.maxArchiveHistory;
    }

    public void setMaxArchiveHistory(int maxArchiveHistory) {
        this.maxArchiveHistory = maxArchiveHistory;
    }


    public FileNamePattern getArchiveNamePatternInternal() {
        return archiveNamePatternInternal;
    }

    public void setArchiveNamePatternInternal(FileNamePattern archiveNamePatternInternal) {
        this.archiveNamePatternInternal = archiveNamePatternInternal;
    }

    public String getArchiveNamePattern() {
        return archiveNamePattern;
    }

    public void setArchiveNamePattern(String archiveNamePattern) {
        this.archiveNamePattern = archiveNamePattern;
    }

    public FileSize getMaxArchiveSize() {
        return maxArchiveSize;
    }

    public void setMaxArchiveSize(FileSize maxArchiveSize) {
        this.maxArchiveSize = maxArchiveSize;
    }

    private class AsynchronousArchivingCleaner implements Runnable {
        private final Path archivalDir;
        private final List<Path> filesToArchive;

        /**
         * @param considerCurrentFileInHistory during a rollover there will be a new file created, only max history -1 of the exiting files should then be kept
         */
        private List<Path> getFilesToArchive(String parentDirectory, FileNamePattern pattern, boolean considerCurrentFileInHistory) {
            long filesToKeep = considerCurrentFileInHistory ? maxHistory : maxHistory - 1L;
            return getFilesSortedByDateDescending(parentDirectory, pattern)
                    .stream()
                    .skip(filesToKeep)
                    .collect(Collectors.toList());
        }

        public AsynchronousArchivingCleaner(String dir, String archivalDir, FileNamePattern fileNamePattern, boolean considerCurrentFileInHistory) {
            this.filesToArchive = getFilesToArchive(dir, fileNamePattern, considerCurrentFileInHistory);
            this.archivalDir = Paths.get(archivalDir);
        }

        @Override
        public void run() {
            // We need to remove the oldest file first otherwise the name of the archive directory is not correct, therefore we reverse
            Collections.reverse(filesToArchive);
            for (Path fileToArchive : new ArrayList<>(filesToArchive)) {
                try {
                    archiveFile(fileToArchive, archivalDir);
                } catch (Exception e) {
                    addError("Unable to archive file [" + fileToArchive + "]", e);
                }
                try {
                    Files.deleteIfExists(fileToArchive);
                } catch (IOException e) {
                    addError("Failed to remove file: [" + fileToArchive + "]", e);
                }
            }

        }

        private void archiveFile(Path fileToArchive, Path archivalDir) throws IOException {
            List<Path> currentArchiveFiles = getFilesSortedByDateDescending(archivalDir.toString(), archiveNamePatternInternal);
            Path activeArchiveFile;
            if (currentArchiveFiles.isEmpty() || isExceedingMaxArchiveSize(currentArchiveFiles.get(0))) {
                activeArchiveFile = createNewZipFile(fileToArchive);
                rollOverArchiveFiles(currentArchiveFiles);
            } else {
                activeArchiveFile = currentArchiveFiles.get(0);
            }
            writeFileToArchive(fileToArchive, activeArchiveFile);
        }

        private Path createNewZipFile(Path fileToArchivePath) throws IOException {
            Path newArchiveName = getArchiveFileName(fileToArchivePath);
            try (ZipOutputStream zipOut = new ZipOutputStream(Files.newOutputStream(newArchiveName))) {
                // This creates a zip-file, try block supposed to be empty
            }
            return Paths.get(newArchiveName.toString());
        }

        private Path getArchiveFileName(Path fileToArchivePath) {
            Date extractDateFromFileName;
            try {
                extractDateFromFileName = extractDateFromFileName(fileToArchivePath.getFileName().toString());
            } catch (ParseException e) {
                addError("Unable to parse file that needs to be archived to extract the date, resorting to current date", e);
                extractDateFromFileName = new Date();
            }
            return Paths.get(archiveNamePatternInternal.convert(extractDateFromFileName));
        }

        private void writeFileToArchive(Path fileToArchive, Path activeArchiveFile) throws IOException {
            try (FileSystem fs = FileSystems.newFileSystem(activeArchiveFile, this.getClass().getClassLoader())) {
                Path newZipEntryPath = fs.getPath(fileToArchive.getFileName().toString());
                Files.write(newZipEntryPath, Files.readAllBytes(fileToArchive), StandardOpenOption.CREATE);
            }
        }

        private boolean isExceedingMaxArchiveSize(Path activeArchiveFile) throws IOException {
            return Files.size(activeArchiveFile) > maxArchiveSize.getSize();
        }

        private void rollOverArchiveFiles(List<Path> archiveFiles) throws IOException {
            int archiveFilesSize = archiveFiles.size();
            if (archiveFilesSize >= maxArchiveHistory) {
                Path oldestArchiveFile = archiveFiles.get(archiveFilesSize - 1);
                addInfo("Removing the oldest archive: [" + oldestArchiveFile + "], the maximum amount of archives we keep is: [" + maxArchiveHistory + "]");
                Files.deleteIfExists(oldestArchiveFile);
                rollOverArchiveFiles(archiveFiles.subList(0, archiveFilesSize - 1));
            }
        }
    }
}

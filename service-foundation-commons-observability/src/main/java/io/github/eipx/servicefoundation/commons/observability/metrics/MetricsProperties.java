package io.github.eipx.servicefoundation.commons.observability.metrics;

import java.nio.file.Paths;

import org.springframework.boot.context.properties.ConfigurationProperties;

@SuppressWarnings({"WeakerAccess", "unused"})
@ConfigurationProperties(MetricsProperties.PREFIX)
public class MetricsProperties {

    public static final String PREFIX = "metrics";
    public static final String METRIC_DIR = "metrics";

    private long intervalInSeconds = 30;
    private final CsvReporterConfiguration csvReporter;

    public MetricsProperties(String logDir) {
        csvReporter = new CsvReporterConfiguration(logDir);
    }

    public long getIntervalInSeconds() {
        return intervalInSeconds;
    }

    public void setIntervalInSeconds(long intervalInSeconds) {
        this.intervalInSeconds = intervalInSeconds;
    }

    public CsvReporterConfiguration getCsvReporter() {
        return csvReporter;
    }

    public static class CsvReporterConfiguration {

        private boolean enabled = true;
        private String directory;
        private int fileRetentionDays = 15;

        public CsvReporterConfiguration(String logDir) {
            directory = Paths.get(logDir).resolve(METRIC_DIR).toString();
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getDirectory() {
            return directory;
        }

        public void setDirectory(String directory) {
            this.directory = directory;
        }

        public int getFileRetentionDays() {
            return fileRetentionDays;
        }

        public void setFileRetentionDays(int fileRetentionDays) {
            this.fileRetentionDays = fileRetentionDays;
        }
    }

}

package io.github.eipx.servicefoundation.commons.observability.metrics.reporter;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Metered;
import com.codahale.metrics.MetricAttribute;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Snapshot;
import com.codahale.metrics.Timer;
import com.google.common.base.CharMatcher;
import io.github.eipx.servicefoundation.commons.observability.internal.OutcomeHandler;
import io.github.eipx.servicefoundation.commons.observability.internal.OutcomeHandler.Action;

import static java.nio.charset.StandardCharsets.*;
import static java.nio.file.StandardOpenOption.*;
import static java.time.temporal.ChronoUnit.*;

import static com.codahale.metrics.MetricAttribute.*;
import static com.google.common.base.CharMatcher.*;

public final class RollingCsvReporter extends AbstractScheduledReporter<RollingCsvReporter.ReportContext> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RollingCsvReporter.class);
    private static final String GAUGE_HEADER = "value";
    private static final String GAUGE_FORMAT = "%s";
    private static final String COUNTER_HEADER = header(COUNT);
    private static final String COUNTER_FORMAT = "%d";
    private static final String METER_HEADER = header(COUNT, MEAN_RATE, M1_RATE, M5_RATE, M15_RATE);
    private static final String METER_FORMAT = "%d,%f,%f,%f,%f";
    private static final String HISTOGRAM_HEADER = header(COUNT, MAX, MEAN, MIN, STDDEV, P50, P75, P95, P98, P99, P999);
    private static final String HISTOGRAM_FORMAT = "%d,%d,%f,%d,%f,%f,%f,%f,%f,%f,%f";
    private static final String TIMER_HEADER = header(COUNT, MAX, MEAN, MIN, STDDEV, P50, P75, P95, P98, P99, P999, MEAN_RATE, M1_RATE, M5_RATE, M15_RATE);
    private static final String TIMER_FORMAT = "%d,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f";

    private static final CharMatcher CHARACTERS_TO_REPLACE = inRange('0', '9')
            .or(inRange('A', 'Z'))
            .or(inRange('a', 'z'))
            .or(is('.'))
            .or(is('-'))
            .negate()
            .precomputed();

    private static final char REPLACEMENT_CHAR = '.';

    static final Action ACTION = () -> "Writing metrics to CSV file";

    private static String header(MetricAttribute... attributes) {
        return Stream.of(attributes)
                     .map(MetricAttribute::getCode)
                     .collect(Collectors.joining(","));
    }

    public static Builder forRegistry(MetricRegistry registry) {
        return new Builder("rolling-csv-reporter", registry);
    }

    private final Path rootMetricsDir;
    private final int retentionDays;
    private final ReportCleaner reportCleaner;
    private volatile LocalDate lastRollOver;
    private final ConcurrentMap<String, String> csvFilenames;

    private RollingCsvReporter(String reporterName,
                               MetricRegistry registry,
                               MetricFilter filter,
                               TimeUnit rateUnit,
                               TimeUnit durationUnit,
                               ScheduledExecutorService executor,
                               boolean shutdownExecutorOnStop,
                               Set<MetricAttribute> disabledMetricAttributes,
                               Clock clock,
                               String prefix,
                               OutcomeHandler<Action, Exception> outcomeHandler,
                               Path rootMetricsDir,
                               int retentionDays, ReportCleaner reportCleaner) {
        super(reporterName, registry, filter, rateUnit, durationUnit, executor, shutdownExecutorOnStop, disabledMetricAttributes, clock, prefix, ACTION,
              outcomeHandler);
        this.rootMetricsDir = rootMetricsDir;
        this.retentionDays = retentionDays;
        this.lastRollOver = LocalDate.now(clock);
        this.reportCleaner = reportCleaner;
        this.csvFilenames = new ConcurrentHashMap<>();
    }

    @Override
    protected ReportContext preReport() {
        LocalDateTime nowDateTime = LocalDateTime.now(clock);
        String timestamp = nowDateTime.toLocalTime().truncatedTo(SECONDS).toString();
        LocalDate nowDate = nowDateTime.toLocalDate();
        if (!lastRollOver.equals(nowDate)) {
            lastRollOver = nowDate;
            try {
                reportCleaner.deleteDirectoriesStrictlyOlderThan(nowDate.minusDays(retentionDays));
            } catch (IOException e) {
                LOGGER.warn("Could not delete old metrics data", e);
            }
        }
        Path directory = rootMetricsDir.resolve(lastRollOver.toString());
        return new ReportContext(timestamp, directory);
    }


    @Override
    protected void reportTimer(String name, Timer timer, ReportContext reportContext) throws Exception {
        Snapshot snapshot = timer.getSnapshot();
        report(reportContext, name, TIMER_HEADER, TIMER_FORMAT,
               timer.getCount(),
               convertDuration(snapshot.getMax()),
               convertDuration(snapshot.getMean()),
               convertDuration(snapshot.getMin()),
               convertDuration(snapshot.getStdDev()),
               convertDuration(snapshot.getMedian()),
               convertDuration(snapshot.get75thPercentile()),
               convertDuration(snapshot.get95thPercentile()),
               convertDuration(snapshot.get98thPercentile()),
               convertDuration(snapshot.get99thPercentile()),
               convertDuration(snapshot.get999thPercentile()),
               convertRate(timer.getMeanRate()),
               convertRate(timer.getOneMinuteRate()),
               convertRate(timer.getFiveMinuteRate()),
               convertRate(timer.getFifteenMinuteRate()));
    }

    @Override
    protected void reportMetered(String name, Metered meter, ReportContext reportContext) throws Exception {
        report(reportContext, name, METER_HEADER, METER_FORMAT,
               meter.getCount(),
               convertRate(meter.getMeanRate()),
               convertRate(meter.getOneMinuteRate()),
               convertRate(meter.getFiveMinuteRate()),
               convertRate(meter.getFifteenMinuteRate()));
    }

    @Override
    protected void reportHistogram(String name, Histogram histogram, ReportContext reportContext) throws Exception {
        Snapshot snapshot = histogram.getSnapshot();
        report(reportContext, name, HISTOGRAM_HEADER, HISTOGRAM_FORMAT,
               histogram.getCount(),
               snapshot.getMax(),
               snapshot.getMean(),
               snapshot.getMin(),
               snapshot.getStdDev(),
               snapshot.getMedian(),
               snapshot.get75thPercentile(),
               snapshot.get95thPercentile(),
               snapshot.get98thPercentile(),
               snapshot.get99thPercentile(),
               snapshot.get999thPercentile());
    }

    @Override
    protected void reportCounter(String name, Counter counter, ReportContext reportContext) throws Exception {
        report(reportContext, name, COUNTER_HEADER, COUNTER_FORMAT,
               counter.getCount());
    }

    @Override
    protected void reportGauge(String name, Gauge gauge, ReportContext reportContext) throws Exception {
        report(reportContext, name, GAUGE_HEADER, GAUGE_FORMAT,
               gauge.getValue());
    }

    private void report(ReportContext reportContext, String name, String header, String line, Object... values) {
        Path csvFile = reportContext.directory.resolve(getCsvFilename(prefix, name));
        try {
            Files.createDirectories(csvFile.getParent());
        } catch (IOException e) {
            LOGGER.warn("Cannot create directory " + csvFile.getParent(), e);
            return;
        }
        boolean fileAlreadyExists = Files.exists(csvFile);
        try (final PrintWriter out = new PrintWriter(Files.newBufferedWriter(csvFile, UTF_8, CREATE, WRITE, APPEND))) {
            if (!fileAlreadyExists) {
                out.print("t,");
                out.println(header);
            }
            out.printf(Locale.US, String.format(Locale.US, "%s,%s%n", reportContext.timestamp, line), values);
        } catch (IOException e) {
            LOGGER.warn("Error writing to {}", name, e);
        }
    }

    private String getCsvFilename(String prefix, String name) {
        return csvFilenames.computeIfAbsent(name, nm -> createCsvFilename(prefix, nm));
    }

    public static String createCsvFilename(String prefix, String name) {
        StringBuilder filename = new StringBuilder();
        if (prefix != null) {
            filename.append(prefix);
            filename.append('.');
        }
        filename.append(CHARACTERS_TO_REPLACE.replaceFrom(name, REPLACEMENT_CHAR));
        filename.append(".csv");
        return filename.toString();
    }

    public static final class Builder extends AbstractBuilder<Builder, RollingCsvReporter> {

        private Path rootMetricsDir;
        private int retentionDays = 30;
        private ReportCleaner reportCleaner;

        protected Builder(String reporterName, MetricRegistry registry) {
            super(reporterName, registry);
        }

        public Builder withRetentionDays(int retentionDays) {
            this.retentionDays = retentionDays;
            return this;
        }

        public Builder withRootMetricsDir(Path rootMetricsDir) {
            this.rootMetricsDir = rootMetricsDir;
            return this;
        }

        public Builder withReportCleaner(ReportCleaner reportCleaner) {
            this.reportCleaner = reportCleaner;
            return this;
        }

        public RollingCsvReporter build() {
            return new RollingCsvReporter(reporterName,
                                          registry,
                                          filter,
                                          rateUnit,
                                          durationUnit,
                                          executor,
                                          shutdownExecutorOnStop,
                                          disabledMetricAttributes,
                                          clock,
                                          prefix,
                                          outcomeHandler,
                                          rootMetricsDir,
                                          retentionDays, reportCleaner);
        }
    }

    static class ReportContext {
        private final String timestamp;
        private final Path directory;

        private ReportContext(String timestamp, Path directory) {
            this.timestamp = timestamp;
            this.directory = directory;
        }
    }
}

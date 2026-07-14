package io.github.eipx.servicefoundation.commons.observability.metrics.reporter;

import java.nio.file.Path;
import java.nio.file.Paths;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import io.github.eipx.servicefoundation.commons.observability.metrics.MetricReporterFactory;
import io.github.eipx.servicefoundation.commons.observability.metrics.MetricsProperties;
import io.github.eipx.servicefoundation.commons.observability.internal.OutcomeHandler;

public class RollingCsvReporterFactory implements MetricReporterFactory {

    private final MetricRegistry metricRegistry;
    private final String applicationName;
    private final String serviceInstanceId;
    private final MetricsProperties.CsvReporterConfiguration csvReporterConfiguration;
    private final OutcomeHandler<OutcomeHandler.Action, Exception> outcomeHandler;
    private final ReportCleaner reportCleaner;
    private final Path rootMetricsDir;

    public RollingCsvReporterFactory(MetricRegistry metricRegistry, String applicationName, String serviceInstanceId,
                                     MetricsProperties.CsvReporterConfiguration csvReporterConfiguration,
                                     OutcomeHandler<OutcomeHandler.Action, Exception> outcomeHandler) {
        this.metricRegistry = metricRegistry;
        this.applicationName = applicationName;
        this.serviceInstanceId = serviceInstanceId;
        this.csvReporterConfiguration = csvReporterConfiguration;
        this.outcomeHandler = outcomeHandler;
        rootMetricsDir = Paths.get(csvReporterConfiguration.getDirectory());
        reportCleaner = new ReportCleaner(rootMetricsDir);
    }

    @Override
    public ScheduledReporter create() {

        return RollingCsvReporter.forRegistry(metricRegistry)
                                 .outcomeHandler(outcomeHandler)
                                 .prefixedWith(applicationName + "." + serviceInstanceId)
                                 .withRootMetricsDir(rootMetricsDir)
                                 .withRetentionDays(csvReporterConfiguration.getFileRetentionDays())
                                 .withReportCleaner(reportCleaner)
                                 .build();
    }
}

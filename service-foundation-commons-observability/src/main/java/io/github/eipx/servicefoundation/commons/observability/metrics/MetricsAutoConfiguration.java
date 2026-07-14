package io.github.eipx.servicefoundation.commons.observability.metrics;

import java.time.Duration;
import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.metrics.CompositeMeterRegistryAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.metrics.export.simple.SimpleMetricsExportAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

import com.codahale.metrics.MetricRegistry;
import io.github.eipx.servicefoundation.commons.observability.internal.OutcomeHandler;
import io.github.eipx.servicefoundation.commons.observability.internal.OutcomeHandler.Action;
import io.github.eipx.servicefoundation.commons.observability.internal.SmartLoggerOutcomeHandler;
import io.github.eipx.servicefoundation.commons.observability.metrics.reporter.RollingCsvReporterFactory;

import io.micrometer.core.instrument.Clock;
import io.micrometer.observation.ObservationPredicate;

@AutoConfiguration
@AutoConfigureBefore({CompositeMeterRegistryAutoConfiguration.class, SimpleMetricsExportAutoConfiguration.class})
@AutoConfigureAfter(org.springframework.boot.actuate.autoconfigure.metrics.MetricsAutoConfiguration.class)
@ConditionalOnClass({Clock.class, MetricRegistry.class})
@ConditionalOnBean(Clock.class)
public class MetricsAutoConfiguration {

    public static final String METRIC_REPORTING_OUTCOME_HANDLER = "metricReportingOutcomeHandler";

    @Bean
    @ConditionalOnMissingBean
    public MetricRegistry metricRegistry() {
        return new MetricRegistry();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConfigurationProperties(MetricsProperties.PREFIX)
    public MetricsProperties metricsProperties(@Value("${log-dir:logs}") String logDir) {
        return new MetricsProperties(logDir);
    }

    @Bean
    @ConditionalOnMissingBean
    public DefaultDropwizardConfig meterRegistryConfig(MetricsProperties metricsProperties) {
        return new DefaultDropwizardConfig(Duration.ofSeconds(metricsProperties.getIntervalInSeconds()));
    }

    @Bean
    @ConditionalOnMissingBean(DefaultDropwizardMeterRegistry.class)
    public DefaultDropwizardMeterRegistry meterRegistry(
            DefaultDropwizardConfig config, MetricRegistry metricRegistry, Clock clock) {
        return new DefaultDropwizardMeterRegistry(
                config, metricRegistry, MeterTypeHierarchicalNameMapper.getInstance(), clock);
    }

    @Bean
    @ConditionalOnMissingBean
    public static MetricHealthIndicatorRegisterer metricHealthIndicatorRegisterer() {
        return new MetricHealthIndicatorRegisterer();
    }

    @Bean(name = METRIC_REPORTING_OUTCOME_HANDLER)
    @ConditionalOnMissingBean(name = METRIC_REPORTING_OUTCOME_HANDLER)
    public OutcomeHandler<Action, Exception> metricReportingOutcomeHandler() {
        return new SmartLoggerOutcomeHandler<>();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(
            prefix = MetricsProperties.PREFIX,
            name = "csv-reporter.enabled",
            havingValue = "true",
            matchIfMissing = true)
    public RollingCsvReporterFactory metricCsvReporterFactory(
            MetricRegistry metricRegistry,
            MetricsProperties metricsProperties,
            Environment environment,
            @Qualifier(METRIC_REPORTING_OUTCOME_HANDLER) OutcomeHandler<Action, Exception> outcomeHandler) {
        return new RollingCsvReporterFactory(
                metricRegistry,
                environment.getProperty("spring.application.name", "unknown"),
                environment.getProperty("service-instance-id"),
                metricsProperties.getCsvReporter(),
                outcomeHandler);
    }

    @Bean
    @ConditionalOnMissingBean
    public MetricReportersLifecycle metricReportersLifecycle(
            MetricsProperties metricsProperties, List<MetricReporterFactory> reporterFactories) {
        return new MetricReportersLifecycle(metricsProperties, reporterFactories);
    }

    @Bean
    @ConditionalOnProperty(
            prefix = MetricsProperties.PREFIX,
            name = "scheduled.tasks.observation.enabled",
            havingValue = "false",
            matchIfMissing = true)
    public ObservationPredicate disableTaskScheduledExecution() {
        return (name, context) -> !name.startsWith("tasks.scheduled.execution");
    }
}

package io.github.eipx.servicefoundation.commons.observability.metrics.reporter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;

import io.github.eipx.servicefoundation.commons.observability.metrics.MetricsAutoConfiguration;
import io.github.eipx.servicefoundation.commons.observability.internal.OutcomeHandler;

public class RollingCsvReporterHealthIndicator implements HealthIndicator {

    @Autowired
    @Qualifier(MetricsAutoConfiguration.METRIC_REPORTING_OUTCOME_HANDLER)
    private OutcomeHandler<OutcomeHandler.Action, Exception> metricReportingOutcomeHandler;

    @Override
    public Health health() {
        Exception failure = metricReportingOutcomeHandler.getFailure(RollingCsvReporter.ACTION);
        return failure == null
                ? Health.up().build()
                : Health.status("LOCAL ERROR").withException(failure).build();
    }
}

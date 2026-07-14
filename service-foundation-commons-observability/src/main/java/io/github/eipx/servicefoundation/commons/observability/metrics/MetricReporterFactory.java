package io.github.eipx.servicefoundation.commons.observability.metrics;

import com.codahale.metrics.ScheduledReporter;

public interface MetricReporterFactory {

    ScheduledReporter create();
}

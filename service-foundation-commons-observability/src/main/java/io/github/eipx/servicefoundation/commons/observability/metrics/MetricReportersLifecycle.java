package io.github.eipx.servicefoundation.commons.observability.metrics;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;

import com.codahale.metrics.ScheduledReporter;

import static java.util.Objects.*;
import static java.util.concurrent.TimeUnit.*;

import static com.google.common.collect.ImmutableList.*;

public final class MetricReportersLifecycle implements SmartLifecycle {

    private static final Logger LOGGER = LoggerFactory.getLogger(MetricReportersLifecycle.class);

    private final MetricsProperties metricsProperties;
    private final List<MetricReporterFactory> metricReporterFactories;
    private volatile List<ScheduledReporter> reporters;
    private volatile boolean running;

    public MetricReportersLifecycle(MetricsProperties metricsProperties, List<MetricReporterFactory> metricReporterFactories) {
        this.metricsProperties = requireNonNull(metricsProperties, "metricsProperties");
        this.metricReporterFactories = copyOf(requireNonNull(metricReporterFactories, "metricReporterFactories"));
    }

    @Override
    public synchronized void start() {
        if (running) {
            return;
        }
        this.reporters = metricReporterFactories.stream()
                                                .map(MetricReporterFactory::create)
                                                .peek(this::startReporter)
                                                .collect(toImmutableList());
        running = true;
    }

    @Override
    public synchronized void stop() {
        if (!running) {
            return;
        }
        if (reporters != null) {
            reporters.forEach(this::stopReporter);
            reporters = null;
        }
        running = false;
    }

    @Override
    public void stop(Runnable callback) {
        stop();
        callback.run();
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public boolean isAutoStartup() {
        return true;
    }

    @Override
    public int getPhase() {
        return 0;
    }

    private void startReporter(ScheduledReporter reporter) {
        LOGGER.info("Starting scheduled metrics reporter {}", reporter);
        try {
            reporter.start(metricsProperties.getIntervalInSeconds(), SECONDS);
        } catch (Exception e) {
            LOGGER.error("Failed to start scheduled metrics reporter {}", reporter, e);
        }
    }

    private void stopReporter(ScheduledReporter reporter) {
        LOGGER.info("Stopping scheduled metrics reporter {}", reporter);
        try {
            reporter.stop();
        } catch (Exception e) {
            LOGGER.warn("Failed to stop scheduled metrics reporter {}", reporter, e);
        }
    }
}

package io.github.eipx.servicefoundation.commons.observability.metrics.reporter;

import java.time.Clock;
import java.util.Set;
import java.util.SortedMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Metered;
import com.codahale.metrics.MetricAttribute;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import com.codahale.metrics.Timer;
import io.github.eipx.servicefoundation.commons.observability.internal.ExecutorSupport;
import io.github.eipx.servicefoundation.commons.observability.internal.OutcomeHandler;
import io.github.eipx.servicefoundation.commons.observability.internal.OutcomeHandler.Action;
import io.github.eipx.servicefoundation.commons.observability.internal.SmartLoggerOutcomeHandler;

import static java.util.Collections.*;
import static java.util.concurrent.TimeUnit.*;

import static com.codahale.metrics.MetricFilter.*;
import static com.google.common.base.Preconditions.*;

public abstract class AbstractScheduledReporter<T> extends ScheduledReporter {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractScheduledReporter.class);

    protected final Clock clock;
    protected final String prefix;
    private final Action action;
    private final OutcomeHandler<Action, Exception> outcomeHandler;

    protected AbstractScheduledReporter(String reporterName,
                                        MetricRegistry registry,
                                        MetricFilter filter,
                                        TimeUnit rateUnit,
                                        TimeUnit durationUnit,
                                        ScheduledExecutorService executor,
                                        boolean shutdownExecutorOnStop,
                                        Set<MetricAttribute> disabledMetricAttributes,
                                        Clock clock,
                                        String prefix,
                                        Action action,
                                        OutcomeHandler<Action, Exception> outcomeHandler) {
        super(registry, reporterName, filter, rateUnit, durationUnit, executor, shutdownExecutorOnStop, disabledMetricAttributes);
        this.clock = checkNotNull(clock);
        this.prefix = prefix;
        this.outcomeHandler = checkNotNull(outcomeHandler);
        this.action = action;
    }

    @Override
    public final synchronized void report(SortedMap<String, Gauge> gauges,
                                          SortedMap<String, Counter> counters,
                                          SortedMap<String, Histogram> histograms,
                                          SortedMap<String, Meter> meters,
                                          SortedMap<String, Timer> timers) {
        try {
            doReport(gauges, counters, histograms, meters, timers);
            outcomeHandler.onSuccess(action);
        } catch (Exception e) {
            outcomeHandler.onFailure(action, e);
            errorOnReport(e);
        }
    }

    protected void doReport(SortedMap<String, Gauge> gauges,
                            SortedMap<String, Counter> counters,
                            SortedMap<String, Histogram> histograms,
                            SortedMap<String, Meter> meters,
                            SortedMap<String, Timer> timers) throws Exception {
        if (gauges.isEmpty()
                && counters.isEmpty()
                && histograms.isEmpty()
                && meters.isEmpty()
                && timers.isEmpty()) {
            LOGGER.debug("No metric to report");
            return;
        }
        T reportContext = preReport();
        LOGGER.debug("Reporting metrics");
        for (var entry : gauges.entrySet()) {
            reportGauge(entry.getKey(), entry.getValue(), reportContext);
        }
        for (var entry : counters.entrySet()) {
            reportCounter(entry.getKey(), entry.getValue(), reportContext);
        }
        for (var entry : histograms.entrySet()) {
            reportHistogram(entry.getKey(), entry.getValue(), reportContext);
        }
        for (var entry : meters.entrySet()) {
            reportMetered(entry.getKey(), entry.getValue(), reportContext);
        }
        for (var entry : timers.entrySet()) {
            reportTimer(entry.getKey(), entry.getValue(), reportContext);
        }
        postReport();
    }

    protected abstract T preReport() throws Exception;

    protected void postReport() throws Exception {
        // nothing by default
    }

    protected void errorOnReport(Exception exception) {
        // nothing by default
    }

    protected abstract void reportTimer(String name, Timer timer, T reportContext) throws Exception;

    protected abstract void reportMetered(String name, Metered meter, T reportContext) throws Exception;

    protected abstract void reportHistogram(String name, Histogram histogram, T reportContext) throws Exception;

    protected abstract void reportCounter(String name, Counter counter, T reportContext) throws Exception;

    protected abstract void reportGauge(String name, Gauge gauge, T reportContext) throws Exception;

    protected abstract static class AbstractBuilder<I extends AbstractBuilder<I, T>, T extends AbstractScheduledReporter> {

        protected final String reporterName;
        protected final MetricRegistry registry;
        protected Clock clock = Clock.systemUTC();
        protected String prefix = null;
        protected TimeUnit rateUnit = SECONDS;
        protected TimeUnit durationUnit = MILLISECONDS;
        protected MetricFilter filter = ALL;
        protected ScheduledExecutorService executor;
        protected boolean shutdownExecutorOnStop = true;
        protected Set<MetricAttribute> disabledMetricAttributes = emptySet();
        protected OutcomeHandler<Action, Exception> outcomeHandler = new SmartLoggerOutcomeHandler<>();

        protected AbstractBuilder(String reporterName, MetricRegistry registry) {
            this.reporterName = reporterName;
            this.registry = registry;
            this.executor = ExecutorSupport.newSingleThreadScheduler(
                    this.reporterName, t -> LOGGER.error("Unexpected error", t));
        }

        public I shutdownExecutorOnStop(boolean shutdownExecutorOnStop) {
            this.shutdownExecutorOnStop = shutdownExecutorOnStop;
            return getThis();
        }

        public I scheduleOn(ScheduledExecutorService executor) {
            this.executor = executor;
            return getThis();
        }

        public I withClock(Clock clock) {
            this.clock = clock;
            return getThis();
        }

        public I prefixedWith(String prefix) {
            this.prefix = prefix;
            return getThis();
        }

        public I convertRatesTo(TimeUnit rateUnit) {
            this.rateUnit = rateUnit;
            return getThis();
        }

        public I convertDurationsTo(TimeUnit durationUnit) {
            this.durationUnit = durationUnit;
            return getThis();
        }

        public I filter(MetricFilter filter) {
            this.filter = filter;
            return getThis();
        }

        public I disabledMetricAttributes(Set<MetricAttribute> disabledMetricAttributes) {
            this.disabledMetricAttributes = disabledMetricAttributes;
            return getThis();
        }

        public I outcomeHandler(OutcomeHandler<Action, Exception> outcomeHandler) {
            this.outcomeHandler = outcomeHandler;
            return getThis();
        }

        protected final I getThis() {
            return (I) this;
        }

        public abstract T build();
    }
}

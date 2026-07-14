package io.github.eipx.servicefoundation.commons.observability.metrics;

import com.codahale.metrics.MetricRegistry;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.dropwizard.DropwizardConfig;
import io.micrometer.core.instrument.dropwizard.DropwizardMeterRegistry;
import io.micrometer.core.instrument.util.HierarchicalNameMapper;

import static java.util.Objects.*;

public final class DefaultDropwizardMeterRegistry extends DropwizardMeterRegistry {

    private final DropwizardConfig config;

    private final MetricRegistry registry;

    private final HierarchicalNameMapper nameMapper;

    public DefaultDropwizardMeterRegistry(DropwizardConfig config, MetricRegistry registry, HierarchicalNameMapper nameMapper,
                                          Clock clock) {
        super(config, registry, nameMapper, clock);
        this.config = requireNonNull(config, "config");
        this.registry = requireNonNull(registry, "registry");
        this.nameMapper = requireNonNull(nameMapper, "nameMapper");
    }

    @Override
    protected Double nullGaugeValue() {
        return null;
    }

    @Override
    public String toString() {
        return "DefaultDropwizardMeterRegistry{" +
                "config=" + config +
                ", registry=" + registry +
                ", nameMapper=" + nameMapper +
                ", clock=" + clock +
                '}';
    }
}

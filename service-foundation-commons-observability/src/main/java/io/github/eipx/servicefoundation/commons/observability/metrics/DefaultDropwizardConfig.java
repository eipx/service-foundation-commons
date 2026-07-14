package io.github.eipx.servicefoundation.commons.observability.metrics;

import java.time.Duration;

import io.micrometer.core.instrument.dropwizard.DropwizardConfig;

import static java.util.Objects.*;

public final class DefaultDropwizardConfig implements DropwizardConfig {

    private final Duration step;

    public DefaultDropwizardConfig(Duration step) {
        this.step = requireNonNull(step, "step");
    }

    @Override
    public String prefix() {
        return "";
    }

    @Override
    public String get(String key) {
        return null;
    }

    @Override
    public Duration step() {
        return step;
    }

    @Override
    public String toString() {
        return "DefaultDropwizardConfig{" +
                "step=" + step +
                '}';
    }
}

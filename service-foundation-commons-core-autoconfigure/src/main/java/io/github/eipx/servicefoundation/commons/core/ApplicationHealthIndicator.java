package io.github.eipx.servicefoundation.commons.core;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;

public class ApplicationHealthIndicator implements HealthIndicator {

    private final ApplicationStatus status;

    public ApplicationHealthIndicator(ApplicationStatus status) {
        this.status = status;
    }

    @Override
    public Health health() {
        return Health.status(status.get()).build();
    }
}

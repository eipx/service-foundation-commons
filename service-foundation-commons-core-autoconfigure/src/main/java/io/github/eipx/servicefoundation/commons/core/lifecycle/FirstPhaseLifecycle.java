package io.github.eipx.servicefoundation.commons.core.lifecycle;

import io.github.eipx.servicefoundation.commons.core.ApplicationStatus;

public class FirstPhaseLifecycle extends SimpleSmartLifecycle {

    private final ApplicationStatus applicationStatus;

    public FirstPhaseLifecycle(ApplicationStatus applicationStatus) {
        super(FIRST_STARTUP_LAST_SHUTDOWN_PHASE);
        this.applicationStatus = applicationStatus;
    }

    @Override
    protected void doStart() {
        applicationStatus.set(ApplicationStatus.STARTING_UP);
    }

    @Override
    protected void doShutdown() {
        applicationStatus.set(ApplicationStatus.DOWN);
    }
}

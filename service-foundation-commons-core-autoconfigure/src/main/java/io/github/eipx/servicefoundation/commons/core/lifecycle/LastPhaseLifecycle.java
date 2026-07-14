package io.github.eipx.servicefoundation.commons.core.lifecycle;

import io.github.eipx.servicefoundation.commons.core.ApplicationStatus;
import io.github.eipx.servicefoundation.commons.lifecycle.ApplicationShutdown;
import io.github.eipx.servicefoundation.commons.lifecycle.ApplicationStartup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class LastPhaseLifecycle extends SimpleSmartLifecycle implements ApplicationContextAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(LastPhaseLifecycle.class);

    private final ApplicationStatus applicationStatus;
    private ApplicationContext applicationContext;

    public LastPhaseLifecycle(ApplicationStatus applicationStatus) {
        super(LAST_STARTUP_FIRST_SHUTDOWN_PHASE);
        this.applicationStatus = applicationStatus;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    protected void doStart() {
        applicationContext.getBeansOfType(ApplicationStartup.class)
                .values()
                .forEach(applicationStartup -> {
                    LOGGER.debug("Starting up {}", applicationStartup);
                    try {
                        applicationStartup.syncStartup();
                    } catch (RuntimeException exception) {
                        doShutdown();
                        throw exception;
                    }
                });
        applicationStatus.set(ApplicationStatus.UP);
    }

    @Override
    protected void doShutdown() {
        applicationStatus.set(ApplicationStatus.SHUTTING_DOWN);
        applicationContext.getBeansOfType(ApplicationShutdown.class)
                .values()
                .forEach(applicationShutdown -> {
                    LOGGER.debug("Shutting down {}", applicationShutdown);
                    try {
                        applicationShutdown.syncShutdown();
                    } catch (Exception exception) {
                        LOGGER.warn("Failed to shutdown {}", applicationShutdown, exception);
                    }
                });
    }
}

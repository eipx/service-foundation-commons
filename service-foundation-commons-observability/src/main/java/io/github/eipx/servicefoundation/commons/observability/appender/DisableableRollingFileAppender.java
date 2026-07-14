package io.github.eipx.servicefoundation.commons.observability.appender;

import ch.qos.logback.core.rolling.RollingFileAppender;

public class DisableableRollingFileAppender<E> extends RollingFileAppender<E> {

    private boolean enabled = true;

    @Override
    public void start() {
        if (enabled) {
            super.start();
        }
    }

    @Override
    public void doAppend(E eventObject) {
        if (enabled) {
            super.doAppend(eventObject);
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}

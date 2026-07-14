package io.github.eipx.servicefoundation.commons.observability.appender;

import ch.qos.logback.classic.spi.ILoggingEvent;
import net.logstash.logback.appender.LogstashTcpSocketAppender;

public class DisableableLogstashTcpSocketAppender extends LogstashTcpSocketAppender {

    private boolean enabled = true;

    @Override
    public synchronized void start() {
        if (enabled) {
            super.start();
        }
    }

    @Override
    public void doAppend(ILoggingEvent eventObject) {
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

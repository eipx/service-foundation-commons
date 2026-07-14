package io.github.eipx.servicefoundation.commons.observability.activitylog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;

/**
 * Default implementation of {@link ActivityLog} based on slf4j {@link Logger}.
 * Activity trace are logged using a logger named {@link #ACTIVITY_LOG_LOGGER_NAME}
 */
public class DefaultActivityLog implements ActivityLog {

    @VisibleForTesting
    static final String ACTIVITY_LOG_LOGGER_NAME = "activity";

    /**
     * Logger to be used by the Activity Log appender
     */
    private static final Logger ACTIVITY_LOG = LoggerFactory.getLogger(ACTIVITY_LOG_LOGGER_NAME);

    @Override
    public void log(ActivityTraceBuilder activityTrace) {
        ACTIVITY_LOG.info(activityTrace.build().toString());
    }
}

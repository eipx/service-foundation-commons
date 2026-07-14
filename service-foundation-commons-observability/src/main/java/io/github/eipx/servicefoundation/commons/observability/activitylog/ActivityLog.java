package io.github.eipx.servicefoundation.commons.observability.activitylog;

/**
 * Interface to log activity in the activity log.
 */
public interface ActivityLog {

    /**
     * Logs activity trace in the Activity Log
     *
     * @param activityTrace
     */
    void log(ActivityTraceBuilder activityTrace);
}

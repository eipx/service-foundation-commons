package io.github.eipx.servicefoundation.commons.observability.event;

/**
 * MDC constant that are used for the event journal
 */
final class MdcConstants {

    /**
     * Key to put/get the event code in the MDC
     */
    static final String MDC_EVENT_CODE = "event_code";

    /**
     * Key to put/get the event name in the MDC
     */
    static final String MDC_EVENT_NAME = "event_name";

    /**
     * Key to put/get the event category in the MDC
     */
    static final String MDC_EVENT_CATEGORY = "event_category";

    /**
     * Key to put/get the event severity in the MDC
     */
    static final String MDC_EVENT_SEVERITY = "event_severity";

    private MdcConstants() {}
}

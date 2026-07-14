package io.github.eipx.servicefoundation.commons.observability.event;

import static com.google.common.base.MoreObjects.*;

/**
 * A builder to build {@link Event}s that can be logged in the event journal
 */
public final class EventBuilder {

    /**
     * Builds an {@link Event}s that can be logged in the event journal
     * <p>
     *
     * @param component the code of the micro-service that generated the event
     * @param code      the code of the event to build
     * @param severity  the severity of the event to build
     * @param category  the category of the event to build
     * @param title     the title of the event to build
     */
    public static Event buildEvent(String component, int code, Event.Severity severity, Event.Category category, String title) {
        return buildEvent(component, code, severity, category, Event.RetryLogic.NONE, title);
    }

    /**
     * Builds an {@link Event}s that can be logged in the event journal
     * <p>
     *
     * @param component  the code of the micro-service that sent the event
     * @param code       the code of the event to build
     * @param severity   the severity of the event to build
     * @param category   the category of the event to build
     * @param retryLogic the expected retry mechanism to be used by gRPC client
     * @param title      the title of the event to build
     */
    public static Event buildEvent(String component, int code, Event.Severity severity, Event.Category category, Event.RetryLogic retryLogic, String title) {
        return new Event() {

            @Override
            public String component() {
                return component;
            }

            @Override
            public int code() {
                return code;
            }

            @Override
            public Severity severity() {
                return severity;
            }

            @Override
            public Category category() {
                return category;
            }

            @Override
            public String title() {
                return title;
            }

            @Override
            public RetryLogic retryLogic() {
                return retryLogic;
            }

            @Override
            public String toString() {
                return toStringHelper(this)
                        .add("code", code)
                        .add("severity", severity)
                        .add("category", category)
                        .add("title", title)
                        .toString();
            }
        };
    }

    private EventBuilder() {}
}

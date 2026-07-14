package io.github.eipx.servicefoundation.commons.observability.event;

import java.io.Serializable;
import java.util.function.Supplier;

/**
 * The definition of an event that can be logged in the event journal
 */
public interface Event extends Serializable {

    /**
     * The available severities for an event
     */
    enum Severity {
        INFO, WARNING, ERROR, FATAL
    }

    /**
     * The category of the event as they must be output in the event journal
     */
    enum Category {
        /**
         * Event linked to a violated message <b>precondition</b>, such as invalid payload,
         * headers, routing metadata, or processing context.
         * {@link RuntimeException}s (bugs) must be of category {@link Category#SOFTWARE}
         */
        MESSAGE(1),
        /**
         * Event linked to communication with an external message broker, network service,
         * or authentication provider.
         */
        COMMUNICATION(2),
        /**
         * Event linked to security
         */
        SECURITY(3),
        /**
         * Event linked to internal problems (setup, config, JVM, bugs, ...)
         */
        SOFTWARE(4),
        /**
         * Event linked to a configuration problem
         */
        CONFIGURATION(5);

        private final int id;

        Category(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }
    }

    enum RetryLogic {
        /**
         * Used for events not returned as gRPC errors.
         */
        NONE,
        /**
         * Retries are allowed, but shouldn't be done immediatly on this instance of the microservice
         */
        RETRY_LATER,
        /**
         * Immediate retries allowed, transient error only.
         */
        RETRY_DIRECTLY,
        /**
         * No retry allowed.
         */
        INVALID_REQUEST
    }

    /**
     * @return the code of the micro-service that generated the event
     */
    String component();

    /**
     * @return the code of the event
     */
    int code();

    /**
     * Helper method to retrieve the gRPC full code of this event.
     *
     * @param eventSupplier supplier of this event
     * @return this event's code
     */
    static int fullCode(Supplier<Event> eventSupplier) {
        return eventSupplier.get().code();
    }

    /**
     * @return the severity of the event
     */
    Severity severity();

    /**
     * @return the category of the event
     */
    Category category();

    /**
     * @return the title of the event
     */
    String title();

    /**
     * @return the expected logic to be applied by the client for retries
     */
    RetryLogic retryLogic();
}

package io.github.eipx.servicefoundation.commons.observability.integrity;

import java.util.function.Supplier;

import io.github.eipx.servicefoundation.commons.observability.event.DocumentedEvent;
import io.github.eipx.servicefoundation.commons.observability.event.Event;
import io.github.eipx.servicefoundation.commons.observability.event.EventBuilder;

public enum IntegrityEvent implements Supplier<Event> {
    @DocumentedEvent
    FILE_INTEGRITY_ERROR(301, Event.Severity.ERROR, Event.Category.SECURITY, "File integrity error detected"),
    @DocumentedEvent
    MEMORY_INTEGRITY_ERROR(302, Event.Severity.ERROR, Event.Category.SECURITY, "Memory integrity error detected");

    private static final String DEFAULT_COMPONENT = "INTEGRITY";
    private final Event event;

    IntegrityEvent(int code, Event.Severity severity, Event.Category category, String title) {
        event = EventBuilder.buildEvent(DEFAULT_COMPONENT, code, severity, category, title);
    }

    @Override
    public Event get() {
        return event;
    }
}

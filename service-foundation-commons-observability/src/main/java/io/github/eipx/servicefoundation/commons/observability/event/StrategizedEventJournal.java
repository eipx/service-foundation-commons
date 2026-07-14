package io.github.eipx.servicefoundation.commons.observability.event;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.springframework.lang.Nullable;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import io.github.eipx.servicefoundation.commons.observability.cef.CEFEvent;

/**
 * Allows {@link Event} to be modified before logging them in an {@link EventJournal}
 * <p/>
 * {@link Event} can be modified as follow:
 * <ul>
 * <li>
 * The component can be updated to allow a generic event to be reported by different microservices.
 * </li>
 * <li>
 * The title can be updated with a list of parameters.
 * The title has to follow slf4j like syntax.
 * </li>
 * </ul>
 * If an event logged is not present in events given at the constructor, it is logged as it
 */
public class StrategizedEventJournal implements EventJournal {

    @VisibleForTesting
    public final ImmutableMap<Event, Event> updatedEvents;
    private final EventJournal delegate;

    public StrategizedEventJournal(EventJournal delegate, Event[] events, @Nullable String component, @Nullable List<String> params) {
        this.delegate = delegate;
        updatedEvents = ImmutableMap.copyOf(updateEvents(events, component, params));
    }

    @Override
    public void log(Event event, Throwable throwable) {
        Event updatedEvent = updatedEvents.getOrDefault(event, event);
        delegate.log(updatedEvent, throwable);
    }

    @Override
    public void log(Supplier<Event> eventSupplier, Throwable throwable) {
        log(eventSupplier.get(), throwable);
    }

    @Override
    public void log(Event event, String description, Object... descriptionParamsAndEx) {
        Event updatedEvent = updatedEvents.getOrDefault(event, event);
        delegate.log(updatedEvent, description, descriptionParamsAndEx);
    }

    @Override
    public CEFEvent createCefEvent(Event event, String description, Object... descriptionParamsAndEx) {
        Event updatedEvent = updatedEvents.getOrDefault(event, event);
        return delegate.createCefEvent(updatedEvent, description, descriptionParamsAndEx);
    }

    @Override
    public void log(Supplier<Event> eventSupplier, String description, Object... descriptionParamsAndEx) {
        Event updatedEvent = updatedEvents.getOrDefault(eventSupplier.get(), eventSupplier.get());
        delegate.log(updatedEvent, description, descriptionParamsAndEx);
    }

    private Map<Event, Event> updateEvents(Event[] events, @Nullable String component, @Nullable List<String> params) {
        Map<Event, Event> toUpdateMap = new HashMap<>();
        for (Event event : events) {
            String updatedTitle = event.title();
            if (params != null) {
                for (String param : params) {
                    updatedTitle = updatedTitle.replaceFirst("\\{}", param);
                }
            }
            toUpdateMap.put(event, EventBuilder.buildEvent(component != null ? component : event.component(),
                                                           event.code(),
                                                           event.severity(),
                                                           event.category(),
                                                           updatedTitle));
        }
        return toUpdateMap;
    }
}

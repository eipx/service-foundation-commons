package io.github.eipx.servicefoundation.commons.events.maven.plugin;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import io.github.eipx.servicefoundation.commons.events.maven.plugin.model.EventModel;
import io.github.eipx.servicefoundation.commons.observability.event.DocumentedEvent;
import io.github.eipx.servicefoundation.commons.observability.event.Event;

public class EventsModelMapper {

    private final String product;
    private final String applicationShortName;
    private final List<String> eventTitleParams;

    public EventsModelMapper(String product, String applicationShortName, List<String> eventTitleParams) {
        this.product = product;
        this.applicationShortName = applicationShortName;
        this.eventTitleParams = eventTitleParams;
    }

    public List<EventModel> mapToModel(Set<Field> fields) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        List<EventModel> events = new ArrayList<>();

        Method getMethod = Supplier.class.getMethod("get");

        for (Field f : fields) {
            if (Supplier.class.isAssignableFrom(f.getType())) {
                Object eventObj = getMethod.invoke(Enum.valueOf((Class<Enum>) f.getType(), f.getName()));

                events.add(new EventModel(product, applicationShortName, eventTitleParams, (Event) eventObj));

            } else {
                throw new RuntimeException(String.format("Field [%s] annotated with [%s] is not an instance of [%s]", f, DocumentedEvent.class, Supplier
                        .class));
            }
        }

        return events.stream().sorted(Comparator.comparing(EventModel::code)).collect(Collectors.toList());
    }
}

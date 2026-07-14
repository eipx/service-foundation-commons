package io.github.eipx.servicefoundation.commons.events.maven.plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import io.github.eipx.servicefoundation.commons.events.maven.plugin.model.EventModel;
import io.github.eipx.servicefoundation.commons.events.maven.plugin.model.ServiceModel;

public class ServiceValidator {

    public void validate(ServiceModel service) {

        Map<String, Integer> eventCodeCount = new HashMap<>();

        for (EventModel event : service.getEvents()) {
            eventCodeCount.compute(event.code(), (k, v) -> v == null ? 1 : v + 1);
        }

        String codesUsedMoreThanOnce = eventCodeCount.entrySet()
                                                     .stream()
                                                     .filter(entry -> entry.getValue() > 1)
                                                     .map(entry -> entry.getKey())
                                                     .sorted()
                                                     .collect(Collectors.joining(","));

        if (!codesUsedMoreThanOnce.isEmpty()) {
            throw new RuntimeException(String.format("The following event codes are used more than once [%s]", codesUsedMoreThanOnce));
        }
    }
}

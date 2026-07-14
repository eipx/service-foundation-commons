package io.github.eipx.servicefoundation.commons.events.maven.plugin.model;

import java.util.List;

public class ServiceModel {

    private final String serviceName;

    private final List<EventModel> events;

    public ServiceModel(String serviceName, List<EventModel> events) {
        this.serviceName = serviceName;
        this.events = events;
    }

    public String getServiceName() {
        return serviceName;
    }

    public List<EventModel> getEvents() {
        return events;
    }
}

package io.github.eipx.servicefoundation.commons.events.maven.plugin.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.github.eipx.servicefoundation.commons.observability.event.Event;

@JsonPropertyOrder({EventModel.CODE, EventModel.SEVERITY, EventModel.CATEGORY, EventModel.TITLE})
public class EventModel {

    static final String CODE = "code";
    static final String SEVERITY = "severity";
    static final String CATEGORY = "category";
    static final String TITLE = "title";

    private final String product;
    private final String applicationShortName;
    private final List<String> eventTitlesParams;
    private final Event event;

    public EventModel(String product, String applicationShortName, List<String> eventTitlesParams, Event event) {
        this.product = product;
        this.applicationShortName = applicationShortName;
        this.eventTitlesParams = eventTitlesParams;
        this.event = event;
    }

    @JsonProperty(value = CODE)
    public String code() {
        return String.format("%s.%s.%04d", product, applicationShortName != null ? applicationShortName : event.component(), event.code());
    }

    @JsonProperty(value = SEVERITY)
    public Event.Severity severity() {
        return event.severity();
    }

    @JsonProperty(value = CATEGORY)
    public Event.Category category() {
        return event.category();
    }

    @JsonProperty(value = TITLE)
    public String title() {
        String updatedTitle = event.title();
        if (eventTitlesParams != null) {
            for (String param : eventTitlesParams) {
                updatedTitle = updatedTitle.replaceFirst("\\{}", param);
            }
        }
        return updatedTitle;
    }

}

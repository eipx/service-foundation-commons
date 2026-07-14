package io.github.eipx.servicefoundation.commons.observability.event;

import io.github.eipx.servicefoundation.commons.observability.cef.CEFEvent;

public final class EventToCEFMapper {

    private EventToCEFMapper() {
    }

    public static CEFEvent.Severity getSeverity(Event.Severity eventSeverity) {
        CEFEvent.Severity severity = CEFEvent.Severity.Low;
        if (eventSeverity != null) {
            switch (eventSeverity) {
                case INFO:
                    severity = CEFEvent.Severity.Low;
                    break;
                case WARNING:
                    severity = CEFEvent.Severity.Medium;
                    break;
                case ERROR:
                    severity = CEFEvent.Severity.High;
                    break;
                case FATAL:
                    severity = CEFEvent.Severity.VeryHigh;
                    break;
            }
        }
        return severity;
    }
}

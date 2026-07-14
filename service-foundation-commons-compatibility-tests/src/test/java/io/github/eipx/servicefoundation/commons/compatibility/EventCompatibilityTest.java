package io.github.eipx.servicefoundation.commons.compatibility;

import io.github.eipx.servicefoundation.commons.observability.event.Event;
import io.github.eipx.servicefoundation.commons.observability.event.EventBuilder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EventCompatibilityTest {

    @Test
    void eventBuilderPreservesTheUrcVisibleEventFieldsAndDefaultRetryLogic() {
        Event event = EventBuilder.buildEvent(
                "SampleService",
                101,
                Event.Severity.ERROR,
                Event.Category.SOFTWARE,
                "SampleService startup failed");

        assertEquals("SampleService", event.component());
        assertEquals(101, event.code());
        assertEquals(Event.Severity.ERROR, event.severity());
        assertEquals(Event.Category.SOFTWARE, event.category());
        assertEquals("SampleService startup failed", event.title());
        assertEquals(Event.RetryLogic.NONE, event.retryLogic());
    }
}

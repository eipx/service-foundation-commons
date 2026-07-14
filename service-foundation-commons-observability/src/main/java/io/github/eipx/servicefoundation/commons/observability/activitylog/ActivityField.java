package io.github.eipx.servicefoundation.commons.observability.activitylog;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
public @interface ActivityField {

    String name();

    Type type();

    String description();

    int column();

    enum Type {
        STRING,
        DATETIME_EPOCH_MICROS,
        DATETIME_EPOCH_MILLIS,
        DATETIME_EPOCH_SECONDS,
        INTEGER,
        UTC_FORMATTED_TIMESTAMP,
        DURATION_MILLIS
    }
}

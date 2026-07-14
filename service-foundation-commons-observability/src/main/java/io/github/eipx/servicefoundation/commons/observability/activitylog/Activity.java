package io.github.eipx.servicefoundation.commons.observability.activitylog;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
public @interface Activity {
    String[] names();

}

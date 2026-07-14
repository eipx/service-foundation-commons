package io.github.eipx.servicefoundation.commons.core;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.boot.actuate.health.Status;

public class ApplicationStatus {

    public static final Status UP = Status.UP;
    public static final Status DOWN = Status.DOWN;
    public static final Status UNKNOWN = Status.UNKNOWN;
    public static final Status STARTING_UP = new Status("STARTING_UP");
    public static final Status PARTIALLY_UP = new Status("PARTIALLY_UP");
    public static final Status SHUTTING_DOWN = new Status("SHUTTING_DOWN");
    public static final Status ISOLATED = new Status("ISOLATED");
    public static final Status LOCAL_ERROR = new Status("LOCAL ERROR");

    public static final List<Status> DEFAULT_ORDER = List.of(
            DOWN,
            PARTIALLY_UP,
            STARTING_UP,
            SHUTTING_DOWN,
            ISOLATED,
            UP,
            UNKNOWN);

    private final AtomicReference<Status> current = new AtomicReference<>(DOWN);

    public void set(Status status) {
        current.set(status);
    }

    public Status get() {
        return current.get();
    }
}

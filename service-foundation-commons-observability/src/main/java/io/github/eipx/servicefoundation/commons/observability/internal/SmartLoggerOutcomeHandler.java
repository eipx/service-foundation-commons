package io.github.eipx.servicefoundation.commons.observability.internal;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;

public class SmartLoggerOutcomeHandler<A extends OutcomeHandler.Action, T extends Throwable>
        implements OutcomeHandler<A, T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SmartLoggerOutcomeHandler.class);

    private final ConcurrentMap<String, T> failures = new ConcurrentHashMap<>();

    @Override
    public final void onSuccess(A action) {
        if (failures.remove(action.id()) != null) {
            LOGGER.info("{} succeeded", action.id());
        }
    }

    @Override
    public final void onFailure(A action, T failure) {
        if (failures.putIfAbsent(action.id(), failure) == null) {
            LOGGER.warn("{} failed", action.id(), failure);
        }
    }

    @Override
    @Nullable
    public final T getFailure(A action) {
        return failures.get(action.id());
    }
}

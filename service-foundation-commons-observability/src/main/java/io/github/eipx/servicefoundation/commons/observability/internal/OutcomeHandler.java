package io.github.eipx.servicefoundation.commons.observability.internal;

import org.springframework.lang.Nullable;

public interface OutcomeHandler<A extends OutcomeHandler.Action, T extends Throwable> {

    void onSuccess(A action);

    void onFailure(A action, T failure);

    @Nullable
    T getFailure(A action);

    interface Action {
        String id();
    }
}

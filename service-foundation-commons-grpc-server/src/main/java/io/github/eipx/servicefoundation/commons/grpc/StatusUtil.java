package io.github.eipx.servicefoundation.commons.grpc;

import org.springframework.lang.Nullable;

import io.grpc.Status;
import io.grpc.StatusException;
import io.grpc.StatusRuntimeException;

public final class StatusUtil {

    @Nullable
    public static Status fromFailure(Throwable throwable) {
        if (throwable instanceof StatusException statusException) {
            return statusException.getStatus();
        }
        if (throwable instanceof StatusRuntimeException statusRuntimeException) {
            return statusRuntimeException.getStatus();
        }
        return null;
    }

    private StatusUtil() {
    }
}

package io.github.eipx.servicefoundation.commons.grpc;

import java.util.function.Consumer;

import io.grpc.stub.StreamObserver;

/** Stream observer enforcing the single-response contract of a unary call. */
public abstract class UnaryCallStreamObserver<T> implements StreamObserver<T> {

    private T value;

    @Override
    public final void onNext(T value) {
        if (value == null) {
            throw new IllegalArgumentException("value cannot be null");
        }
        if (this.value == null) {
            this.value = value;
            return;
        }
        throw new IllegalStateException(
                "Cannot assign already assigned value [" + this.value + "] to other value [" + value + "] for unary call");
    }

    @Override
    public final void onCompleted() {
        if (value == null) {
            throw new IllegalStateException("Completion happened while no response value was received");
        }
        try {
            onGrpcSuccess(value);
        } catch (Throwable throwable) {
            onCallbackFailure(throwable);
        }
    }

    @Override
    public final void onError(Throwable throwable) {
        try {
            onGrpcFailure(throwable);
        } catch (Throwable callbackFailure) {
            onCallbackFailure(callbackFailure);
        }
    }

    protected abstract void onGrpcSuccess(T value);

    protected abstract void onGrpcFailure(Throwable throwable);

    protected abstract void onCallbackFailure(Throwable throwable);

    public static final class SimpleUnaryCallStreamObserver<T> extends UnaryCallStreamObserver<T> {

        private final Consumer<T> onGrpcSuccess;
        private final Consumer<Throwable> onGrpcFailure;
        private final Consumer<Throwable> onCallbackFailure;

        public SimpleUnaryCallStreamObserver(
                Consumer<T> onGrpcSuccess,
                Consumer<Throwable> onGrpcFailure,
                Consumer<Throwable> onCallbackFailure) {
            this.onGrpcSuccess = onGrpcSuccess;
            this.onGrpcFailure = onGrpcFailure;
            this.onCallbackFailure = onCallbackFailure;
        }

        @Override
        protected void onGrpcSuccess(T value) {
            onGrpcSuccess.accept(value);
        }

        @Override
        protected void onGrpcFailure(Throwable throwable) {
            onGrpcFailure.accept(throwable);
        }

        @Override
        protected void onCallbackFailure(Throwable throwable) {
            onCallbackFailure.accept(throwable);
        }
    }
}

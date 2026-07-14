package io.github.eipx.servicefoundation.commons.grpc.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Stopwatch;

import io.grpc.ForwardingServerCall;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.Status;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;

import static java.util.Collections.singleton;
import static java.util.Objects.requireNonNull;

public final class MetricsGlobalServerInterceptor implements GlobalServerInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(MetricsGlobalServerInterceptor.class);

    private final MeterRegistry meterRegistry;

    public MetricsGlobalServerInterceptor(MeterRegistry meterRegistry) {
        this.meterRegistry = requireNonNull(meterRegistry, "meterRegistry");
    }

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        ServerCall<ReqT, RespT> serverCallWithTimer =
                new ServerCallWithTimer<>(call, meterRegistry, call.getMethodDescriptor(), stopwatch);
        return next.startCall(serverCallWithTimer, headers);
    }

    private static final class ServerCallWithTimer<ReqT, RespT>
            extends ForwardingServerCall.SimpleForwardingServerCall<ReqT, RespT> {

        private final MeterRegistry meterRegistry;
        private final MethodDescriptor<ReqT, RespT> method;
        private final Stopwatch stopwatch;

        private ServerCallWithTimer(
                ServerCall<ReqT, RespT> delegate,
                MeterRegistry meterRegistry,
                MethodDescriptor<ReqT, RespT> method,
                Stopwatch stopwatch) {
            super(delegate);
            this.meterRegistry = meterRegistry;
            this.method = method;
            this.stopwatch = stopwatch;
        }

        @Override
        public void close(Status status, Metadata trailers) {
            try {
                timer(status).record(stopwatch.elapsed());
            } catch (Exception exception) {
                LOGGER.warn("Cannot compute metrics for grpc call [{}]. Ignoring error.", method, exception);
            } finally {
                super.close(status, trailers);
            }
        }

        private Timer timer(Status status) {
            Tag tag = Tag.of("status", StatusTagValue.fromStatus(status).value);
            return meterRegistry.timer("grpc.server." + method.getFullMethodName(), singleton(tag));
        }
    }

    private enum StatusTagValue {
        OK("ok"),
        DEADLINE_EXCEEDED("deadlineExceeded"),
        FAILURE("failure");

        private final String value;

        StatusTagValue(String value) {
            this.value = value;
        }

        private static StatusTagValue fromStatus(Status status) {
            if (status.isOk()) {
                return OK;
            }
            if (Status.Code.DEADLINE_EXCEEDED.equals(status.getCode())) {
                return DEADLINE_EXCEEDED;
            }
            return FAILURE;
        }
    }
}

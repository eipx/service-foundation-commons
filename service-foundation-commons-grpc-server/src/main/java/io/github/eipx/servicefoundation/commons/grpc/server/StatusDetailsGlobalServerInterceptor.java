package io.github.eipx.servicefoundation.commons.grpc.server;

import io.github.eipx.servicefoundation.commons.grpc.TrailerUtil;

import io.grpc.ForwardingServerCall;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.Status;

/** Adds thread-local status details to successful gRPC responses. */
public class StatusDetailsGlobalServerInterceptor implements GlobalServerInterceptor {

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {
        return next.startCall(
                new ForwardingServerCall.SimpleForwardingServerCall<ReqT, RespT>(call) {
                    @Override
                    public void close(Status status, Metadata trailers) {
                        if (status.isOk()) {
                            trailers.merge(TrailerUtil.getMetadata());
                        }
                        super.close(status, trailers);
                    }
                },
                headers);
    }
}

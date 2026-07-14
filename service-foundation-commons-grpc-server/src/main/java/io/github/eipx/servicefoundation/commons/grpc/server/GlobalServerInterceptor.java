package io.github.eipx.servicefoundation.commons.grpc.server;

import io.grpc.ServerInterceptor;

/** Marker interface for interceptors applied to every discovered service. */
public interface GlobalServerInterceptor extends ServerInterceptor {
}

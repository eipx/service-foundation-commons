package io.github.eipx.servicefoundation.commons.grpc.server;

import org.springframework.lang.Nullable;

import io.grpc.Server;

public interface GrpcServerFactory {

    @Nullable
    Server createServer();

    String getAddress();

    int getPort();
}

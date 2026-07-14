package io.github.eipx.servicefoundation.commons.grpc.server;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.eipx.servicefoundation.commons.core.lifecycle.SimpleSmartLifecycle;

import io.grpc.Server;

public class GrpcServerLifecycle extends SimpleSmartLifecycle {

    private static final Logger LOGGER = LoggerFactory.getLogger(GrpcServerLifecycle.class);
    private static final AtomicInteger SERVER_COUNTER = new AtomicInteger(-1);

    private final GrpcServerFactory factory;
    private volatile Server server;

    public GrpcServerLifecycle(GrpcServerFactory factory) {
        super(MIDDLE_STARTUP_MIDDLE_SHUTDOWN_PHASE);
        this.factory = factory;
    }

    @Override
    protected void doStart() {
        Server newServer = factory.createServer();
        if (newServer == null) {
            LOGGER.info("No service registered on gRPC server, server will not be started");
            return;
        }
        try {
            newServer.start();
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Failed to start gRPC server on " + factory.getAddress() + ":" + factory.getPort(),
                    exception);
        }

        Thread awaitThread = new Thread("container-" + SERVER_COUNTER.incrementAndGet()) {
            @Override
            public void run() {
                try {
                    newServer.awaitTermination();
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                }
            }
        };
        awaitThread.setDaemon(false);
        awaitThread.start();
        server = newServer;
        LOGGER.info("gRPC server started on {}:{}", factory.getAddress(), factory.getPort());
    }

    @Override
    protected void doShutdown() {
        if (server == null) {
            return;
        }
        LOGGER.info("Stopping gRPC server on {}:{}", factory.getAddress(), factory.getPort());
        server.shutdown();
        try {
            if (!server.awaitTermination(10, TimeUnit.SECONDS)) {
                server.shutdownNow();
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            server.shutdownNow();
        }
        server = null;
    }
}

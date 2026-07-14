package io.github.eipx.servicefoundation.commons.grpc.server;

import io.grpc.ServerServiceDefinition;

public class GrpcServerServiceDefinition {

    private final String beanName;
    private final Class<?> beanClass;
    private final ServerServiceDefinition definition;

    public GrpcServerServiceDefinition(
            String beanName,
            Class<?> beanClass,
            ServerServiceDefinition definition) {
        this.beanName = beanName;
        this.beanClass = beanClass;
        this.definition = definition;
    }

    public String getBeanName() {
        return beanName;
    }

    public Class<?> getBeanClass() {
        return beanClass;
    }

    public ServerServiceDefinition getDefinition() {
        return definition;
    }
}

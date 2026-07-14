package io.github.eipx.servicefoundation.commons.grpc.server;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import jakarta.annotation.PostConstruct;
import io.grpc.ServerInterceptor;

public class GlobalServerInterceptorRegistry implements ApplicationContextAware {

    private final List<ServerInterceptor> serverInterceptors = new ArrayList<>();
    private ApplicationContext applicationContext;

    @PostConstruct
    public void init() {
        applicationContext.getBeansOfType(GlobalServerInterceptor.class)
                .values()
                .forEach(serverInterceptors::add);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    List<ServerInterceptor> getServerInterceptors() {
        return serverInterceptors;
    }
}

package io.github.eipx.servicefoundation.commons.grpc.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import io.grpc.BindableService;
import io.grpc.ServerInterceptor;
import io.grpc.ServerInterceptors;
import io.grpc.ServerServiceDefinition;

import static java.util.stream.Collectors.toList;

public class AnnotationGrpcServiceDiscoverer implements ApplicationContextAware, GrpcServiceDiscoverer {

    private static final Logger LOGGER = LoggerFactory.getLogger(AnnotationGrpcServiceDiscoverer.class);

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public Collection<GrpcServerServiceDefinition> findGrpcServices() {
        List<String> beanNames = Arrays.asList(
                applicationContext.getBeanNamesForAnnotation(GrpcServerService.class));
        return beanNames.stream()
                .map(beanName -> {
                    BindableService bindableService = applicationContext.getBean(beanName, BindableService.class);
                    ServerServiceDefinition definition = bindableService.bindService();
                    GrpcServerService grpcService =
                            applicationContext.findAnnotationOnBean(beanName, GrpcServerService.class);
                    ServerServiceDefinition wrapped =
                            ServerInterceptors.intercept(definition, resolveAllInterceptors(grpcService));
                    LOGGER.info(
                            "Found gRPC service: {}, bean: {}, class: {}",
                            wrapped.getServiceDescriptor().getName(),
                            beanName,
                            bindableService.getClass().getName());
                    return new GrpcServerServiceDefinition(beanName, bindableService.getClass(), wrapped);
                })
                .collect(toList());
    }

    @SuppressWarnings("deprecation")
    private List<ServerInterceptor> resolveAllInterceptors(GrpcServerService grpcService) {
        GlobalServerInterceptorRegistry registry =
                applicationContext.getBean(GlobalServerInterceptorRegistry.class);
        List<ServerInterceptor> globalInterceptors = registry.getServerInterceptors();
        Set<ServerInterceptor> interceptorSet = new HashSet<>(globalInterceptors);
        Arrays.stream(grpcService.interceptors())
                .map(interceptorClass -> {
                    try {
                        return applicationContext.getBeanNamesForType(interceptorClass).length > 0
                                ? applicationContext.getBean(interceptorClass)
                                : interceptorClass.newInstance();
                    } catch (Exception exception) {
                        throw new BeanCreationException(
                                "Failed to create server interceptor instance", exception);
                    }
                })
                .forEach(interceptorSet::add);
        return new ArrayList<>(interceptorSet);
    }
}

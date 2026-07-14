package io.github.eipx.servicefoundation.commons.grpc.server;

import org.springframework.boot.actuate.autoconfigure.tracing.BraveAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.lang.Nullable;

import brave.Tracing;
import brave.grpc.GrpcTracing;

@AutoConfiguration
@EnableConfigurationProperties
@AutoConfigureAfter({GrpcServerMetricsAutoConfiguration.class, BraveAutoConfiguration.class})
public class GrpcServerAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public GrpcServerProperties defaultGrpcServerProperties() {
        return new GrpcServerProperties();
    }

    @Bean
    public StatusDetailsGlobalServerInterceptor statusDetailsGlobalServerInterceptor() {
        return new StatusDetailsGlobalServerInterceptor();
    }

    @Bean
    public GlobalServerInterceptorRegistry globalServerInterceptorRegistry() {
        return new GlobalServerInterceptorRegistry();
    }

    @Bean
    @ConditionalOnMissingBean
    public AnnotationGrpcServiceDiscoverer defaultGrpcServiceDiscoverer() {
        return new AnnotationGrpcServiceDiscoverer();
    }

    @Bean
    @ConditionalOnMissingBean
    public NettyGrpcServerFactory nettyGrpcServerFactory(
            GrpcServerProperties properties,
            GrpcServiceDiscoverer discoverer,
            @Nullable GrpcTracing grpcTracing) {
        return new NettyGrpcServerFactory(properties, discoverer.findGrpcServices(), grpcTracing);
    }

    @Bean
    @ConditionalOnMissingBean
    public GrpcServerLifecycle grpcServerLifecycle(GrpcServerFactory factory) {
        return new GrpcServerLifecycle(factory);
    }

    @Bean
    @ConditionalOnBean(Tracing.class)
    @ConditionalOnMissingBean
    public GrpcTracing grpcTracing(Tracing tracing) {
        return GrpcTracing.create(tracing);
    }
}

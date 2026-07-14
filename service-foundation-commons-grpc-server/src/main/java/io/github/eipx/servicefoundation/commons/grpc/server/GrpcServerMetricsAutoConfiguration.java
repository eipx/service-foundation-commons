package io.github.eipx.servicefoundation.commons.grpc.server;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.actuate.autoconfigure.metrics.MetricsAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.metrics.export.simple.SimpleMetricsExportAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;

import io.micrometer.core.instrument.MeterRegistry;

@AutoConfiguration
@AutoConfigureAfter(
        value = {MetricsAutoConfiguration.class, SimpleMetricsExportAutoConfiguration.class},
        name = {
            "io.github.eipx.servicefoundation.commons.observability.MetricsAutoConfiguration",
            "io.github.eipx.servicefoundation.commons.observability.metrics.MetricsAutoConfiguration"
        })
@ConditionalOnClass(MeterRegistry.class)
@ConditionalOnBean(MeterRegistry.class)
public class GrpcServerMetricsAutoConfiguration {

    @Bean
    public MetricsGlobalServerInterceptor metricsGlobalServerInterceptor(MeterRegistry meterRegistry) {
        return new MetricsGlobalServerInterceptor(meterRegistry);
    }

    @Bean("forceMeterRegistryPostProcessor2")
    public InitializingBean forceMeterRegistryPostProcessor(
            @Qualifier("meterRegistryPostProcessor") BeanPostProcessor meterRegistryPostProcessor,
            MeterRegistry meterRegistry) {
        return () -> meterRegistryPostProcessor.postProcessAfterInitialization(meterRegistry, "");
    }
}

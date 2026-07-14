package io.github.eipx.servicefoundation.commons.observability.metrics;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;

import io.micrometer.core.instrument.MeterRegistry;

@AutoConfiguration
@AutoConfigureAfter({
        org.springframework.boot.actuate.autoconfigure.metrics.MetricsAutoConfiguration.class,
        MetricsAutoConfiguration.class
})
@ConditionalOnClass({MeterRegistry.class})
@ConditionalOnBean({MeterRegistry.class})
public class SystemMetricsAutoConfiguration {

    @Bean
    public SystemMetrics systemMetrics() {
        // See org.springframework.boot.actuate.autoconfigure.metrics.MetricsAutoConfiguration.meterRegistryPostProcessor()
        return new SystemMetrics();
    }
}

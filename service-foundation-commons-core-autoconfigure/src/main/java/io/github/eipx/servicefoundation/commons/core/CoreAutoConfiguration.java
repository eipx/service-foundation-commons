package io.github.eipx.servicefoundation.commons.core;

import io.github.eipx.servicefoundation.commons.core.lifecycle.FirstPhaseLifecycle;
import io.github.eipx.servicefoundation.commons.core.lifecycle.LastPhaseLifecycle;
import org.springframework.boot.actuate.autoconfigure.health.HealthEndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.health.HealthEndpointProperties;
import org.springframework.boot.actuate.health.SimpleStatusAggregator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.actuate.health.StatusAggregator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@EnableConfigurationProperties(CoreProperties.class)
@AutoConfigureBefore(HealthEndpointAutoConfiguration.class)
public class CoreAutoConfiguration {

    @Bean
    public StatusAggregator statusAggregator(HealthEndpointProperties healthProperties) {
        if (!healthProperties.getStatus().getOrder().isEmpty()) {
            return new SimpleStatusAggregator(healthProperties.getStatus().getOrder());
        }
        return new SimpleStatusAggregator(ApplicationStatus.DEFAULT_ORDER.toArray(new Status[0]));
    }

    @Bean
    @ConditionalOnMissingBean
    public ApplicationStatus applicationStatus() {
        return new ApplicationStatus();
    }

    @Bean
    @ConditionalOnMissingBean
    public ApplicationHealthIndicator applicationHealthIndicator(ApplicationStatus applicationStatus) {
        return new ApplicationHealthIndicator(applicationStatus);
    }

    @Bean
    @ConditionalOnMissingBean
    public FirstPhaseLifecycle firstPhaseLifecycle(ApplicationStatus applicationStatus) {
        return new FirstPhaseLifecycle(applicationStatus);
    }

    @Bean
    @ConditionalOnMissingBean
    public LastPhaseLifecycle lastPhaseLifecycle(ApplicationStatus applicationStatus) {
        return new LastPhaseLifecycle(applicationStatus);
    }
}

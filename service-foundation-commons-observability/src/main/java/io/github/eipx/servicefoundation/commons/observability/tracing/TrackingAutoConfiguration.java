package io.github.eipx.servicefoundation.commons.observability.tracing;

import org.springframework.boot.actuate.autoconfigure.tracing.zipkin.ZipkinAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import zipkin2.Span;
import zipkin2.reporter.Reporter;

@AutoConfiguration
@AutoConfigureBefore(ZipkinAutoConfiguration.class)
@ConditionalOnClass({brave.Tracing.class, Reporter.class})
@EnableConfigurationProperties(TrackingProperties.class)
public class TrackingAutoConfiguration {

    @Bean(name = "spanReporter")
    @ConditionalOnMissingBean(name = "spanReporter")
    @ConditionalOnProperty(name = "tracking.enabled", havingValue = "false", matchIfMissing = true)
    public Reporter<Span> spanReporter() {
        return new Reporter<>() {
            @Override
            public void report(Span span) {
                // Intentionally discard spans while retaining Brave/Micrometer context propagation.
            }

            @Override
            public String toString() {
                return "NoopReporter{}";
            }
        };
    }
}

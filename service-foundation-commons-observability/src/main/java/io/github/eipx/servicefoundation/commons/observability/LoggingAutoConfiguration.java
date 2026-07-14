package io.github.eipx.servicefoundation.commons.observability;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.logging.LoggingSystem;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

import io.github.eipx.servicefoundation.commons.observability.activitylog.ActivityLog;
import io.github.eipx.servicefoundation.commons.observability.activitylog.DefaultActivityLog;
import io.github.eipx.servicefoundation.commons.observability.event.DefaultEventJournal;
import io.github.eipx.servicefoundation.commons.observability.event.EventJournal;

@AutoConfiguration
public class LoggingAutoConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingAutoConfiguration.class);

    @Bean(name = "eventJournal")
    @ConditionalOnMissingBean(name = "eventJournal")
    public EventJournal eventJournal(Environment environment) {
        String productName = environment.getProperty("product-name", "Unknown");
        return new DefaultEventJournal(
                environment.getProperty("company-name", "SAMPLE"),
                productName,
                environment.getProperty("product-version", "Unknown"),
                0,
                environment.getProperty("spring.application.name", "unknown"),
                localAddress(),
                environment.getProperty("service-instance-id"),
                Calendar.getInstance().getTimeZone(),
                environment.getProperty("app-instance-uuid", "UNDEFINED"),
                environment.getProperty("app-instance-id", "UNDEFINED"),
                productName + " ID");
    }

    @Bean
    @ConditionalOnMissingBean(ActivityLog.class)
    public ActivityLog activityLog() {
        return new DefaultActivityLog();
    }

    @Bean
    @ConditionalOnClass(LoggingSystem.class)
    @ConditionalOnMissingBean
    public LoggingEndPoint loggingEndPoint(LoggingSystem loggingSystem) {
        return new LoggingEndPoint(loggingSystem);
    }

    private static String localAddress() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException failure) {
            LOGGER.warn("Unable to resolve the local host address; event journal source defaults to 0.0.0.0", failure);
            return "0.0.0.0";
        }
    }
}

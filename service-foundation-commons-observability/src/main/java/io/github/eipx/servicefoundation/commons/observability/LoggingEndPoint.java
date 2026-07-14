package io.github.eipx.servicefoundation.commons.observability;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.springframework.boot.actuate.endpoint.web.annotation.RestControllerEndpoint;
import org.springframework.boot.logging.LogLevel;
import org.springframework.boot.logging.LoggerConfiguration;
import org.springframework.boot.logging.LoggingSystem;
import org.springframework.context.SmartLifecycle;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import io.github.eipx.servicefoundation.commons.observability.internal.ExecutorSupport;

import static java.util.Objects.*;

import static org.slf4j.LoggerFactory.*;

@RestControllerEndpoint(id = "logging")
public class LoggingEndPoint implements SmartLifecycle {

    private static final Logger LOGGER = getLogger(LoggingEndPoint.class);

    private final LoggingSystem loggingSystem;
    private final Map<String, Optional<LogLevel>> startupLogLevels;
    private ScheduledExecutorService scheduledExecutorService;
    private volatile boolean isRunning = false;

    public LoggingEndPoint(LoggingSystem loggingSystem) {
        this.loggingSystem = requireNonNull(loggingSystem, "loggingSystem");
        this.startupLogLevels = new ConcurrentHashMap<>();
    }

    @Override
    public void start() {
        scheduledExecutorService = ExecutorSupport.newSingleThreadScheduler(
                "LoggingEndPoint", t -> LOGGER.error("Unexpected error", t));
        isRunning = true;
    }

    @Override
    public void stop() {
        ExecutorSupport.shutdown(scheduledExecutorService, 0, TimeUnit.SECONDS);
        isRunning = false;
    }

    @Override
    public boolean isRunning() {
        return isRunning;
    }

    @RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, Object> loggers() {
        Collection<LoggerConfiguration> configurations = loggingSystem.getLoggerConfigurations();
        if (configurations == null) {
            return Collections.emptyMap();
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("levels", getLevels());
        result.put("loggers", getLoggers(configurations));
        return result;
    }

    @RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE, path = "/{name}")
    @ResponseBody
    public LoggerLevels loggerLevels(@PathVariable String name) {
        Assert.notNull(name, "Name must not be null");
        LoggerConfiguration configuration = loggingSystem.getLoggerConfiguration(name);
        return (configuration == null) ? null : new LoggerLevels(configuration);
    }

    @PostMapping
    public ResponseEntity<?> configureLogLevel(@RequestParam(required = false, defaultValue = "io.github.eipx") String loggers,
                                               @RequestParam int duration,
                                               @RequestParam LogLevel level,
                                               @RequestBody(required = false) String body) {
        Assert.notNull(loggers, "List of loggers must not be empty");
        synchronized (startupLogLevels) {
            if (!startupLogLevels.isEmpty()) {
                return ResponseEntity.status(HttpStatus.CONFLICT).contentType(MediaType.TEXT_PLAIN).body("Detailed logging already activated");
            }
            for (String pkg : loggers.split(",")) {
                LoggerConfiguration loggerConfiguration = loggingSystem.getLoggerConfiguration(pkg);
                startupLogLevels.put(pkg, loggerConfiguration != null ? Optional.of(loggerConfiguration.getEffectiveLevel()) : Optional.empty());
                loggingSystem.setLogLevel(pkg, level);
            }
            scheduledExecutorService.schedule((Callable<ResponseEntity<?>>) () -> resetLogLevel(null), duration, TimeUnit.SECONDS);
        }
        return ResponseEntity.ok(null);
    }

    @PostMapping(path = "/reset")
    public ResponseEntity<?> resetLogLevel(@RequestBody(required = false) String body) {
        synchronized (startupLogLevels) {
            if (startupLogLevels.isEmpty()) {
                return ResponseEntity.ok(null);
            }
            startupLogLevels.entrySet().stream().forEach(entry -> loggingSystem.setLogLevel(entry.getKey(), entry.getValue().orElse(null)));
            startupLogLevels.clear();
        }
        return ResponseEntity.ok(null);
    }

    private NavigableSet<LogLevel> getLevels() {
        Set<LogLevel> levels = loggingSystem.getSupportedLogLevels();
        return new TreeSet<>(levels).descendingSet();
    }

    private Map<String, LoggerLevels> getLoggers(
            Collection<LoggerConfiguration> configurations) {
        Map<String, LoggerLevels> loggers = new LinkedHashMap<>(configurations.size());
        for (LoggerConfiguration configuration : configurations) {
            loggers.put(configuration.getName(), new LoggerLevels(configuration));
        }
        return loggers;
    }

    @Override
    public boolean isAutoStartup() {
        return true;
    }

    @Override
    public void stop(Runnable callback) {
        stop();
        callback.run();
    }

    @Override
    public int getPhase() {
        return Integer.MIN_VALUE;
    }

    /**
     * Levels configured for a given logger exposed in a JSON friendly way.
     */
    private static class LoggerLevels {

        private final String configuredLevel;
        private final String effectiveLevel;

        LoggerLevels(LoggerConfiguration configuration) {
            this.configuredLevel = getName(configuration.getConfiguredLevel());
            this.effectiveLevel = getName(configuration.getEffectiveLevel());
        }

        private String getName(LogLevel level) {
            return level == null ? null : level.name();
        }

        public String getConfiguredLevel() {
            return this.configuredLevel;
        }

        public String getEffectiveLevel() {
            return this.effectiveLevel;
        }
    }
}

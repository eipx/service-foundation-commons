package io.github.eipx.servicefoundation.commons.observability.event;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.TimeZone;
import java.util.function.Supplier;
import java.util.regex.Matcher;

import org.springframework.lang.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import io.github.eipx.servicefoundation.commons.observability.event.Event.Severity;
import io.github.eipx.servicefoundation.commons.observability.cef.CEFEvent;
import io.github.eipx.servicefoundation.commons.observability.cef.CEFEventBuilder;
import io.github.eipx.servicefoundation.commons.observability.cef.CEFEventSerializer;

/**
 * Default implementation of {@link EventJournal}, based on a slf4j {@link Logger}.
 * Events are logged using a logger named {@link #EVENT_JOURNAL_LOGGER_NAME}
 */
public class DefaultEventJournal implements EventJournal {
    private static final DateTimeFormatter UTC_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss.SSS'Z'");
    private static final DateTimeFormatter LOCAL_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss.SSS");
    @VisibleForTesting
    static final String EVENT_JOURNAL_LOGGER_NAME = "journal";

    private final CEFEventBuilder cefEventBuilder;
    private final String processName;
    private final CEFEventSerializer cefEventSerializer = new CEFEventSerializer();

    public DefaultEventJournal(String deviceVendor, String deviceProduct, String deviceVersion, int cefVersion, String microservice,
                               String ip, String serviceInstanceId, TimeZone timeZone, String instanceUUID, String applicationId, String applicationIdLabel) {
        this.processName = microservice + "-" + serviceInstanceId;
        this.cefEventBuilder = new CEFEventBuilder().withDeviceProcessName(processName)
                                                    .withDeviceProduct(deviceProduct)
                                                    .withDeviceVendor(deviceVendor)
                                                    .withDeviceVersion(deviceVersion)
                                                    .withVersion(cefVersion)
                                                    .withSource(ip)
                                                    .withCs3label("UTCtime")
                                                    .withCs4label("Localtime")
                                                    .withCs2(applicationId)
                                                    .withCs2label(applicationIdLabel)
                                                    .withCs1label("Instance UUID")
                                                    .withCs1(instanceUUID)
                                                    .withDtz(timeZone.getID());
    }

    /**
     * Logger to be used by the EventJournal appender
     */
    private static final Logger JOURNAL = LoggerFactory.getLogger(EVENT_JOURNAL_LOGGER_NAME);
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultEventJournal.class);

    @Override
    public void log(Event event, String description, Object... descriptionParamsAndEx) {
        CEFEvent cefEvent = createCefEvent(event, description, descriptionParamsAndEx);
        JOURNAL.info(cefEventSerializer.serialize(cefEvent));
        logApplication(event.severity(), description, descriptionParamsAndEx);
    }

    @Override
    public CEFEvent createCefEvent(Event event, String description, Object[] descriptionParamsAndEx) {
        Object[] params = extractParameters(description, descriptionParamsAndEx);
        String descriptionCompleted = description;
        for (Object param : params) {
            String paramString = "null";
            if (param != null) {
                paramString = param.toString();
            }
            descriptionCompleted = descriptionCompleted.replaceFirst("\\{\\}", Matcher.quoteReplacement(paramString));
        }

        CEFEvent.Severity severity = EventToCEFMapper.getSeverity(event.severity());
        Instant now = Instant.now();
        return cefEventBuilder.withCategory(event.category().name())
                              .withCode(event.code())
                              .withSeverity(severity)
                              .withName(event.title())
                              .withComponent(event.component())
                              .withMessage(descriptionCompleted)
                              .withCs3(UTC_DATE_TIME_FORMATTER.format(LocalDateTime.ofInstant(now, ZoneOffset.UTC)))
                              .withCs4(LOCAL_DATE_TIME_FORMATTER.format(LocalDateTime.ofInstant(now, ZoneId.systemDefault())))
                              .withRt(now.toEpochMilli())
                              .build();
    }

    @Override
    public void log(Supplier<Event> eventSupplier, String description, Object... descriptionParamsAndEx) {
        log(eventSupplier.get(), description, descriptionParamsAndEx);
    }

    @Override
    public void log(Event event, Throwable throwable) {
        log(event, throwable.getMessage(), throwable);
    }

    @Override
    public void log(Supplier<Event> eventSupplier, Throwable throwable) {
        log(eventSupplier.get(), throwable);
    }

    private void logApplication(Severity severity, String description, Object[] descriptionParamsAndEx) {
        switch (severity) {
            case INFO:
                LOGGER.info(description, descriptionParamsAndEx);
                break;
            case WARNING:
                LOGGER.warn(description, descriptionParamsAndEx);
                break;
            case ERROR:
            case FATAL:
                LOGGER.error(description, descriptionParamsAndEx);
                break;
            default:
                throw new AssertionError("Unknown severity: " + severity);
        }
    }

    private Object[] extractParameters(@Nullable String messageTemplate, Object[] paramsAndEx) {
        int placeholderCount = StringUtils.countMatches(messageTemplate, "{}");
        int paramCount = paramsAndEx.length;
        if (paramCount == placeholderCount) {
            return paramsAndEx;
        }
        int lastParamIndex = paramCount - 1;
        return Arrays.copyOf(paramsAndEx, lastParamIndex);
    }

}

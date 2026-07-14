package io.github.eipx.servicefoundation.commons.observability.event;


import java.util.function.Supplier;

import io.github.eipx.servicefoundation.commons.observability.cef.CEFEvent;

/**
 * Interface to log events in the event journal.
 */
public interface EventJournal {

    /**
     * Logs an event, with an optional description and exception in the Event Journal and in the standard log.
     * <p/>
     * The syntax is slf4j-like, an exception ({@link Throwable}) can be provided as the last item
     * of the description parameters ({@code descriptionParamsAndEx}).
     * <p/>
     * In the Event Journal, the event description is generated as follows:
     * <ul>
     * <li>
     * If a {@code description} is provided: build a description
     * from the {@code description} and the given {@code descriptionParamsAndEx}.
     * </li>
     * <li>
     * If an exception is provided as the last item of {@code descriptionParamsAndEx}
     * and no {@code description} was given, use the exception message.
     * </li>
     * </ul>
     * In the standard log, the {@code description} and its {@code descriptionParamsAndEx} are used to generate
     * the log message and the full stacktrace of the optional exception is printed out.
     * <p/>
     * Note: Do not invoke log like this...
     * <pre>
     *     log(..., ..., exception.getMessage(), exception);
     * </pre>
     * ... as it would end up by printing the exception message twice
     *
     * @param event                  the event code to be used in the Event Journal.
     * @param description            the details about the event, using slf4j {} placeholders
     * @param descriptionParamsAndEx array of parameters to fill the placeholders of the {@code description}
     *                               and an optional {@link Throwable} as last item (slf4j-like).<br/>
     *                               For convenience, a {@code null} value is accepted for this optional
     *                               {@link Throwable}, in which case it is simply ignored.
     */
    void log(Event event, String description, Object... descriptionParamsAndEx);

    /***
     * Create a CEF event from an application event
     * @param event event
     * @param description description
     * @param descriptionParamsAndEx optional parameters
     * @return cef event
     */
    CEFEvent createCefEvent(Event event, String description, Object... descriptionParamsAndEx);

    /**
     * Logs an event, with an optional description and exception in the Event Journal and in the standard log.
     * <p/>
     * The syntax is slf4j-like, an exception ({@link Throwable}) can be provided as the last item
     * of the description parameters ({@code descriptionParamsAndEx}).
     * <p/>
     * In the Event Journal, the event description is generated as follows:
     * <ul>
     * <li>
     * If a {@code description} is provided: build a description
     * from the {@code description} and the given {@code descriptionParamsAndEx}.
     * </li>
     * <li>
     * If an exception is provided as the last item of {@code descriptionParamsAndEx}
     * and no {@code description} was given, use the exception message.
     * </li>
     * </ul>
     * In the standard log, the {@code description} and its {@code descriptionParamsAndEx} are used to generate
     * the log message and the full stacktrace of the optional exception is printed out.
     * <p/>
     * Note: Do not invoke log like this...
     * <pre>
     *     log(..., ..., exception.getMessage(), exception);
     * </pre>
     * ... as it would end up by printing the exception message twice
     *
     * @param eventSupplier          a supplier for the event code to be used in the Event Journal.
     * @param description            the details about the event, using slf4j {} placeholders
     * @param descriptionParamsAndEx array of parameters to fill the placeholders of the {@code description}
     *                               and an optional {@link Throwable} as last item (slf4j-like).<br/>
     *                               For convenience, a {@code null} value is accepted for this optional
     *                               {@link Throwable}, in which case it is simply ignored.
     */
    void log(Supplier<Event> eventSupplier, String description, Object... descriptionParamsAndEx);


    /**
     * Logs an event with an exception in the Event Journal and in the standard log.
     * <p/>
     * The syntax is slf4j-like, an exception ({@link Throwable}) can be provided as the last item
     * of the message parameters ({@code messageParamsAndEx}).
     * <p/>
     * In the Event Journal, the event description (&lt;Text&gt; element) is built from the exception message.
     *
     * @param event     the event code to be used in the Event Journal.
     * @param throwable the exception linked to the event
     */
    void log(Event event, Throwable throwable);


    /**
     * Logs an event with an exception in the Event Journal and in the standard log.
     * <p/>
     * The syntax is slf4j-like, an exception ({@link Throwable}) can be provided as the last item
     * of the message parameters ({@code messageParamsAndEx}).
     * <p/>
     * In the Event Journal, the event description (&lt;Text&gt; element) is built from the exception message.
     *
     * @param eventSupplier a supplier for the event code to be used in the Event Journal.
     * @param throwable     the exception linked to the event
     */
    void log(Supplier<Event> eventSupplier, Throwable throwable);
}

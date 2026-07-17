package io.github.eipx.servicefoundation.commons.examples.testing;

import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Correlates a synchronous call with records appended to two container log streams.
 *
 * <p>Application-specific call types, log paths, and matching rules are supplied by the caller.
 * Commands are executed through an adapter so this example is independent of any container SDK.
 * The target container must provide {@code sh}, {@code stat}, {@code tail}, and
 * {@code /proc/self/fd}.
 */
public final class ContainerCallTrace<Q, R> {
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration DEFAULT_POLL_INTERVAL = Duration.ofMillis(25);

    private final String environmentName;
    private final CommandExecutor commands;
    private final Lock callLock;
    private final LogSpec<Q> primaryLog;
    private final LogSpec<Q> relatedLog;
    private final Duration timeout;
    private final Duration pollInterval;
    private final ThreadLocal<Observation<Q, R>> current = new ThreadLocal<>();

    public ContainerCallTrace(
            String environmentName,
            CommandExecutor commands,
            Lock callLock,
            LogSpec<Q> primaryLog,
            LogSpec<Q> relatedLog) {
        this(
                environmentName,
                commands,
                callLock,
                primaryLog,
                relatedLog,
                DEFAULT_TIMEOUT,
                DEFAULT_POLL_INTERVAL);
    }

    public ContainerCallTrace(
            String environmentName,
            CommandExecutor commands,
            Lock callLock,
            LogSpec<Q> primaryLog,
            LogSpec<Q> relatedLog,
            Duration timeout,
            Duration pollInterval) {
        this.environmentName = requireText(environmentName, "environmentName");
        this.commands = Objects.requireNonNull(commands, "commands");
        this.callLock = Objects.requireNonNull(callLock, "callLock");
        this.primaryLog = Objects.requireNonNull(primaryLog, "primaryLog");
        this.relatedLog = Objects.requireNonNull(relatedLog, "relatedLog");
        this.timeout = requirePositive(timeout, "timeout");
        this.pollInterval = requirePositive(pollInterval, "pollInterval");
    }

    public R invoke(Q request, Function<Q, R> delegate) {
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(delegate, "delegate");

        callLock.lock();
        try {
            clear();
            Snapshot before = snapshot();
            R response = delegate.apply(request);
            current.set(awaitObservation(request, response, before));
            return response;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw traceFailure(e);
        } catch (IOException e) {
            throw traceFailure(e);
        } finally {
            callLock.unlock();
        }
    }

    public void clear() {
        current.remove();
    }

    public String primaryRecordFor(Q request, R response) {
        return require(request, response).primaryRecord();
    }

    public String relatedTail(int lineCount) {
        if (lineCount <= 0) {
            throw new IllegalArgumentException("lineCount must be greater than zero");
        }

        Observation<Q, R> observation = requireCurrent();
        List<String> records = observation.relatedRecords();
        if (records.size() < lineCount) {
            throw new AssertionError("Expected at least " + lineCount + " matching "
                    + relatedLog.displayName() + " record(s) in " + environmentName
                    + ", but captured " + records.size());
        }
        return String.join(
                System.lineSeparator(),
                records.subList(records.size() - lineCount, records.size()));
    }

    @FunctionalInterface
    public interface CommandExecutor {
        CommandResult execute(String... command) throws IOException, InterruptedException;
    }

    public record CommandResult(int exitCode, String standardOutput, String standardError) {
        public CommandResult {
            standardOutput = Objects.requireNonNullElse(standardOutput, "");
            standardError = Objects.requireNonNullElse(standardError, "");
        }
    }

    public record LogSpec<T>(String displayName, String path, BiPredicate<T, String> matcher) {
        public LogSpec {
            displayName = requireText(displayName, "displayName");
            path = requireText(path, "path");
            matcher = Objects.requireNonNull(matcher, "matcher");
        }
    }

    private record Observation<T, U>(
            T request,
            U response,
            String primaryRecord,
            List<String> relatedRecords) {
        private Observation {
            relatedRecords = List.copyOf(relatedRecords);
        }
    }

    private record Snapshot(Cursor primary, Cursor related) {
    }

    private record Cursor(String fileIdentity, long byteOffset) {
    }

    private Snapshot snapshot() throws IOException, InterruptedException {
        return new Snapshot(cursor(primaryLog), cursor(relatedLog));
    }

    private Observation<Q, R> awaitObservation(Q request, R response, Snapshot before)
            throws IOException, InterruptedException {
        long deadline = System.nanoTime() + timeout.toNanos();
        List<String> primaryLines = Collections.emptyList();

        do {
            primaryLines = appendedLines(primaryLog, before.primary());
            String primaryRecord = primaryLines.stream()
                    .filter(line -> primaryLog.matcher().test(request, line))
                    .findFirst()
                    .orElse(null);
            if (primaryRecord != null) {
                List<String> relatedRecords = appendedLines(relatedLog, before.related()).stream()
                        .filter(line -> relatedLog.matcher().test(request, line))
                        .collect(Collectors.toList());
                return new Observation<>(request, response, primaryRecord, relatedRecords);
            }

            Thread.sleep(pollInterval.toMillis());
        } while (System.nanoTime() < deadline);

        throw new AssertionError("Timed out waiting for a matching " + primaryLog.displayName()
                + " record in " + environmentName + "; observed " + primaryLines.size()
                + " newly appended complete line(s)");
    }

    private Cursor cursor(LogSpec<Q> log) throws IOException, InterruptedException {
        CommandResult result = commands.execute(
                "sh", "-c",
                "if [ -f \"$1\" ]; then stat -c '%d:%i %s' \"$1\"; else printf 'missing 0'; fi",
                "capture-log-cursor", log.path());
        requireSuccess(result, "capture a cursor for", log);

        String[] fields = result.standardOutput().trim().split("\\s+");
        if (fields.length != 2) {
            throw new IOException("Unexpected cursor for " + log.displayName()
                    + " in " + environmentName + ": " + result.standardOutput());
        }
        try {
            return new Cursor(fields[0], Long.parseLong(fields[1]));
        } catch (NumberFormatException e) {
            throw new IOException("Invalid cursor for " + log.displayName()
                    + " in " + environmentName + ": " + result.standardOutput(), e);
        }
    }

    private List<String> appendedLines(LogSpec<Q> log, Cursor cursor)
            throws IOException, InterruptedException {
        CommandResult result = commands.execute(
                "sh", "-c",
                "if [ ! -f \"$1\" ]; then exit 0; fi; "
                        + "exec 3<\"$1\" || exit 0; "
                        + "metadata=$(stat -Lc '%d:%i %s' /proc/self/fd/3) || exit $?; "
                        + "identity=${metadata% *}; "
                        + "size=${metadata##* }; "
                        + "start=1; "
                        + "if [ \"$identity\" = \"$2\" ] && [ \"$size\" -ge \"$3\" ]; "
                        + "then start=$(( $3 + 1 )); fi; "
                        + "tail -c +\"$start\" <&3",
                "read-appended-log", log.path(), cursor.fileIdentity(), Long.toString(cursor.byteOffset()));
        requireSuccess(result, "read appended content from", log);

        String appended = result.standardOutput();
        int lastLineBreak = appended.lastIndexOf('\n');
        if (lastLineBreak < 0) {
            return Collections.emptyList();
        }
        return appended.substring(0, lastLineBreak + 1).lines().collect(Collectors.toList());
    }

    private void requireSuccess(CommandResult result, String action, LogSpec<Q> log) throws IOException {
        if (result.exitCode() != 0) {
            throw new IOException("Could not " + action + " " + log.displayName()
                    + " in " + environmentName + " (exit " + result.exitCode() + "): "
                    + result.standardError());
        }
    }

    private Observation<Q, R> require(Q request, R response) {
        Observation<Q, R> observation = requireCurrent();
        if (observation.request() != request) {
            throw new AssertionError("The primary record belongs to a different request");
        }
        if (observation.response() != response) {
            throw new AssertionError("The primary record belongs to a different response");
        }
        return observation;
    }

    private Observation<Q, R> requireCurrent() {
        Observation<Q, R> observation = current.get();
        if (observation == null) {
            throw new AssertionError("No completed call trace is available");
        }
        return observation;
    }

    private AssertionError traceFailure(Exception cause) {
        return new AssertionError("Could not trace a call in " + environmentName, cause);
    }

    private static Duration requirePositive(Duration value, String name) {
        Objects.requireNonNull(value, name);
        if (value.isZero() || value.isNegative()) {
            throw new IllegalArgumentException(name + " must be greater than zero");
        }
        if (value.toMillis() == 0) {
            throw new IllegalArgumentException(name + " must be at least one millisecond");
        }
        return value;
    }

    private static String requireText(String value, String name) {
        Objects.requireNonNull(value, name);
        if (value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}

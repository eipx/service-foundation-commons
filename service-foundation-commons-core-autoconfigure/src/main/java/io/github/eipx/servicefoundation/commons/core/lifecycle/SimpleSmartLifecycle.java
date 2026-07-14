package io.github.eipx.servicefoundation.commons.core.lifecycle;

import java.util.concurrent.atomic.AtomicReference;

import org.springframework.context.SmartLifecycle;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.locks.LockSupport.parkNanos;

/**
 * Idempotent lifecycle base preserving the established startup/shutdown phase behavior.
 */
public abstract class SimpleSmartLifecycle implements SmartLifecycle {

    private static final long SLEEP_NANOS = MILLISECONDS.toNanos(1);

    public static final int FIRST_STARTUP_LAST_SHUTDOWN_PHASE = Integer.MIN_VALUE;
    public static final int MIDDLE_STARTUP_MIDDLE_SHUTDOWN_PHASE = 0;
    public static final int LAST_STARTUP_FIRST_SHUTDOWN_PHASE = Integer.MAX_VALUE;

    public enum State {
        STOPPED,
        STARTING,
        STARTED,
        STOPPING
    }

    private final int phase;
    private final AtomicReference<State> state = new AtomicReference<>(State.STOPPED);

    protected SimpleSmartLifecycle(int phase) {
        this.phase = phase;
    }

    @Override
    public void start() {
        while (true) {
            if (state.compareAndSet(State.STOPPED, State.STARTING)) {
                boolean success = false;
                try {
                    doStart();
                    success = true;
                    return;
                } finally {
                    state.set(success ? State.STARTED : State.STOPPED);
                }
            }
            if (state.get() == State.STARTED) {
                return;
            }
            parkNanos(SLEEP_NANOS);
        }
    }

    @Override
    public void stop() {
        while (true) {
            if (state.compareAndSet(State.STARTED, State.STOPPING)) {
                try {
                    doShutdown();
                    return;
                } finally {
                    state.set(State.STOPPED);
                }
            }
            if (state.get() == State.STOPPED) {
                return;
            }
            parkNanos(SLEEP_NANOS);
        }
    }

    @Override
    public void stop(Runnable callback) {
        stop();
        callback.run();
    }

    @Override
    public boolean isRunning() {
        return state.get() == State.STARTED;
    }

    @Override
    public int getPhase() {
        return phase;
    }

    public State getState() {
        return state.get();
    }

    @Override
    public boolean isAutoStartup() {
        return true;
    }

    protected abstract void doStart();

    protected abstract void doShutdown();
}

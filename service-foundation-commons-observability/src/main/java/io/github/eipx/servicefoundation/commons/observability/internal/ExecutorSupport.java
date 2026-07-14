package io.github.eipx.servicefoundation.commons.observability.internal;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public final class ExecutorSupport {

    private ExecutorSupport() {
    }

    public static ScheduledExecutorService newSingleThreadScheduler(
            String threadNamePrefix, Consumer<Throwable> failureHandler) {
        Objects.requireNonNull(threadNamePrefix, "threadNamePrefix");
        Objects.requireNonNull(failureHandler, "failureHandler");
        ScheduledThreadPoolExecutor executor =
                new ScheduledThreadPoolExecutor(1, new NamedThreadFactory(threadNamePrefix, failureHandler)) {
                    @Override
                    public ScheduledFuture<?> scheduleAtFixedRate(
                            Runnable command, long initialDelay, long period, TimeUnit unit) {
                        return super.scheduleAtFixedRate(
                                resilient(command, failureHandler), initialDelay, period, unit);
                    }

                    @Override
                    public ScheduledFuture<?> scheduleWithFixedDelay(
                            Runnable command, long initialDelay, long delay, TimeUnit unit) {
                        return super.scheduleWithFixedDelay(
                                resilient(command, failureHandler), initialDelay, delay, unit);
                    }
                };
        executor.setRemoveOnCancelPolicy(true);
        executor.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
        executor.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
        return executor;
    }

    private static Runnable resilient(Runnable delegate, Consumer<Throwable> failureHandler) {
        return () -> {
            try {
                delegate.run();
            } catch (Throwable failure) {
                failureHandler.accept(failure);
            }
        };
    }

    public static void shutdown(ExecutorService executor, long timeout, TimeUnit unit) {
        if (executor == null) {
            return;
        }
        executor.shutdown();
        try {
            if (!executor.awaitTermination(timeout, unit)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException interrupted) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private static final class NamedThreadFactory implements ThreadFactory {

        private final String prefix;
        private final Consumer<Throwable> failureHandler;
        private final AtomicInteger counter = new AtomicInteger();

        private NamedThreadFactory(String prefix, Consumer<Throwable> failureHandler) {
            this.prefix = prefix;
            this.failureHandler = failureHandler;
        }

        @Override
        public Thread newThread(Runnable runnable) {
            int index = counter.incrementAndGet();
            Thread thread = new Thread(runnable, index == 1 ? prefix : prefix + "_" + index);
            thread.setUncaughtExceptionHandler((ignored, failure) -> failureHandler.accept(failure));
            return thread;
        }
    }
}

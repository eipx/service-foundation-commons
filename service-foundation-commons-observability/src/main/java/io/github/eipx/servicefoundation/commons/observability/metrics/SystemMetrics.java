package io.github.eipx.servicefoundation.commons.observability.metrics;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.ThreadMXBean;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.ToDoubleFunction;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.sun.management.OperatingSystemMXBean;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.binder.MeterBinder;

import static java.util.Objects.*;

/**
 * Additional metrics to Spring Boot metrics (see dependency micrometer-core , package io.micrometer.core.instrument.binder.jvm)
 */
public final class SystemMetrics implements MeterBinder {

    private static final Logger LOGGER = LoggerFactory.getLogger(SystemMetrics.class);
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]+");
    private static final ImmutableList<Tag> NO_TAGS = ImmutableList.of();

    @Override
    public void bindTo(MeterRegistry metricRegistry) {
        registerCpuMetrics(metricRegistry);
        registerGcMetrics(metricRegistry);
        registerMemoryMetrics(metricRegistry);
        registerThreadMetrics(metricRegistry);
    }

    private void registerMemoryMetrics(MeterRegistry metricRegistry) {
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        metricRegistry.gauge("memory.heap", NO_TAGS, memoryMXBean, mmb -> mmb.getHeapMemoryUsage().getMax());
        metricRegistry.gauge("memory.heap.used", NO_TAGS, memoryMXBean, mmb -> mmb.getHeapMemoryUsage().getUsed());
        metricRegistry.gauge("memory.non-heap.used", NO_TAGS, memoryMXBean, mmb -> mmb.getNonHeapMemoryUsage().getUsed());
    }

    private void registerGcMetrics(MeterRegistry metricRegistry) {
        List<GarbageCollectorMXBean> garbageCollectorMXBeans = ManagementFactory.getGarbageCollectorMXBeans();
        garbageCollectorMXBeans.forEach(gcBean -> {
            String gcName = WHITESPACE.matcher(gcBean.getName()).replaceAll("-");
            metricRegistry.gauge(gcName + ".count", NO_TAGS, gcBean, GarbageCollectorMXBean::getCollectionCount);
            metricRegistry.gauge(gcName + ".time", NO_TAGS, gcBean, GarbageCollectorMXBean::getCollectionTime);
        });
    }

    private void registerThreadMetrics(MeterRegistry metricRegistry) {
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        metricRegistry.gauge("thread.live.count", NO_TAGS, threadMXBean, ThreadMXBean::getThreadCount);
        metricRegistry.gauge("thread.deadlock.count", NO_TAGS, threadMXBean, tmb -> {
            long[] deadlockedThreads = tmb.findDeadlockedThreads();
            return deadlockedThreads == null ? 0 : deadlockedThreads.length;
        });
    }

    private void registerCpuMetrics(MeterRegistry metricRegistry) {
        OperatingSystemMXBean operatingSystemMXBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        try {
            metricRegistry.gauge("cpu.jvm.usage", NO_TAGS, operatingSystemMXBean, new GetProcessCpuTime(operatingSystemMXBean));
        } catch (Exception e) {
            LOGGER.warn("CPU usage metric is unavailable for this JVM", e);
        }
    }

    private static final class GetProcessCpuTime implements ToDoubleFunction<OperatingSystemMXBean> {

        private final AtomicLong lastNanos;

        private final AtomicLong lastCpuNanos;

        GetProcessCpuTime(OperatingSystemMXBean operatingSystemMXBean) throws ReflectiveOperationException {
            requireNonNull(operatingSystemMXBean, "operatingSystemMXBean");
            this.lastNanos = new AtomicLong(System.nanoTime());
            this.lastCpuNanos = new AtomicLong(operatingSystemMXBean.getProcessCpuTime());
        }

        @Override
        public double applyAsDouble(OperatingSystemMXBean operatingSystemMXBean) {
            try {
                long nanos = System.nanoTime();
                long cpuNanos = operatingSystemMXBean.getProcessCpuTime();
                return (double) (cpuNanos - lastCpuNanos.getAndSet(cpuNanos)) / (double) (nanos - lastNanos.getAndSet(nanos));
            } catch (Exception e) {
                LOGGER.debug("Failed to get process CPU time", e);
                return -1.0;
            }
        }

    }
}

package com.wepay.riff.metrics.jmx;

import com.wepay.riff.metrics.core.Counter;
import com.wepay.riff.metrics.core.Gauge;
import com.wepay.riff.metrics.core.Histogram;
import com.wepay.riff.metrics.core.Meter;
import com.wepay.riff.metrics.core.MetricFilter;
import com.wepay.riff.metrics.core.MetricRegistry;
import com.wepay.riff.metrics.core.Snapshot;
import com.wepay.riff.metrics.core.Timer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.InstanceNotFoundException;
import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("rawtypes")
public class JmxReporterTest {
    private final MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
    private final String name = UUID.randomUUID().toString().replaceAll("[{\\-}]", "");
    private final MetricRegistry registry = MetricRegistry.getInstance();

    private final JmxReporter reporter = JmxReporter.forRegistry(registry)
            .registerWith(mBeanServer)
            .inDomain(name)
            .convertDurationsTo(TimeUnit.MILLISECONDS)
            .convertRatesTo(TimeUnit.SECONDS)
            .filter(MetricFilter.ALL)
            .build();

    private final Gauge gauge = mock(Gauge.class);
    private final Counter counter = mock(Counter.class);
    private final Histogram histogram = mock(Histogram.class);
    private final Meter meter = mock(Meter.class);
    private final Timer timer = mock(Timer.class);
    private final ObjectNameFactory mockObjectNameFactory = mock(ObjectNameFactory.class);
    private final ObjectNameFactory concreteObjectNameFactory = reporter.getObjectNameFactory();

    @Before
    public void setUp() {
        when(gauge.getValue()).thenReturn(1);

        when(counter.getCount()).thenReturn(100L);

        when(histogram.getCount()).thenReturn(1L);

        final Snapshot hSnapshot = mock(Snapshot.class);
        when(hSnapshot.getMax()).thenReturn(2L);
        when(hSnapshot.getMean()).thenReturn(3.0);
        when(hSnapshot.getMin()).thenReturn(4L);
        when(hSnapshot.getStdDev()).thenReturn(5.0);
        when(hSnapshot.getMedian()).thenReturn(6.0);
        when(hSnapshot.get75thPercentile()).thenReturn(7.0);
        when(hSnapshot.get95thPercentile()).thenReturn(8.0);
        when(hSnapshot.get98thPercentile()).thenReturn(9.0);
        when(hSnapshot.get99thPercentile()).thenReturn(10.0);
        when(hSnapshot.get999thPercentile()).thenReturn(11.0);
        when(hSnapshot.size()).thenReturn(1);

        when(histogram.getSnapshot()).thenReturn(hSnapshot);

        when(meter.getCount()).thenReturn(1L);
        when(meter.getMeanRate()).thenReturn(2.0);
        when(meter.getOneMinuteRate()).thenReturn(3.0);
        when(meter.getFiveMinuteRate()).thenReturn(4.0);
        when(meter.getFifteenMinuteRate()).thenReturn(5.0);

        when(timer.getCount()).thenReturn(1L);
        when(timer.getMeanRate()).thenReturn(2.0);
        when(timer.getOneMinuteRate()).thenReturn(3.0);
        when(timer.getFiveMinuteRate()).thenReturn(4.0);
        when(timer.getFifteenMinuteRate()).thenReturn(5.0);

        final Snapshot tSnapshot = mock(Snapshot.class);
        when(tSnapshot.getMax()).thenReturn(TimeUnit.MILLISECONDS.toNanos(100));
        when(tSnapshot.getMean()).thenReturn((double) TimeUnit.MILLISECONDS.toNanos(200));
        when(tSnapshot.getMin()).thenReturn(TimeUnit.MILLISECONDS.toNanos(300));
        when(tSnapshot.getStdDev()).thenReturn((double) TimeUnit.MILLISECONDS.toNanos(400));
        when(tSnapshot.getMedian()).thenReturn((double) TimeUnit.MILLISECONDS.toNanos(500));
        when(tSnapshot.get75thPercentile()).thenReturn((double) TimeUnit.MILLISECONDS.toNanos(600));
        when(tSnapshot.get95thPercentile()).thenReturn((double) TimeUnit.MILLISECONDS.toNanos(700));
        when(tSnapshot.get98thPercentile()).thenReturn((double) TimeUnit.MILLISECONDS.toNanos(800));
        when(tSnapshot.get99thPercentile()).thenReturn((double) TimeUnit.MILLISECONDS.toNanos(900));
        when(tSnapshot.get999thPercentile()).thenReturn((double) TimeUnit.MILLISECONDS.toNanos(1000));
        when(tSnapshot.size()).thenReturn(1);

        when(timer.getSnapshot()).thenReturn(tSnapshot);

        registry.register("group", "gauge", gauge);
        registry.register("group", "test.counter", counter);
        registry.register("group", "test.histogram", histogram);
        registry.register("group", "test.meter", meter);
        registry.register("group", "test.another.timer", timer);

        reporter.start();
    }

    @After
    public void cleanUp() {
        reporter.stop();
        registry.removeAllMetrics();
    }

    @Test
    public void registersMBeansForMetricObjectsUsingProvidedObjectNameFactory() throws Exception {
        ObjectName n = new ObjectName(name + ":name=dummy");
        try {
            String widgetName = "something";
            when(mockObjectNameFactory.createName(any(String.class), any(String.class), any(String.class))).thenReturn(n);
            Gauge aGauge = mock(Gauge.class);
            when(aGauge.getValue()).thenReturn(1);

            try (JmxReporter reporter =
                     JmxReporter.forRegistry(registry)
                         .registerWith(mBeanServer)
                         .inDomain(name)
                         .createsObjectNamesWith(mockObjectNameFactory)
                         .build()) {
                registry.register("group", widgetName, aGauge);
                reporter.start();
                verify(mockObjectNameFactory).createName(eq("gauges"), eq("group"), eq("something"));
                reporter.stop();
            }
        } finally {
            if (mBeanServer.isRegistered(n)) {
                mBeanServer.unregisterMBean(n);
            }
        }
    }

    @Test
    public void registersMBeansForGauges() throws Exception {
        final AttributeList attributes = getAttributes("gauges", "group", "gauge", "Value");

        assertThat(values(attributes))
                .contains(entry("Value", 1));
    }

    @Test
    public void registersMBeansForCounters() throws Exception {
        final AttributeList attributes = getAttributes("counters", "group", "test.counter", "Count");

        assertThat(values(attributes))
                .contains(entry("Count", 100L));
    }

    @Test
    public void registersMBeansForHistograms() throws Exception {
        final AttributeList attributes = getAttributes("histograms", "group", "test.histogram",
                "Count",
                "Max",
                "Mean",
                "Min",
                "StdDev",
                "50thPercentile",
                "75thPercentile",
                "95thPercentile",
                "98thPercentile",
                "99thPercentile",
                "999thPercentile",
                "SnapshotSize");

        assertThat(values(attributes))
                .contains(entry("Count", 1L))
                .contains(entry("Max", 2L))
                .contains(entry("Mean", 3.0))
                .contains(entry("Min", 4L))
                .contains(entry("StdDev", 5.0))
                .contains(entry("50thPercentile", 6.0))
                .contains(entry("75thPercentile", 7.0))
                .contains(entry("95thPercentile", 8.0))
                .contains(entry("98thPercentile", 9.0))
                .contains(entry("99thPercentile", 10.0))
                .contains(entry("999thPercentile", 11.0))
                .contains(entry("SnapshotSize", 1L));
    }

    @Test
    public void registersMBeansForMeters() throws Exception {
        final AttributeList attributes = getAttributes("meters", "group", "test.meter",
                "Count",
                "MeanRate",
                "OneMinuteRate",
                "FiveMinuteRate",
                "FifteenMinuteRate",
                "RateUnit");

        assertThat(values(attributes))
                .contains(entry("Count", 1L))
                .contains(entry("MeanRate", 2.0))
                .contains(entry("OneMinuteRate", 3.0))
                .contains(entry("FiveMinuteRate", 4.0))
                .contains(entry("FifteenMinuteRate", 5.0))
                .contains(entry("RateUnit", "events/second"));
    }

    @Test
    public void registersMBeansForTimers() throws Exception {
        final AttributeList attributes = getAttributes("timers", "group", "test.another.timer",
                "Count",
                "MeanRate",
                "OneMinuteRate",
                "FiveMinuteRate",
                "FifteenMinuteRate",
                "Max",
                "Mean",
                "Min",
                "StdDev",
                "50thPercentile",
                "75thPercentile",
                "95thPercentile",
                "98thPercentile",
                "99thPercentile",
                "999thPercentile",
                "RateUnit",
                "DurationUnit");

        assertThat(values(attributes))
                .contains(entry("Count", 1L))
                .contains(entry("MeanRate", 2.0))
                .contains(entry("OneMinuteRate", 3.0))
                .contains(entry("FiveMinuteRate", 4.0))
                .contains(entry("FifteenMinuteRate", 5.0))
                .contains(entry("Max", 100.0))
                .contains(entry("Mean", 200.0))
                .contains(entry("Min", 300.0))
                .contains(entry("StdDev", 400.0))
                .contains(entry("50thPercentile", 500.0))
                .contains(entry("75thPercentile", 600.0))
                .contains(entry("95thPercentile", 700.0))
                .contains(entry("98thPercentile", 800.0))
                .contains(entry("99thPercentile", 900.0))
                .contains(entry("999thPercentile", 1000.0))
                .contains(entry("RateUnit", "events/second"))
                .contains(entry("DurationUnit", "milliseconds"));
    }

    @Test
    public void cleansUpAfterItselfWhenStopped() throws Exception {
        reporter.stop();

        try {
            getAttributes("gauges", "gauge", "Value");
            failBecauseExceptionWasNotThrown(InstanceNotFoundException.class);
        } catch (InstanceNotFoundException e) {

        }
    }

    @Test
    public void objectNameModifyingMBeanServer() throws Exception {
        MBeanServer mockedMBeanServer = mock(MBeanServer.class);

        // overwrite the objectName
        when(mockedMBeanServer.registerMBean(any(Object.class), any(ObjectName.class))).thenReturn(new ObjectInstance("DOMAIN:key=value", "className"));

        MetricRegistry testRegistry = MetricRegistry.getInstance();
        testRegistry.removeAllMetrics();

        try (JmxReporter testJmxReporter =
                 JmxReporter.forRegistry(testRegistry)
                     .registerWith(mockedMBeanServer)
                     .inDomain(name)
                     .build()) {

            testJmxReporter.start();

            // should trigger a registerMBean
            testRegistry.timer("group", "test");

            // should trigger an unregisterMBean with the overwritten objectName = "DOMAIN:key=value"
            testJmxReporter.stop();

            verify(mockedMBeanServer).unregisterMBean(new ObjectName("DOMAIN:key=value"));

        }
    }

    @Test
    public void testJmxMetricNameWithAsterisk() {
        MetricRegistry metricRegistry = MetricRegistry.getInstance();
        try (JmxReporter jmxReporter = JmxReporter.forRegistry(metricRegistry).build()) {
            jmxReporter.start();
            metricRegistry.counter("group", "test*");
        }
    }

    private AttributeList getAttributes(String type, String group, String name, String... attributeNames) throws JMException {
        ObjectName n = concreteObjectNameFactory.createName(type, group, name);
        return mBeanServer.getAttributes(n, attributeNames);
    }

    private SortedMap<String, Object> values(AttributeList attributes) {
        final TreeMap<String, Object> values = new TreeMap<>();
        for (Object o : attributes) {
            final Attribute attribute = (Attribute) o;
            values.put(attribute.getName(), attribute.getValue());
        }
        return values;
    }
}

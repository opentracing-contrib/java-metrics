/**
 * Copyright 2017-2018 The OpenTracing Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package io.opentracing.contrib.metrics.micrometer;

import io.micrometer.core.instrument.ImmutableTag;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.opentracing.contrib.api.SpanData;
import io.opentracing.contrib.metrics.MetricLabel;
import io.opentracing.contrib.metrics.label.BaggageMetricLabel;
import io.opentracing.tag.Tags;
import org.junit.Before;
import org.junit.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MicrometerMetricsReporterTest {

    public static final String METRIC_LABEL_NAME = "foo";
    public static final String METRIC_LABEL_VALUE = "bar";
    public static final String BAGGAGE_LABEL_NAME = "transaction";
    public static final String BAGGAGE_LABEL_VALUE = "n/a";
    public static final String TAG_LABEL_NAME = "Tag";
    public static final String TAG_LABEL_VALUE = "IAmATag";
    private SimpleMeterRegistry registry;

    @Before
    public void init() {
        for (MeterRegistry registry : Metrics.globalRegistry.getRegistries()) {
            Metrics.removeRegistry(registry);
        }
        this.registry = new SimpleMeterRegistry();
        Metrics.addRegistry(this.registry);
    }

    @Test
    public void testWithCustomMetricTypeNames() {
        String metricName = "testWithCustomMetricTypeNames";

        // prepare
        SpanData spanData = defaultMockSpanData();
        MicrometerMetricsReporter reporter = MicrometerMetricsReporter.newMetricsReporter()
                .withName(metricName)
                .withConstLabel("span.kind", Tags.SPAN_KIND_CLIENT)
                .build();

        // test
        reporter.reportSpan(spanData);

        // verify
        List<Tag> tags = defaultTags();

        assertEquals(100, (long) registry.find(metricName).timer().totalTime(TimeUnit.MILLISECONDS));
        assertEquals(1, Metrics.timer(metricName, tags).count());
    }

    @Test
    public void testSkipMetricReport() {
        String metricName = "testSkipMetricReport";

        // prepare
        SpanData spanData = defaultMockSpanData();
        MicrometerMetricsReporter reporter = MicrometerMetricsReporter.newMetricsReporter()
                .withName(metricName)
                .withConstLabel("skip", null) // any metric with a null value will cause the reporter to skip
                .build();

        // test
        reporter.reportSpan(spanData);

        // verify
        List<Tag> tags = defaultTags();
        assertEquals(0, Metrics.timer(metricName, tags).count());
    }

    @Test
    public void testWithTagAndBaggageLabels() {
        String metricName = "testWithTagAndBaggageLabels";

        // prepare
        SpanData spanData = defaultMockSpanData();
        MicrometerMetricsReporter reporter = MicrometerMetricsReporter.newMetricsReporter()
                .withName(metricName)
                .withBaggageLabel(BAGGAGE_LABEL_NAME, BAGGAGE_LABEL_VALUE)
                .withTagLabel(TAG_LABEL_NAME, TAG_LABEL_VALUE)
                .withConstLabel("span.kind", Tags.SPAN_KIND_CLIENT)
                .build();

        // test
        reporter.reportSpan(spanData);

        // verify
        List<Tag> tags = defaultTags();
        tags.add(new ImmutableTag(BAGGAGE_LABEL_NAME, BAGGAGE_LABEL_VALUE));
        tags.add(new ImmutableTag(TAG_LABEL_NAME, TAG_LABEL_VALUE));

        assertEquals(100, (long) registry.find(metricName).timer().totalTime(TimeUnit.MILLISECONDS));
        assertEquals(1, Metrics.timer(metricName, tags).count());
    }

    @Test
    public void testOverriddenTagAndBaggageLabels() {
        String metricName = "testOverriddenTagAndBaggageLabels";

        // prepare
        SpanData spanData = defaultMockSpanData();
        when(spanData.getBaggageItem(BAGGAGE_LABEL_NAME)).thenReturn("NewBaggageValue");
        when(spanData.getTags()).thenReturn(Collections.singletonMap(TAG_LABEL_NAME, "NewTagValue"));

        MicrometerMetricsReporter reporter = MicrometerMetricsReporter.newMetricsReporter()
                .withName(metricName)
                .withBaggageLabel(BAGGAGE_LABEL_NAME, BAGGAGE_LABEL_VALUE)
                .withTagLabel(TAG_LABEL_NAME, TAG_LABEL_VALUE)
                .withConstLabel("span.kind", Tags.SPAN_KIND_CLIENT)
                .build();

        // test
        reporter.reportSpan(spanData);

        // verify
        List<Tag> tags = defaultTags();
        tags.add(new ImmutableTag(BAGGAGE_LABEL_NAME, "NewBaggageValue"));
        tags.add(new ImmutableTag(TAG_LABEL_NAME, "NewTagValue"));

        assertEquals(100, (long) registry.find(metricName).timer().totalTime(TimeUnit.MILLISECONDS));
        assertEquals(1, Metrics.timer(metricName, tags).count());
    }

    @Test
    public void testWithCustomLabel() {
        String metricName = "testWithCustomLabel";

        // prepare
        SpanData spanData = defaultMockSpanData();
        MetricLabel metricLabel = new BaggageMetricLabel(METRIC_LABEL_NAME, METRIC_LABEL_VALUE);
        MicrometerMetricsReporter reporter = MicrometerMetricsReporter.newMetricsReporter()
                .withName(metricName)
                .withCustomLabel(metricLabel)
                .withConstLabel("span.kind", Tags.SPAN_KIND_CLIENT)
                .build();

        // test
        reporter.reportSpan(spanData);

        // verify
        List<Tag> tags = defaultTags();
        tags.add(new ImmutableTag(METRIC_LABEL_NAME, METRIC_LABEL_VALUE));

        assertEquals(100, (long) registry.find(metricName).timer().totalTime(TimeUnit.MILLISECONDS));
        assertEquals(1, Metrics.timer(metricName, tags).count());
    }

    @Test
    public void testReportSpan() {
        // prepare
        SpanData spanData = defaultMockSpanData();
        when(spanData.getTags()).thenReturn(Collections.singletonMap(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CLIENT));

        MicrometerMetricsReporter reporter = MicrometerMetricsReporter.newMetricsReporter()
                .withConstLabel("span.kind", Tags.SPAN_KIND_CLIENT)
                .build();

        // test
        reporter.reportSpan(spanData);

        // verify
        List<Tag> tags = defaultTags();

        assertEquals(100, (long) registry.find("span").timer().totalTime(TimeUnit.MILLISECONDS));
        assertEquals(1, Metrics.timer("span", tags).count());
    }

    @Test
    public void testWithPercentiles() {
        String metricName = "testWithPercentiles";

        // prepare
        SpanData spanData = defaultMockSpanData();

        MicrometerMetricsReporter reporter = MicrometerMetricsReporter.newMetricsReporter()
                .withName(metricName)
                .withPercentiles(0.5, 0.95)
                .build();

        // test
        reporter.reportSpan(spanData);

        // verify
        assertNull(registry.find(metricName+".percentile").tag("phi", "0.9").gauge());
        assertNotNull(registry.find(metricName+".percentile").tag("phi", "0.95").gauge());
        assertNotNull(registry.find(metricName+".percentile").tag("phi", "0.5").gauge());
    }

    @Test
    public void testDisablePercentiles() {
        String metricName = "testDisablePercentiles";

        // prepare
        SpanData spanData = defaultMockSpanData();

        MicrometerMetricsReporter reporter = MicrometerMetricsReporter.newMetricsReporter()
                .withName(metricName)
                .enablePercentileHistogram()
                .disablePercentileHistogram()
                .build();

        // test
        reporter.reportSpan(spanData);

        // verify
        assertNull(registry.find(metricName+".percentile").tag("phi", "0.9").gauge());
        assertNull(registry.find(metricName+".percentile").tag("phi", "0.95").gauge());
        assertNull(registry.find(metricName+".percentile").tag("phi", "0.5").gauge());
    }

    @Test
    public void testCustomPercentiles() {
        String metricName = "testCustomPercentiles";

        // prepare
        SpanData spanData = defaultMockSpanData();

        MicrometerMetricsReporter reporter = MicrometerMetricsReporter.newMetricsReporter()
                .withName(metricName)
                .enablePercentileHistogram()
                .withPercentiles(0.9)
                .build();

        // test
        reporter.reportSpan(spanData);

        // verify
        assertNotNull(registry.find(metricName+".percentile").tag("phi", "0.9").gauge());
        assertNull(registry.find(metricName+".percentile").tag("phi", "0.95").gauge());
        assertNull(registry.find(metricName+".percentile").tag("phi", "0.5").gauge());
    }

    @Test
    public void testSla() {
        String metricName = "testSla";

        // prepare
        SpanData spanData = defaultMockSpanData();

        MicrometerMetricsReporter reporter = MicrometerMetricsReporter.newMetricsReporter()
                .withName(metricName)
                .withSla(Duration.ofDays(1))
                .build();

        // test
        reporter.reportSpan(spanData);

        // verify
        assertNotNull(registry.find(metricName+".histogram").tag("le", "86400").gauge());
    }

    private List<Tag> defaultTags() {
        List<Tag> tags = new ArrayList<>();
        tags.add(new ImmutableTag("error", "false"));
        tags.add(new ImmutableTag("operation", "testop"));
        tags.add(new ImmutableTag("span.kind", Tags.SPAN_KIND_CLIENT));
        return tags;
    }

    private SpanData defaultMockSpanData() {
        Map<String, Object> tags = defaultTags().stream().collect(Collectors.toMap(Tag::getKey, Tag::getValue));
        SpanData spanData = mock(SpanData.class);
        when(spanData.getOperationName()).thenReturn("testop");
        when(spanData.getTags()).thenReturn(tags);
        when(spanData.getDuration()).thenReturn(100_000L); // 100ms
        return spanData;
    }
}

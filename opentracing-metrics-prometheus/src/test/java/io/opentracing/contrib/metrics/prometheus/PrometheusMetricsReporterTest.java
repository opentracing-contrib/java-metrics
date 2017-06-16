/**
 * Copyright 2017 The OpenTracing Authors
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
package io.opentracing.contrib.metrics.prometheus;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import io.opentracing.contrib.metrics.SpanData;
import io.opentracing.tag.Tags;
import io.prometheus.client.Collector.MetricFamilySamples;
import io.prometheus.client.Collector.MetricFamilySamples.Sample;
import io.prometheus.client.CollectorRegistry;

public class PrometheusMetricsReporterTest {

    private CollectorRegistry collectorRegistry;

    @Before
    public void init() {
        collectorRegistry = new CollectorRegistry();
    }

    @After
    public void close() {
        collectorRegistry.clear();
    }

    @Test
    public void testWithCustomMetricTypeNames() {
        PrometheusMetricsReporter reporter = PrometheusMetricsReporter.newMetricsReporter()
                .withName("MyName")
                .withCollectorRegistry(collectorRegistry)
                .withConstLabel("span.kind", Tags.SPAN_KIND_CLIENT) // Override the default, to make sure span metrics reported
                .build();

        SpanData spanData = mock(SpanData.class);
        when(spanData.getOperationName()).thenReturn("testop");
        when(spanData.getTags()).thenReturn(Collections.<String,Object>emptyMap());
        when(spanData.getDuration()).thenReturn(100000L);

        reporter.reportSpan(spanData);

        // Check span duration
        List<MetricFamilySamples> samples = reporter.getHistogram().collect();
        assertEquals(1, samples.size());
        assertEquals("MyName", samples.get(0).name);
    }

    @Test
    public void testReportSpan() {
        PrometheusMetricsReporter reporter = PrometheusMetricsReporter.newMetricsReporter()
                .withCollectorRegistry(collectorRegistry)
                .withConstLabel("span.kind", Tags.SPAN_KIND_CLIENT) // Override the default, to make sure span metrics reported
                .build();

        Map<String,Object> spanTags = new HashMap<String,Object>();
        spanTags.put(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CLIENT);

        SpanData spanData = mock(SpanData.class);
        when(spanData.getOperationName()).thenReturn("testop");
        when(spanData.getTags()).thenReturn(spanTags);
        when(spanData.getDuration()).thenReturn(100000L);

        reporter.reportSpan(spanData);

        // Check histogram
        List<MetricFamilySamples> samples = reporter.getHistogram().collect();
        assertEquals(1, samples.size());
        assertEquals(17, samples.get(0).samples.size());
        
        for (int i=0; i < samples.get(0).samples.size(); i++) {
            Sample sample = samples.get(0).samples.get(i);
            // Verify operation name
            assertEquals("testop", sample.labelValues.get(0));
            List<String> labelNames = new ArrayList<String>(sample.labelNames);
            if (labelNames.get(labelNames.size()-1).equals("le")) {
                // Remove additional label added by previous for all but last sample
                labelNames.remove(labelNames.size()-1);

                // Check if value is "+Inf" - if so, then check count
                // See https://prometheus.io/docs/concepts/metric_types/ (Histogram explanation
                // of count)
                if (sample.labelValues.get(sample.labelNames.size()-1).equals("+Inf")) {
                    assertEquals(1, (int)sample.value);                    
                }
            }
            assertEquals(Arrays.asList(reporter.getLabelNames()), labelNames);
        }
    }

    @Test
    public void testConvertLabel() {
        assertEquals("Hello9", PrometheusMetricsReporter.convertLabel("Hello9"));
        assertEquals("Hello_there", PrometheusMetricsReporter.convertLabel("Hello there"));  // Space invalid
        assertEquals("_tag1", PrometheusMetricsReporter.convertLabel("1tag1"));  // Leading number invalid
        assertEquals("tag_:_", PrometheusMetricsReporter.convertLabel("tag£:%"));  // Some characters invalid
    }

}

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
package io.opentracing.contrib.metrics;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.mockito.Mockito;

import io.opentracing.BaseSpan;
import io.opentracing.Span;
import io.opentracing.contrib.metrics.label.ConstMetricLabel;
import io.opentracing.tag.Tags;

public class AbstractMetricsReporterTest {

    @Test
    public void testSuppliedTag() {
        AbstractMetricsReporter reporter = new AbstractMetricsReporter(
                Collections.<MetricLabel>singletonList(new ConstMetricLabel("service", "TestService"))) {
            @Override
            public void reportSpan(BaseSpan<?> span, String operation, Map<String, Object> tags, long duration) {
            }
        };

        assertEquals(AbstractMetricsReporter.STANDARD_SPAN_LABELS.size(), reporter.metricLabels.length - 1);

        assertEquals("service", reporter.metricLabels[0].name());
        assertEquals("TestService", reporter.metricLabels[0].value(null, null, null));
    }

    @Test
    public void testDefaultLabels() {
        AbstractMetricsReporter reporter = new AbstractMetricsReporter(
                Collections.<MetricLabel>emptyList()) {
            @Override
            public void reportSpan(BaseSpan<?> span, String operation, Map<String, Object> tags, long duration) {
            }
        };

        assertEquals(AbstractMetricsReporter.STANDARD_SPAN_LABELS.size(), reporter.metricLabels.length);

        assertEquals(AbstractMetricsReporter.STANDARD_SPAN_LABELS, Arrays.asList(reporter.metricLabels));
    }

    @Test
    public void testWithSpecifiedTagValues() {
        AbstractMetricsReporter reporter = new AbstractMetricsReporter(
                Collections.<MetricLabel>emptyList()) {
            @Override
            public void reportSpan(BaseSpan<?> span, String operation, Map<String, Object> tags, long duration) {
            }
        };

        Span span = Mockito.mock(Span.class);

        Map<String,Object> spanTags = new HashMap<String,Object>();
        spanTags.put(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_SERVER);
        spanTags.put(Tags.ERROR.getKey(), true);

        String[] labelValues = reporter.getLabelValues(span, "testop", spanTags, 100000L);

        assertEquals(3, labelValues.length);
        assertEquals(Tags.SPAN_KIND_SERVER, labelValues[1]);
        assertEquals(Boolean.toString(true), labelValues[2]);
    }

    @Test
    public void testWithSpecifiedOverriddenTagValue() {
        MetricLabel errorMetricTag = new MetricLabel() {
            @Override
            public String name() {
                return Tags.ERROR.getKey();
            }
            @Override
            public Object defaultValue() {
                return null;
            }
            @Override
            public Object value(BaseSpan<?> span, String operation, Map<String, Object> tags) {
                Object error = tags.containsKey(name()) ? tags.get(name()) : false;
                if (tags.containsKey(Tags.HTTP_STATUS.getKey())) {
                    int status = (int)tags.get(Tags.HTTP_STATUS.getKey());
                    if (status > 400) {
                        error = "4xx";
                    } else if (status > 500) {
                        error = "5xx";
                    }
                }
                return error;
            }
        };

        // Add system specified tag (i.e. for 'service'), to ensure override metric tag is set in the correct
        // order even when additional tags are specified
        AbstractMetricsReporter reporter = new AbstractMetricsReporter(
                Arrays.<MetricLabel>asList(new ConstMetricLabel("service", "TestService"), errorMetricTag)) {
            @Override
            public void reportSpan(BaseSpan<?> span, String operation, Map<String, Object> tags, long duration) {
            }
        };

        Span span = Mockito.mock(Span.class);

        // Specify standard error tag and http status - which will then be used to derive a
        // custom error label/tag on the metric
        Map<String,Object> spanTags = new HashMap<String,Object>();
        spanTags.put(Tags.ERROR.getKey(), true);
        spanTags.put(Tags.HTTP_STATUS.getKey(), 401);
        spanTags.put(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CLIENT);

        String[] labelValues = reporter.getLabelValues(span, "testop", spanTags, 100000L);

        assertEquals(4, labelValues.length);
        assertEquals("TestService", labelValues[0]);
        assertEquals("testop", labelValues[1]);
        assertEquals(Tags.SPAN_KIND_CLIENT, labelValues[2]);
        assertEquals("4xx", labelValues[3]);
    }

}

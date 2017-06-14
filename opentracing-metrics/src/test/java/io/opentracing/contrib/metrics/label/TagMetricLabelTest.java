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
package io.opentracing.contrib.metrics.label;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import io.opentracing.contrib.metrics.MetricLabel;
import io.opentracing.contrib.metrics.MetricsSpanData;
import io.opentracing.contrib.metrics.label.TagMetricLabel;

public class TagMetricLabelTest {

    private static final String TEST_LABEL_DEFAULT = "testLabelDefault";
    private static final String TEST_LABEL = "testLabel";

    @Test
    public void testLabelDefault() {
        MetricLabel label = new TagMetricLabel(TEST_LABEL, TEST_LABEL_DEFAULT);
        MetricsSpanData metricsSpanData = mock(MetricsSpanData.class);
        when(metricsSpanData.getTags()).thenReturn(Collections.<String,Object>emptyMap());
        assertEquals(TEST_LABEL, label.name());
        assertEquals(TEST_LABEL_DEFAULT, label.value(metricsSpanData));
    }

    @Test
    public void testLabelFromTagWithValue() {
        MetricLabel label = new TagMetricLabel(TEST_LABEL, TEST_LABEL_DEFAULT);
        Map<String,Object> tags = new HashMap<String,Object>();
        tags.put(TEST_LABEL, "TagValue");
        MetricsSpanData metricsSpanData = mock(MetricsSpanData.class);
        when(metricsSpanData.getTags()).thenReturn(tags);
        assertEquals("TagValue", label.value(metricsSpanData));
    }

    @Test
    public void testLabelFromTagWithNull() {
        MetricLabel label = new TagMetricLabel(TEST_LABEL, TEST_LABEL_DEFAULT);
        Map<String,Object> tags = new HashMap<String,Object>();
        tags.put(TEST_LABEL, null);
        MetricsSpanData metricsSpanData = mock(MetricsSpanData.class);
        when(metricsSpanData.getTags()).thenReturn(tags);
        assertEquals(TEST_LABEL_DEFAULT, label.value(metricsSpanData));
    }

}

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

import org.junit.Test;
import static org.mockito.Mockito.*;

import io.opentracing.contrib.metrics.MetricLabel;
import io.opentracing.contrib.metrics.SpanData;
import io.opentracing.contrib.metrics.label.BaggageMetricLabel;

public class BaggageMetricLabelTest {

    private static final String TEST_LABEL_DEFAULT = "testLabelDefault";
    private static final String TEST_LABEL = "testLabel";

    @Test
    public void testLabelDefault() {
        MetricLabel label = new BaggageMetricLabel(TEST_LABEL, TEST_LABEL_DEFAULT);
        SpanData spanData = mock(SpanData.class);
        when(spanData.getBaggageItem(anyString())).thenReturn(null);

        assertEquals(TEST_LABEL, label.name());
        assertEquals(TEST_LABEL_DEFAULT, label.value(spanData));
        assertEquals(TEST_LABEL_DEFAULT, label.defaultValue());
        verify(spanData, times(1)).getBaggageItem(TEST_LABEL);
    }

    @Test
    public void testLabelFromBaggage() {
        MetricLabel label = new BaggageMetricLabel(TEST_LABEL, TEST_LABEL_DEFAULT);
        SpanData spanData = mock(SpanData.class);
        when(spanData.getBaggageItem(anyString())).thenReturn("BaggageValue");
        
        assertEquals("BaggageValue", label.value(spanData));
        verify(spanData, times(1)).getBaggageItem(TEST_LABEL);
    }

}

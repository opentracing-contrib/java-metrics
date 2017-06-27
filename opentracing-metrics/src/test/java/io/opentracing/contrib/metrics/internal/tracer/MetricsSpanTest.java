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
package io.opentracing.contrib.metrics.internal.tracer;

import static org.junit.Assert.assertEquals;

import java.util.Collections;

import org.junit.Test;
import org.mockito.Mockito;
import io.opentracing.Span;

public class MetricsSpanTest {

    @Test
    public void testSetOperation() {
        Span span = Mockito.mock(Span.class);
        MetricsSpan metricsSpan = new MetricsSpan(null, span, "initialOp", 0, null);
        assertEquals("initialOp", metricsSpan.getOperationName());
        metricsSpan.setOperationName("updatedOp");
        assertEquals("updatedOp", metricsSpan.getOperationName());
    }

    @Test
    public void testSetTag() {
        Span span = Mockito.mock(Span.class);
        MetricsSpan metricsSpan = new MetricsSpan(null, span, null, 0, Collections.<String,Object>emptyMap());
        metricsSpan.setTag("hello", "world");
        assertEquals("world", metricsSpan.getTags().get("hello"));
    }

}

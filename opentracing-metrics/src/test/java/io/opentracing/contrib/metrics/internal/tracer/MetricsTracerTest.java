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
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import io.opentracing.ActiveSpan;
import io.opentracing.BaseSpan;
import io.opentracing.Tracer;
import io.opentracing.contrib.metrics.Metrics;
import io.opentracing.contrib.metrics.MetricsReporter;
import io.opentracing.mock.MockSpan;
import io.opentracing.mock.MockTracer;
import io.opentracing.util.ThreadLocalActiveSpanSource;

public class MetricsTracerTest {

    @Captor
    private ArgumentCaptor<BaseSpan<?>> spanCaptor;
    
    @Captor
    private ArgumentCaptor<String> operationCaptor;

    @Captor
    private ArgumentCaptor<Long> durationCaptor;

    @Captor
    private ArgumentCaptor<Map<String,Object>> tagsCaptor;

    @Before
    public void init(){
       MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testStandardUsage() {
        Map<String,Object> sysTags = new HashMap<String,Object>();
        sysTags.put("service", "TestService");
        
        MetricsReporter reporter = Mockito.mock(MetricsReporter.class);
        MockTracer tracer = new MockTracer(new ThreadLocalActiveSpanSource());
        Tracer metricsTracer = Metrics.decorate(tracer, reporter);

        ActiveSpan parent = metricsTracer.buildSpan("parent").withTag("spanName","parent").startActive();
        parent.setTag("additionalTag", "parent");
        
        ActiveSpan child = metricsTracer.buildSpan("child").withTag("spanName","child").startActive();
        child.setTag("additionalTag", "child");

        // Test ref counting works fine
        ActiveSpan child2 = child.capture().activate();
        
        child2.deactivate();
        
        // Still should be ref to child span
        assertEquals(0, tracer.finishedSpans().size());

        child.deactivate();
        
        assertEquals(1, tracer.finishedSpans().size());

        parent.deactivate();

        List<MockSpan> spans = tracer.finishedSpans();
        assertEquals(2, spans.size());

        Mockito.verify(reporter, Mockito.times(2)).reportSpan(spanCaptor.capture(), operationCaptor.capture(),
                tagsCaptor.capture(), durationCaptor.capture());

        List<BaseSpan<?>> capturedSpans = spanCaptor.getAllValues();
        assertEquals(capturedSpans.size(), spans.size());

        for (int i=0; i < spans.size(); i++) {
            MockSpan span = spans.get(i);
            assertEquals(span.operationName(), span.tags().get("spanName"));
            assertEquals(span.operationName(), span.tags().get("additionalTag"));
            assertEquals(span.context(), capturedSpans.get(i).context());
        }

        assertEquals(Arrays.asList("child","parent"), operationCaptor.getAllValues());
        assertTrue(durationCaptor.getAllValues().get(0) < durationCaptor.getAllValues().get(1));
        assertEquals(tracer.finishedSpans().get(0).tags(), tagsCaptor.getAllValues().get(0));
        assertEquals(tracer.finishedSpans().get(1).tags(), tagsCaptor.getAllValues().get(1));
    }

}

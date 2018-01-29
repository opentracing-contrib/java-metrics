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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import io.opentracing.Scope;
import io.opentracing.Tracer;
import io.opentracing.contrib.api.SpanData;
import io.opentracing.mock.MockSpan;
import io.opentracing.mock.MockTracer;

public class MetricsTest {

    @Captor
    private ArgumentCaptor<SpanData> spanDataCaptor;
    
    @Before
    public void init(){
       MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testStandardUsage() {
        Map<String,Object> sysTags = new HashMap<String,Object>();
        sysTags.put("service", "TestService");
        
        MetricsReporter reporter = Mockito.mock(MetricsReporter.class);
        MockTracer tracer = new MockTracer();
        Tracer metricsTracer = Metrics.decorate(tracer, reporter);

        Scope parent = metricsTracer.buildSpan("parent").withTag("spanName","parent").startActive(true);
        parent.span().setTag("additionalTag", "parent");
        
        Scope child = metricsTracer.buildSpan("child").withTag("spanName","child").startActive(true);
        child.span().setTag("additionalTag", "child");

        assertEquals(0, tracer.finishedSpans().size());

        child.close();
        
        assertEquals(1, tracer.finishedSpans().size());

        parent.close();

        List<MockSpan> spans = tracer.finishedSpans();
        assertEquals(2, spans.size());

        Mockito.verify(reporter, Mockito.times(2)).reportSpan(spanDataCaptor.capture());

        List<SpanData> captured = spanDataCaptor.getAllValues();
        assertEquals(captured.size(), spans.size());

        for (int i=0; i < spans.size(); i++) {
            MockSpan span = spans.get(i);
            assertEquals(span.operationName(), span.tags().get("spanName"));
            assertEquals(span.operationName(), span.tags().get("additionalTag"));
        }

        assertTrue(captured.get(0).getDuration() < captured.get(1).getDuration());
        assertEquals("child", captured.get(0).getOperationName());
        assertEquals("parent", captured.get(1).getOperationName());
        assertEquals(tracer.finishedSpans().get(0).tags(), captured.get(0).getTags());
        assertEquals(tracer.finishedSpans().get(1).tags(), captured.get(1).getTags());
    }

    @Test
    public void testWithTags() {
        MetricsReporter reporter = Mockito.mock(MetricsReporter.class);
        MockTracer tracer = new MockTracer();
        Tracer metricsTracer = Metrics.decorate(tracer, reporter);

        Scope parent = metricsTracer.buildSpan("parent")
                .withTag("booleanTag", true)
                .withTag("numericTag", new Integer(100))
                .startActive(true);

        parent.close();

        List<MockSpan> spans = tracer.finishedSpans();
        assertEquals(1, spans.size());
        MockSpan span = spans.get(0);
        Map<String, Object> tags = span.tags();

        Object booleanTag = tags.get("booleanTag");
        assertNotNull("Expected a tag named 'booleanTag'", booleanTag);
        assertTrue("booleanTag should be a Boolean", booleanTag instanceof Boolean);
        assertEquals("booleanTag should be true", true, booleanTag);

        Object numericTag = tags.get("numericTag");
        assertNotNull("Expected a tag named 'numericTag'", numericTag);
        assertTrue("numericTag should be a Number", numericTag instanceof Number);
        assertEquals("numericTag should be 100", 100, numericTag);
    }

    @Test
    public void testWithStartTimestamp() throws InterruptedException {
        MetricsReporter reporter = Mockito.mock(MetricsReporter.class);
        MockTracer tracer = new MockTracer();
        Tracer metricsTracer = Metrics.decorate(tracer, reporter);

        long start = System.currentTimeMillis() * 687;
        Thread.sleep(100);
        Scope parent = metricsTracer.buildSpan("parent")
                .withStartTimestamp(start)
                .startActive(true);

        parent.close();

        List<MockSpan> spans = tracer.finishedSpans();
        assertEquals(1, spans.size());
        MockSpan span = spans.get(0);
        long started = span.startMicros();
        assertEquals(start, started);
    }

    @Test
    public void testAsChildOf() {
        MetricsReporter reporter = Mockito.mock(MetricsReporter.class);
        MockTracer tracer = new MockTracer();
        Tracer metricsTracer = Metrics.decorate(tracer, reporter);

        Scope parentSpan = metricsTracer.buildSpan("parent")
                .withTag("spanName","parent")
                .startActive(true);
        parentSpan.span().setTag("additionalTag", "parent");

        Scope childSpan = metricsTracer.buildSpan("child")
                .asChildOf(parentSpan.span())
                .withTag("spanName","child")
                .startActive(true);
        childSpan.span().setTag("additionalTag", "child");

        childSpan.close();
        parentSpan.close();

        List<MockSpan> spans = tracer.finishedSpans();
        assertEquals(2, spans.size());
        MockSpan span1 = spans.get(0);
        MockSpan span2 = spans.get(1);

        MockSpan parent, child;
        if (span1.operationName().equals("parent")) {
            parent = span1;
            child = span2;
        } else {
            parent = span2;
            child = span1;
        }

        assertEquals("Child's parent id should be spanId of parent", child.parentId(), parent.context().spanId());
        assertEquals(0, parent.parentId());
    }

}

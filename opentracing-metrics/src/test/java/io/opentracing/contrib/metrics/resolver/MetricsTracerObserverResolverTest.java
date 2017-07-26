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
package io.opentracing.contrib.metrics.resolver;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Collections;
import java.util.ServiceLoader;

import org.junit.Test;
import org.mockito.Mockito;

import io.opentracing.contrib.api.SpanData;
import io.opentracing.contrib.api.TracerObserver;
import io.opentracing.contrib.api.tracer.decorator.TracerObserverResolver;
import io.opentracing.contrib.metrics.MetricsReporter;

public class MetricsTracerObserverResolverTest {

    @Test
    public void testTracerResolverNoMetricsReporters() {
        TestMetricsReporterResolver.setMetricsReporters(Collections.<MetricsReporter>emptySet());

        TracerObserverResolver resolver = ServiceLoader.load(TracerObserverResolver.class).iterator().next();
        assertNull(resolver.resolve());
    }

    @Test
    public void testTracerResolverWithMetricsReporters() {
        MetricsReporter reporter = Mockito.mock(MetricsReporter.class);
        SpanData spanData = Mockito.mock(SpanData.class);

        TestMetricsReporterResolver.setMetricsReporters(Collections.singleton(reporter));

        TracerObserverResolver resolver = ServiceLoader.load(TracerObserverResolver.class).iterator().next();
        TracerObserver observer = resolver.resolve();
        
        assertNotNull(observer);

        observer.onStart(spanData).onFinish(spanData, System.currentTimeMillis());

        Mockito.verify(reporter).reportSpan(spanData);
    }
}

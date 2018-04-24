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
package io.opentracing.contrib.metrics.prometheus.spring.autoconfigure;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer;
import io.opentracing.contrib.api.SpanData;
import io.opentracing.contrib.metrics.MetricsReporter;
import io.opentracing.tag.Tags;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@SpringBootTest(classes = {PrometheusMetricsReporterAutoConfigurationTest.SpringConfiguration.class})
@RunWith(SpringJUnit4ClassRunner.class)
public class PrometheusMetricsReporterAutoConfigurationTest {
    @Autowired
    private MetricsReporter metricsReporter;

    @Configuration
    @EnableAutoConfiguration
    public static class SpringConfiguration {
    }

    @Test
    public void testOutOfTheBox() {
        // prepare
        SpanData metricSpanData = Mockito.mock(SpanData.class);

        Mockito.when(metricSpanData.getOperationName())
                .thenReturn("testOp");

        Mockito.when(metricSpanData.getTags())
                .thenReturn(Collections.<String, Object>singletonMap(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CLIENT));

        Mockito.when(metricSpanData.getDuration())
                .thenReturn(500L);

        // test
        metricsReporter.reportSpan(metricSpanData);

        // verify
        assertEquals(1, Metrics.globalRegistry.getRegistries().size());
        MeterRegistry registry = Metrics.globalRegistry.getRegistries().iterator().next();

        assertEquals(1, registry.getMeters().size());
        Meter meter = registry.getMeters().get(0);
        assertTrue(meter instanceof Timer);

        double total = ((Timer) meter).totalTime(TimeUnit.MICROSECONDS);
        assertEquals(500d, total, 0d);
    }

}

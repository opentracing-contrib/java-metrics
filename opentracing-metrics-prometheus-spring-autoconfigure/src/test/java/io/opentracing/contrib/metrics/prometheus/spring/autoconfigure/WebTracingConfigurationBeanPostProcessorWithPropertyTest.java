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

import static org.junit.Assert.assertEquals;

import java.util.regex.Pattern;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import io.opentracing.contrib.spring.web.autoconfig.WebTracingConfiguration;
import io.prometheus.client.CollectorRegistry;

@SpringBootTest(
        classes = {WebTracingConfigurationBeanPostProcessorWithPropertyTest.SpringConfiguration.class})
@RunWith(SpringJUnit4ClassRunner.class)
public class WebTracingConfigurationBeanPostProcessorWithPropertyTest {

    @Autowired
    private WebTracingConfiguration webTracingConfig;

    private static final CollectorRegistry testCollectorRegistry = new CollectorRegistry(); 

    @BeforeClass
    public static void init() {
        System.setProperty("OPENTRACING_METRICS_EXPORTER_HTTP_PATH", "/metrics");
    }

    @AfterClass
    public static void clear() {
        System.clearProperty("OPENTRACING_METRICS_EXPORTER_HTTP_PATH");
    }

    @Configuration
    @EnableAutoConfiguration
    public static class SpringConfiguration {
        @Bean
        public WebTracingConfiguration testConfig() {
            return WebTracingConfiguration.builder().withSkipPattern(Pattern.compile("/test")).build();
        }

        @Bean
        public CollectorRegistry testCollectorRegistry() {
            return testCollectorRegistry;
        }
    }

    @Test
    public void testModifiedSkipPattern() {
        assertEquals("/test|/metrics", webTracingConfig.getSkipPattern().pattern());
    }

}

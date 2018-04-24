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

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.MetricsServlet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(value = {MetricsServlet.class})
public class PrometheusServletConfiguration {
    @Autowired
    private CollectorRegistry collectorRegistry;

    @Value("${OPENTRACING_METRICS_EXPORTER_HTTP_PATH:false}")
    private String metricsPath;

    @Bean
    @ConditionalOnProperty(name="OPENTRACING_METRICS_EXPORTER_HTTP_PATH")
    ServletRegistrationBean registerPrometheusExporterServlet() {
        return new ServletRegistrationBean(new MetricsServlet(collectorRegistry), metricsPath);
    }

}

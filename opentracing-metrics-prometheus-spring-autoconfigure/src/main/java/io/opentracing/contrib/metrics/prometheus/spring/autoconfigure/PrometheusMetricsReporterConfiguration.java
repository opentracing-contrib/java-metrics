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

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.opentracing.contrib.metrics.MetricLabel;
import io.opentracing.contrib.metrics.MetricsReporter;
import io.opentracing.contrib.metrics.micrometer.MicrometerMetricsReporter;
import io.prometheus.client.CollectorRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

@Configuration
public class PrometheusMetricsReporterConfiguration {
    @Autowired(required=false)
    private Set<MetricLabel> metricLabels;

    @Autowired
    private CollectorRegistry collectorRegistry;

    @Value("${OPENTRACING_METRICS_NAME:}")
    private String metricsName;

    @Bean
    public MetricsReporter prometheusMetricsReporter(PrometheusMeterRegistry prometheusMeterRegistry) {
        Metrics.addRegistry(prometheusMeterRegistry);

        MicrometerMetricsReporter.Builder builder = MicrometerMetricsReporter.newMetricsReporter();
        if (metricsName != null && !metricsName.isEmpty()) {
            builder.withName(metricsName);
        }

        if (metricLabels != null && !metricLabels.isEmpty()) {
            for (MetricLabel label : metricLabels) {
                builder.withCustomLabel(label);
            }
        }
        return builder.build();
    }

    @Bean
    @ConditionalOnMissingBean
    public PrometheusMeterRegistry prometheusMeterRegistry() {
        return new PrometheusMeterRegistry(PrometheusConfig.DEFAULT, collectorRegistry, Clock.SYSTEM);
    }
}

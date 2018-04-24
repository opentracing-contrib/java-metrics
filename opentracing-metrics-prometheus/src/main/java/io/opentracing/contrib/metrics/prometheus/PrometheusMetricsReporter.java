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
package io.opentracing.contrib.metrics.prometheus;

import java.util.ArrayList;
import java.util.List;

import io.opentracing.contrib.api.SpanData;
import io.opentracing.contrib.metrics.AbstractMetricsReporter;
import io.opentracing.contrib.metrics.MetricLabel;
import io.opentracing.contrib.metrics.MetricsReporter;
import io.opentracing.contrib.metrics.label.BaggageMetricLabel;
import io.opentracing.contrib.metrics.label.ConstMetricLabel;
import io.opentracing.contrib.metrics.label.TagMetricLabel;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Histogram;

/**
 * This class provides a Prometheus based implementation of the {@link MetricsReporter}.
 * @deprecated use the Micrometer Metrics reporter, from the Micrometer module
 */
@Deprecated
public class PrometheusMetricsReporter extends AbstractMetricsReporter implements MetricsReporter {

    private final Histogram histogram;

    private PrometheusMetricsReporter(String name,
            CollectorRegistry registry, List<MetricLabel> labels) {
        super(labels);

        String[] labelNames = getLabelNames();
        this.histogram = Histogram.build().name(name).help("The span metrics")
                .labelNames(labelNames).register(registry);
    }

    @Override
    public void reportSpan(SpanData spanData) {
        String[] labelValues = getLabelValues(spanData);
        if (labelValues != null) {
            // Convert microseconds to seconds
            this.histogram.labels(labelValues).observe(spanData.getDuration() / (double)1000000);
        }
    }

    Histogram getHistogram() {
        return histogram;
    }

    /**
     * This method transforms the supplied label name to ensure it conforms to the required
     * Prometheus label format as defined by the regex "[a-zA-Z_:][a-zA-Z0-9_:]*".
     *
     * @param label The label
     * @return The converted label
     */
    protected static String convertLabel(String label) {
        StringBuilder builder = new StringBuilder(label);
        for (int i=0; i < builder.length(); i++) {
            char ch = builder.charAt(i);
            if (!(ch == '_' || ch == ':' || Character.isLetter(ch) || (i > 0 && Character.isDigit(ch)))) {
                builder.setCharAt(i, '_');
            }
        }
        return builder.toString();
    }

    protected String[] getLabelNames() {
        String[] labelNames = new String[metricLabels.length];
        for (int i=0; i < metricLabels.length; i++) {
            labelNames[i] = convertLabel(metricLabels[i].name());
        }
        return labelNames;
    }

    public static Builder newMetricsReporter() {
        return new Builder();
    }

    /**
     * This builder class is responsible for creating an instance of the Prometheus
     * metrics reporter.
     *
     */
    public static class Builder {
        private String name = "span";
        private CollectorRegistry collectorRegistry = CollectorRegistry.defaultRegistry;
        private List<MetricLabel> metricLabels = new ArrayList<MetricLabel>();

        public Builder withCollectorRegistry(CollectorRegistry collectorRegistry) {
            this.collectorRegistry = collectorRegistry;
            return this;
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withCustomLabel(MetricLabel label) {
            metricLabels.add(label);
            return this;
        }

        public Builder withConstLabel(String name, Object value) {
            metricLabels.add(new ConstMetricLabel(name, value));
            return this;
        }

        public Builder withTagLabel(String name, Object defaultValue) {
            metricLabels.add(new TagMetricLabel(name, defaultValue));
            return this;
        }

        public Builder withBaggageLabel(String name, Object defaultValue) {
            metricLabels.add(new BaggageMetricLabel(name, defaultValue));
            return this;
        }

        public PrometheusMetricsReporter build() {
            return new PrometheusMetricsReporter(name, collectorRegistry, metricLabels);
        }
    }
}

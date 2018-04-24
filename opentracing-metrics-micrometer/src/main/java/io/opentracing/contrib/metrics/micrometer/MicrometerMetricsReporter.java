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
package io.opentracing.contrib.metrics.micrometer;

import io.micrometer.core.instrument.ImmutableTag;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;
import io.opentracing.contrib.api.SpanData;
import io.opentracing.contrib.metrics.AbstractMetricsReporter;
import io.opentracing.contrib.metrics.MetricLabel;
import io.opentracing.contrib.metrics.MetricsReporter;
import io.opentracing.contrib.metrics.label.BaggageMetricLabel;
import io.opentracing.contrib.metrics.label.ConstMetricLabel;
import io.opentracing.contrib.metrics.label.TagMetricLabel;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * This class provides a Micrometer based implementation of the {@link MetricsReporter}.
 *
 */
public class MicrometerMetricsReporter extends AbstractMetricsReporter implements MetricsReporter {
    private final String name;
    private final Duration sla, minimumExpectedValue, maximumExpectedValue;
    private final double[] percentiles;
    private final boolean publishPercentileHistogram;
    private final MeterRegistry registry;

    protected MicrometerMetricsReporter(String name, List<MetricLabel> labels,
                                        MeterRegistry registry,
                                        Duration sla, Duration minimumExpectedValue, Duration maximumExpectedValue,
                                        boolean publishPercentileHistogram,
                                        double... percentiles) {
        super(labels);
        this.name = name;
        this.registry = registry;
        this.sla = sla;
        this.minimumExpectedValue = minimumExpectedValue;
        this.maximumExpectedValue = maximumExpectedValue;
        this.publishPercentileHistogram = publishPercentileHistogram;
        this.percentiles = percentiles;
    }

    @Override
    public void reportSpan(SpanData spanData) {
        boolean skip = Arrays.stream(this.metricLabels).anyMatch(m -> m.value(spanData) == null);
        if (skip) {
            return;
        }

        List<Tag> tags = Arrays.stream(this.metricLabels)
            .map(m -> new ImmutableTag(m.name(), m.value(spanData).toString()))
            .collect(Collectors.toList());

        Timer timer = this.registry.find(this.name).tags(tags).timer();
        if (null != timer) {
            // we have a metric registered already, just record the timing:
            timer.record(spanData.getDuration(), TimeUnit.MICROSECONDS);
            return;
        }

        // would be awesome if we could reuse the builder, but looks like we can't, as we can't override the name
        Timer.Builder builder = Timer.builder(this.name).tags(tags);
        if (publishPercentileHistogram) {
            builder.publishPercentileHistogram();
        }

        if (null != percentiles) {
            builder.publishPercentiles(percentiles);
        }

        if (null != sla) {
            builder.sla(sla);
        }

        if (null != minimumExpectedValue) {
            builder.minimumExpectedValue(minimumExpectedValue);
        }

        if (null != maximumExpectedValue) {
            builder.maximumExpectedValue(maximumExpectedValue);
        }

        builder.register(this.registry).record(spanData.getDuration(), TimeUnit.MICROSECONDS);
    }

    public static Builder newMetricsReporter() {
        return new Builder();
    }

    /**
     * This builder class is responsible for creating an instance of the Micrometer
     * metrics reporter.
     *
     */
    public static class Builder {
        private String name = "span";
        private Duration sla, minimumExpectedValue, maximumExpectedValue;
        private double[] percentiles;
        private boolean publishPercentileHistogram;
        private MeterRegistry registry = Metrics.globalRegistry;

        private List<MetricLabel> metricLabels = new ArrayList<>();

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

        public Builder withSla(Duration sla) {
            this.sla = sla;
            return this;
        }

        public Builder withMinimumExpectedValue(Duration minimumExpectedValue) {
            this.minimumExpectedValue = minimumExpectedValue;
            return this;
        }

        public Builder withMaximumExpectedValue(Duration maximumExpectedValue) {
            this.maximumExpectedValue = maximumExpectedValue;
            return this;
        }

        public Builder withPercentiles(double... percentiles) {
            this.percentiles = percentiles;
            return this;
        }

        public Builder enablePercentileHistogram() {
            this.publishPercentileHistogram = true;
            return this;
        }

        public Builder disablePercentileHistogram() {
            this.publishPercentileHistogram = false;
            return this;
        }

        public Builder withRegistry(MeterRegistry registry) {
            this.registry = registry;
            return this;
        }

        public MicrometerMetricsReporter build() {
            return new MicrometerMetricsReporter(name, metricLabels,
                    registry,
                    sla, minimumExpectedValue, maximumExpectedValue,
                    publishPercentileHistogram,
                    percentiles);
        }
    }
}

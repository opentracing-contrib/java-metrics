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
package io.opentracing.contrib.metrics;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import io.opentracing.contrib.api.SpanData;
import io.opentracing.contrib.api.SpanObserver;
import io.opentracing.contrib.api.TracerObserver;

/**
 * This class implements the {@link TracerObserver} API to observer when spans finish, to
 * enable their metrics to be reported.
 *
 */
public class MetricsObserver implements TracerObserver {

    private final MetricsSpanObserver spanObserver;

    public MetricsObserver(MetricsReporter metricsReporter) {
        this(Collections.singleton(metricsReporter));
    }

    public MetricsObserver(Set<MetricsReporter> metricsReporters) {
        spanObserver = new MetricsSpanObserver(metricsReporters);
    }

    @Override
    public SpanObserver onStart(SpanData spanData) {
        return spanObserver;
    }

    private class MetricsSpanObserver implements SpanObserver {

        private final Set<MetricsReporter> metricsReporters;

        public MetricsSpanObserver(final Set<MetricsReporter> metricsReporters) {
            this.metricsReporters = new HashSet<MetricsReporter>(metricsReporters);
        }

        @Override
        public void onSetOperationName(SpanData spanData, String operationName) {
        }

        @Override
        public void onSetTag(SpanData spanData, String key, Object value) {
        }

        @Override
        public void onSetBaggageItem(SpanData spanData, String key, String value) {
        }

        @Override
        public void onLog(SpanData spanData, long timestampMicroseconds, Map<String, ?> fields) {
        }

        @Override
        public void onLog(SpanData spanData, long timestampMicroseconds, String event) {
        }

        @Override
        public void onFinish(SpanData spanData, long finishMicros) {
            for (MetricsReporter reporter : metricsReporters) {
                reporter.reportSpan(spanData);
            }
        }
    }
}

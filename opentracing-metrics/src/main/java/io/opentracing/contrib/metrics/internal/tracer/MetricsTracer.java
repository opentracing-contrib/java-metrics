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

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import io.opentracing.ActiveSpan;
import io.opentracing.BaseSpan;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.contrib.metrics.MetricsReporter;
import io.opentracing.contrib.metrics.MetricsSpanData;
import io.opentracing.propagation.Format;

public class MetricsTracer implements Tracer {

    private final Tracer wrappedTracer;
    private final MetricsReporter reporter;

    private final Map<SpanContext,MetricData> metricData = new WeakHashMap<SpanContext,MetricData>();

    public MetricsTracer(Tracer tracer, MetricsReporter reporter) {
        this.wrappedTracer = tracer;
        this.reporter = reporter;
    }

    MetricsReporter getReporter() {
        return reporter;
    }

    @Override
    public ActiveSpan activeSpan() {
        return new MetricsActiveSpan(this, wrappedTracer.activeSpan());
    }

    @Override
    public ActiveSpan makeActive(Span span) {
        return new MetricsActiveSpan(this, wrappedTracer.makeActive(span));
    }

    @Override
    public SpanBuilder buildSpan(String operation) {
        return new MetricsSpanBuilder(operation, wrappedTracer.buildSpan(operation));
    }

    @Override
    public <C> SpanContext extract(Format<C> format, C carrier) {
        return wrappedTracer.extract(format, carrier);
    }

    @Override
    public <C> void inject(SpanContext context, Format<C> format, C carrier) {
        wrappedTracer.inject(context, format, carrier);
    }

    void spanStarted(BaseSpan<?> span, String operationName, long startNanoTime, Map<String,Object> tags) {
        if (reporter != null) {
            synchronized (metricData) {
                metricData.put(span.context(), new MetricData(span, operationName, startNanoTime, tags));
            }
        }
    }

    void spanUpdateTag(SpanContext context, String key, Object value) {
        if (reporter != null) {
            synchronized (metricData) {
                MetricData data = metricData.get(context);
                if (data != null) {
                    data.getTags().put(key, value);
                }
            }
        }
    }

    void spanUpdateOperation(SpanContext context, String operationName) {
        if (reporter != null) {
            synchronized (metricData) {
                MetricData data = metricData.get(context);
                if (data != null) {
                    data.setOperationName(operationName);
                }
            }
        }        
    }

    void spanFinished(SpanContext context, long finishNanoTime) {
        if (reporter != null) {
            MetricData data = null;
            synchronized (metricData) {
                data = metricData.remove(context);
            }
            if (data != null) {
                data.setFinishNanoTime(finishNanoTime);
                reporter.reportSpan(data);
            }
        }
    }

    void spanCreatedContinuation(SpanContext context) {
        if (reporter != null) {
            synchronized (metricData) {
                MetricData data = metricData.get(context);
                if (data != null) {
                    data.incrementRefCount();
                }
            }
        }
    }

    void spanDeactivated(SpanContext context) {
        if (reporter != null) {
            synchronized (metricData) {
                MetricData data = metricData.get(context);
                if (data != null && data.decrementRefCount() == 0) {
                    spanFinished(context, System.nanoTime());
                }
            }
        }
    }

    static class MetricData implements MetricsSpanData {
        private final AtomicInteger refCount;
        private BaseSpan<?> span;
        private String operationName;
        private final long startNanoTime;
        private long finishNanoTime;
        private final Map<String,Object> tags;
        
        public MetricData(BaseSpan<?> span, String operationName, long startNanoTime, Map<String,Object> tags) {
            this.span = span;
            this.operationName = operationName;
            this.startNanoTime = startNanoTime;
            this.tags = tags;
            this.refCount = new AtomicInteger(1);
        }

        @Override
        public SpanContext getSpanContext() {
            return span.context();
        }

        @Override
        public String getOperationName() {
            return operationName;
        }

        public void setOperationName(String operationName) {
            this.operationName = operationName;
        }

        @Override
        public long getDuration() {
            return TimeUnit.NANOSECONDS.toMicros(finishNanoTime - startNanoTime);
        }

        @Override
        public Map<String,Object> getTags() {
            return tags;
        }

        @Override
        public String getBaggageItem(String key) {
            return span.getBaggageItem(key);
        }

        public void incrementRefCount() {
            refCount.incrementAndGet();
        }

        public int decrementRefCount() {
            return refCount.decrementAndGet();
        }

        public void setFinishNanoTime(long finishNanoTime) {
            this.finishNanoTime = finishNanoTime;
        }
    }

    public class MetricsSpanBuilder implements SpanBuilder {
        
        private final String operationName;
        private final SpanBuilder wrappedBuilder;
        private final long startNanoTime = System.nanoTime();
        private final Map<String,Object> tags = new HashMap<String,Object>();

        public MetricsSpanBuilder(String operationName, SpanBuilder builder) {
            this.operationName = operationName;
            this.wrappedBuilder = builder;
        }

        @Override
        public SpanBuilder asChildOf(SpanContext parent) {
            wrappedBuilder.asChildOf(parent);
            return this;
        }

        @Override
        public SpanBuilder asChildOf(BaseSpan<?> parent) {
            wrappedBuilder.asChildOf(parent);
            return this;
        }

        @Override
        public SpanBuilder addReference(String referenceType, SpanContext referencedContext) {
            wrappedBuilder.addReference(referenceType, referencedContext);
            return this;
        }

        @Override
        public SpanBuilder ignoreActiveSpan() {
            wrappedBuilder.ignoreActiveSpan();
            return this;
        }

        @Override
        public SpanBuilder withTag(String key, String value) {
            tags.put(key, value);
            wrappedBuilder.withTag(key, value);
            return this;
        }

        @Override
        public SpanBuilder withTag(String key, boolean value) {
            tags.put(key, value);
            wrappedBuilder.withTag(key, value);
            return this;
        }

        @Override
        public SpanBuilder withTag(String key, Number value) {
            tags.put(key, value);
            wrappedBuilder.withTag(key, value);
            return this;
        }

        @Override
        public SpanBuilder withStartTimestamp(long microseconds) {
            wrappedBuilder.withStartTimestamp(microseconds);
            return this;
        }

        @Override
        public ActiveSpan startActive() {
            ActiveSpan activeSpan = new MetricsActiveSpan(MetricsTracer.this, wrappedBuilder.startActive());
            spanStarted(activeSpan, operationName, startNanoTime, tags);
            return activeSpan;
        }

        @Override
        public Span startManual() {
            Span span = new MetricsSpan(MetricsTracer.this, wrappedBuilder.startManual());
            spanStarted(span, operationName, startNanoTime, tags);
            return span;
        }

        @Override
        public Span start() {
            return startManual();
        }

    }
}

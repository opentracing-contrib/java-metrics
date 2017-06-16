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

import io.opentracing.ActiveSpan;
import io.opentracing.BaseSpan;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.contrib.metrics.MetricsReporter;
import io.opentracing.propagation.Format;

public class MetricsTracer implements Tracer {

    private final Tracer wrappedTracer;
    private final MetricsReporter reporter;

    public MetricsTracer(Tracer tracer, MetricsReporter reporter) {
        this.wrappedTracer = tracer;
        this.reporter = reporter;
    }

    MetricsReporter getReporter() {
        return reporter;
    }

    @Override
    public ActiveSpan activeSpan() {
        return wrappedTracer.activeSpan();
    }

    @Override
    public ActiveSpan makeActive(Span span) {
        return wrappedTracer.makeActive(span);
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
            return wrappedTracer.makeActive(startManual());
        }

        @Override
        public Span startManual() {
            Span span = new MetricsSpan(MetricsTracer.this, wrappedBuilder.startManual(),
                    operationName, startNanoTime, tags);
            return span;
        }

        @Override
        public Span start() {
            return startManual();
        }

    }
}

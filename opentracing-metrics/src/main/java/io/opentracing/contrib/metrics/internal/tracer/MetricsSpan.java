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

import java.util.Map;

import io.opentracing.Span;
import io.opentracing.SpanContext;

public class MetricsSpan implements Span  {

    private final Span wrappedSpan;
    private final MetricsTracer tracer;

    public MetricsSpan(MetricsTracer tracer, Span span) {
        this.tracer = tracer;
        this.wrappedSpan = span;
    }

    protected Span span() {
        return wrappedSpan;
    }

    protected MetricsTracer metricsTracer() {
        return tracer;
    }

    @Override
    public SpanContext context() {
        return wrappedSpan.context();
    }

    @Override
    public String getBaggageItem(String name) {
        return wrappedSpan.getBaggageItem(name);
    }

    @Override
    public Span setBaggageItem(String name, String value) {
        wrappedSpan.setBaggageItem(name, value);
        return this;
    }

    @Override
    public Span log(Map<String, ?> fields) {
        wrappedSpan.log(fields);
        return this;
    }

    @Override
    public Span log(String event) {
        wrappedSpan.log(event);
        return this;
    }

    @Override
    public Span log(long timestampMicroseconds, Map<String, ?> fields) {
        wrappedSpan.log(timestampMicroseconds, fields);
        return this;
    }

    @Override
    public Span log(long timestampMicroseconds, String event) {
        wrappedSpan.log(timestampMicroseconds, event);
        return this;
    }

    @Override
    public Span setOperationName(String operationName) {
        wrappedSpan.setOperationName(operationName);
        tracer.spanUpdateOperation(wrappedSpan.context(), operationName);
        return this;
    }

    @Override
    public Span setTag(String key, String value) {
        wrappedSpan.setTag(key, value);
        tracer.spanUpdateTag(wrappedSpan.context(), key, value);
        return this;
    }

    @Override
    public Span setTag(String key, boolean value) {
        wrappedSpan.setTag(key, value);
        tracer.spanUpdateTag(wrappedSpan.context(), key, value);
        return this;
    }

    @Override
    public Span setTag(String key, Number value) {
        wrappedSpan.setTag(key, value);
        tracer.spanUpdateTag(wrappedSpan.context(), key, value);
        return this;
    }

    @SuppressWarnings("deprecation")
    @Override
    public Span log(String eventName, Object payload) {
        return wrappedSpan.log(eventName, payload);
    }

    @SuppressWarnings("deprecation")
    @Override
    public Span log(long timestampMicroseconds, String eventName, Object payload) {
        return wrappedSpan.log(timestampMicroseconds, eventName, payload);
    }

    @Override
    public void finish() {
        wrappedSpan.finish();
        metricsTracer().spanFinished(wrappedSpan.context(), System.nanoTime());
    }

    @Override
    public void finish(long finishMicros) {
        wrappedSpan.finish(finishMicros);
        metricsTracer().spanFinished(wrappedSpan.context(), System.nanoTime());
    }

}

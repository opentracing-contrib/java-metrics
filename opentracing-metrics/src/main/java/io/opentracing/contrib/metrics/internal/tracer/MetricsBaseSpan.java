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

import io.opentracing.BaseSpan;
import io.opentracing.SpanContext;

public class MetricsBaseSpan<T extends BaseSpan<?>> implements BaseSpan<T> {

    private final T span;
    private final MetricsTracer tracer;

    public MetricsBaseSpan(MetricsTracer tracer, T span) {
        this.tracer = tracer;
        this.span = span;
    }

    protected T span() {
        return span;
    }

    protected MetricsTracer metricsTracer() {
        return tracer;
    }

    @Override
    public SpanContext context() {
        return span.context();
    }

    @Override
    public String getBaggageItem(String name) {
        return span.getBaggageItem(name);
    }

    @Override
    public T setBaggageItem(String name, String value) {
        return this.setBaggageItem(name, value);
    }

    @SuppressWarnings("unchecked")
    @Override
    public T log(Map<String, ?> fields) {
        span.log(fields);
        return (T)this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T log(String event) {
        span.log(event);
        return (T)this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T log(long timestampMicroseconds, Map<String, ?> fields) {
        span.log(timestampMicroseconds, fields);
        return (T)this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T log(long timestampMicroseconds, String event) {
        span.log(timestampMicroseconds, event);
        return (T)this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T setOperationName(String operationName) {
        span.setOperationName(operationName);
        tracer.spanUpdateOperation(span.context(), operationName);
        return (T)this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T setTag(String key, String value) {
        span.setTag(key, value);
        tracer.spanUpdateTag(span.context(), key, value);
        return (T)this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T setTag(String key, boolean value) {
        span.setTag(key, value);
        tracer.spanUpdateTag(span.context(), key, value);
        return (T)this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T setTag(String key, Number value) {
        span.setTag(key, value);
        tracer.spanUpdateTag(span.context(), key, value);
        return (T)this;
    }

    @SuppressWarnings({ "deprecation", "unchecked" })
    @Override
    public T log(String eventName, Object payload) {
        return (T)span.log(eventName, payload);
    }

    @SuppressWarnings({ "deprecation", "unchecked" })
    @Override
    public T log(long timestampMicroseconds, String eventName, Object payload) {
        return (T)span.log(timestampMicroseconds, eventName, payload);
    }

}

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

import io.opentracing.ActiveSpan;
import io.opentracing.SpanContext;

public class MetricsActiveSpan extends MetricsBaseSpan<ActiveSpan> implements ActiveSpan  {

    public MetricsActiveSpan(MetricsTracer tracer, ActiveSpan span) {
        super(tracer, span);
    }

    public Continuation capture() {
        return new MetricsContinuation(metricsTracer(), span().context(), span().capture());
    }

    public void deactivate() {
        span().deactivate();
        metricsTracer().spanDeactivated(context());
    }

    public void close() {
        deactivate();
    }

    private static class MetricsContinuation implements Continuation {

        private final MetricsTracer tracer;
        private final Continuation continuation;

        public MetricsContinuation(MetricsTracer tracer, SpanContext context, Continuation continuation) {
            this.tracer = tracer;
            this.continuation = continuation;
            tracer.spanCreatedContinuation(context);
        }

        @Override
        public ActiveSpan activate() {
            return new MetricsActiveSpan(tracer, continuation.activate());
        }
        
    }
}

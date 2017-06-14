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

import io.opentracing.Span;

public class MetricsSpan extends MetricsBaseSpan<Span> implements Span  {

    public MetricsSpan(MetricsTracer tracer, Span span) {
        super(tracer, span);
    }

    @Override
    public void finish() {
        span().finish();
        metricsTracer().spanFinished(span().context(), System.nanoTime());
    }

    @Override
    public void finish(long finishMicros) {
        span().finish(finishMicros);
        metricsTracer().spanFinished(span().context(), System.nanoTime());
    }

}

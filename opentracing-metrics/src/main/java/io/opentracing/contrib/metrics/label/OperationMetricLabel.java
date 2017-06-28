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
package io.opentracing.contrib.metrics.label;

import io.opentracing.contrib.api.SpanData;
import io.opentracing.contrib.metrics.MetricLabel;

/**
 * This implementation obtains the metric label value from the span's
 * operation.
 *
 */
public class OperationMetricLabel implements MetricLabel {

    public OperationMetricLabel() {
    }

    @Override
    public String name() {
        return "operation";
    }

    @Override
    public Object defaultValue() {
        return null;
    }

    @Override
    public Object value(SpanData spanData) {
        return spanData.getOperationName();
    }

}

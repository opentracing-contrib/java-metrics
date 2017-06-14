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

import java.util.Map;

import io.opentracing.BaseSpan;
import io.opentracing.contrib.metrics.MetricLabel;

/**
 * This implementation returns a constant value for the metric label.
 *
 */
public class ConstMetricLabel implements MetricLabel {

    private final String name;
    private final Object value;

    public ConstMetricLabel(String name, Object value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public Object defaultValue() {
        return value;
    }

    @Override
    public Object value(BaseSpan<?> span, String operation, Map<String, Object> tags) {
        return value;
    }

    
}

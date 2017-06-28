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
 * This implementation attempts to obtain the metric label value from the span's
 * tags. If not available, it will use the default value.
 *
 */
public class TagMetricLabel implements MetricLabel {

    private final String name;
    private final Object defaultValue;

    public TagMetricLabel(String name, Object defaultValue) {
        this.name = name;
        this.defaultValue = defaultValue;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public Object defaultValue() {
        return defaultValue;
    }

    @Override
    public Object value(SpanData spanData) {
        Object ret = spanData.getTags().get(name());
        return ret == null ? defaultValue : ret;
    }

}

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
package io.opentracing.contrib.metrics;

import java.util.Map;

import io.opentracing.BaseSpan;

/**
 * This interface represents a label associated with a reported metric type. For each sampled
 * metric instance, it can be used to determine the name and value of the label.
 *
 */
public interface MetricLabel {

    /**
     * This method returns the name of the metric tag.
     *
     * @return The name
     */
    String name();

    /**
     * This method returns a default value for the specified
     * label, if one is defined, otherwise null.
     *
     * @return The default value, or null
     */
    Object defaultValue();

    /**
     * This method returns a metric tag value.
     *
     * @param span The span
     * @param operation The operation
     * @param tags The tags
     * @return The value, if null will suppress the metrics for the span being reported
     */
    Object value(BaseSpan<?> span, String operation, Map<String,Object> tags);

}

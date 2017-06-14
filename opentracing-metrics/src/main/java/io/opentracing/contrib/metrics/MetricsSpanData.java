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

import io.opentracing.SpanContext;

/**
 * This interface provides access to relevant data about a span, from which metrics can be
 * derived.
 *
 */
public interface MetricsSpanData {

    /**
     * @return The span context
     */
    SpanContext getSpanContext();

    /**
     * @return The span's operation name
     */
    String getOperationName();

    /**
     * @return The span tags
     */
    Map<String,Object> getTags();

    /**
     * @name key The key
     * @return The baggage item if found, or null
     */
    String getBaggageItem(String key);

    /**
     * @return The duration, in microseconds
     */
    long getDuration();

}

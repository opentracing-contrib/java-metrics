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

/**
 * This interface is used to notify when metrics associated with a finished span can be reported.
 *
 */
public interface MetricsReporter {

    /**
     * This method reports metrics based on the details associated with the supplied {@link SpanData}.
     *
     * @param metricSpanData Span data including operation, tags, baggage and duration
     */
    void reportSpan(SpanData metricSpanData);

}

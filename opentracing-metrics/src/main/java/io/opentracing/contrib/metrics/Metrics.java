/**
 * Copyright 2017-2018 The OpenTracing Authors
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

import java.util.Collections;
import java.util.Set;

import io.opentracing.Tracer;
import io.opentracing.contrib.api.tracer.APIExtensionsTracer;

/**
 * This is the main entry point into the metrics capability, enabling a {@link Tracer}
 * instance to be decorated with the metrics reporting functionality.
 *
 */
public class Metrics {

    /**
     * This method decorates a supplied tracer with the ability to report span
     * based metrics to the supplied {@link MetricsReporter}.
     *
     * @param tracer The tracer
     * @param reporter The metrics reporter
     * @return The decorated tracer
     */
    public static Tracer decorate(Tracer tracer, MetricsReporter reporter) {
        return decorate(tracer, Collections.singleton(reporter));
    }

    /**
     * This method decorates a supplied tracer with the ability to report span
     * based metrics to the supplied set of {@link MetricsReporter}.
     *
     * @param tracer The tracer
     * @param reporters The set of metric reporters
     * @return The decorated tracer
     */
    public static Tracer decorate(Tracer tracer, Set<MetricsReporter> reporters) {
        APIExtensionsTracer ret = new APIExtensionsTracer(tracer);
        ret.addTracerObserver(new MetricsObserver(reporters));
        return ret;
    }
}

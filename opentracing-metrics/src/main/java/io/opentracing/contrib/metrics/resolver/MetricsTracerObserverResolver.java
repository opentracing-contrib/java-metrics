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
package io.opentracing.contrib.metrics.resolver;

import java.util.HashSet;
import java.util.ServiceLoader;
import java.util.Set;

import io.opentracing.contrib.api.TracerObserver;
import io.opentracing.contrib.api.tracer.decorator.TracerObserverResolver;
import io.opentracing.contrib.metrics.MetricsObserver;
import io.opentracing.contrib.metrics.MetricsReporter;

/**
 * This class provides an implementation of the {@link TracerObserverResolver}
 * to return a {@link MetricsObserver} configured with any available {@link MetricsReporter}s.
 *
 */
public class MetricsTracerObserverResolver implements TracerObserverResolver {

    @Override
    public TracerObserver resolve() {
        Set<MetricsReporter> reporters = getMetricsReporters();
        if (!reporters.isEmpty()) {
            return new MetricsObserver(reporters);
        }
        return null;
    }

    protected Set<MetricsReporter> getMetricsReporters() {
        Set<MetricsReporter> reporters = new HashSet<MetricsReporter>();
        ServiceLoader<MetricsReporterResolver> resolvers = ServiceLoader.load(MetricsReporterResolver.class,
                Thread.currentThread().getContextClassLoader());
        for (MetricsReporterResolver resolver : resolvers) {
            Set<MetricsReporter> metricsReporters = resolver.resolve();
            if (metricsReporters != null && !metricsReporters.isEmpty()) {
                reporters.addAll(metricsReporters);
            }
        }
        return reporters;
    }

}

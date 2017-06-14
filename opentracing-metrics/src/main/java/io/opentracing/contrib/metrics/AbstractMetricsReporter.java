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

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import io.opentracing.contrib.metrics.MetricLabel;
import io.opentracing.contrib.metrics.MetricsReporter;
import io.opentracing.contrib.metrics.label.OperationMetricLabel;
import io.opentracing.contrib.metrics.label.TagMetricLabel;
import io.opentracing.tag.Tags;

/**
 * This class provides an abstract base class for the {@link MetricsReporter} interface.
 *
 */
public abstract class AbstractMetricsReporter implements MetricsReporter {

    protected final MetricLabel[] metricLabels;

    protected static final List<MetricLabel> STANDARD_SPAN_LABELS = Arrays.asList(
            new OperationMetricLabel(),
            new TagMetricLabel(Tags.SPAN_KIND.getKey(), null),
            new TagMetricLabel(Tags.ERROR.getKey(), Boolean.FALSE.toString()));

    protected AbstractMetricsReporter(List<MetricLabel> labels) {
        metricLabels = initLabels(labels);
    }

    /**
     * This method initializes the list of metric labels for use by the reporter. The list
     * needs to be initialized on instantiation as they must remain constant for all of the
     * metric types reported. The list of labels is constructed based on the supplied labels
     * as well as the standard labels.
     *
     * @param labels The list of additional and overridden label definitions
     * @return The full list of metric labels
     */
    private MetricLabel[] initLabels(List<MetricLabel> labels) {
        Map<String,MetricLabel> labelsByName = new LinkedHashMap<String,MetricLabel>();
        for (MetricLabel label : labels) {
            labelsByName.put(label.name(), label);
        }
        for (MetricLabel standardLabel : STANDARD_SPAN_LABELS) {
            // If label already exists, then reuse it - remove and re-add to maintain the position
            // within the standard labels
            MetricLabel existingLabel = labelsByName.remove(standardLabel.name());
            labelsByName.put(standardLabel.name(), existingLabel == null ? standardLabel : existingLabel);
        }
        return labelsByName.values().toArray(new MetricLabel[labelsByName.size()]);
    }

    /**
     * This method derives the values for the labels associated with the metrics reporter.
     *
     * @param metricsSpanData The metrics span data
     * @return The label values, or null if sample should not be reported
     */
    protected String[] getLabelValues(MetricsSpanData metricsSpanData) {
        String[] values = new String[metricLabels.length];
        for (int i=0; i < values.length; i++) {
            Object value = metricLabels[i].value(metricsSpanData);
            if (value == null) {
                // Don't report span as not all labels are specified
                // TODO: May need to provide debug log to help if metrics unexpectedly not reported
                return null;
            }
            values[i] = value.toString();
        }
        return values;
    }

}

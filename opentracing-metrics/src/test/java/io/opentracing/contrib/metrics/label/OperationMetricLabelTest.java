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

import static org.junit.Assert.assertEquals;

import java.util.Collections;

import org.junit.Test;

import io.opentracing.contrib.metrics.MetricLabel;
import io.opentracing.contrib.metrics.label.OperationMetricLabel;

public class OperationMetricLabelTest {

    @Test
    public void testOperationLabel() {
        MetricLabel label = new OperationMetricLabel();
        assertEquals("TestOperation", label.value(null, "TestOperation", Collections.<String,Object>emptyMap()));
     }

}

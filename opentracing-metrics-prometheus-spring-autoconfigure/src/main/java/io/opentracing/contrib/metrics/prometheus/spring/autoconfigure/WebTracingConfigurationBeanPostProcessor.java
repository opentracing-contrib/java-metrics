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
package io.opentracing.contrib.metrics.prometheus.spring.autoconfigure;

import java.util.regex.Pattern;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

import io.opentracing.contrib.spring.web.autoconfig.WebTracingConfiguration;

@Configuration
@ConditionalOnClass(value = {WebTracingConfiguration.class})
@ConditionalOnProperty(name="OPENTRACING_METRICS_EXPORTER_HTTP_PATH")
public class WebTracingConfigurationBeanPostProcessor implements BeanPostProcessor {

    @Value("${OPENTRACING_METRICS_EXPORTER_HTTP_PATH}")
    private String metricsPath;

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof WebTracingConfiguration) {
            WebTracingConfiguration config = (WebTracingConfiguration)bean;
            StringBuilder skip =
                    new StringBuilder((config).getSkipPattern().pattern())
                    .append('|')
                    .append(metricsPath);
            return config.toBuilder()
                    .withSkipPattern(Pattern.compile(skip.toString()))
                    .build();
        }
        return bean;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

}

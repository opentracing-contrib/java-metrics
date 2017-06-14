[![Build Status][ci-img]][ci] [![Released Version][maven-img]][maven]

# OpenTracing Metrics

The OpenTracing Metrics project enables any OpenTracing compliant Tracer to be decorated with support
for reporting span based application metrics.

The project currently has support for reporting metrics via:

* Prometheus

A _Tracer_ is decorated in the following way:

```java
	Tracer tracer = ...;
	MetricsReporter reporter = ...;
	Tracer metricsTracer = io.opentracing.contrib.metrics.Metrics.decorate(tracer, reporter);
```

## Metric Labels

Labels are used as a way to separate sampled metric values into related groups. A combination of label values will
uniquely define a specific metric.

If one of the metric labels returned for a particular sampling point (i.e. span) returns a `null` value, then the
metric will not be recorded. This provided a means to selective choose which metrics values are of interest.

For example, by default if the `span.kind` tag is not specified, it's label value will be `null`. This means that
metrics for internal spans will by default not be recorded. If an application/service developer wants specific internal
span metrics recorded, they can add a `MetricLabel` that returns an appropriate value for the `span.kind` for the
spans of interest.

### Label Types

Label types:

* ConstMetricLabel

When a label will have a constant value, e.g. service name

* OperationMetricLabel

The label tags the span's operation name

* TagBaggageMetricLabel

This label attempts to obtain the value associated with the requested name from the span tags, followed
by the span's baggage, and if not found uses a default value.


### Default Labels

By default, the metrics are reported using the following labels:

* operation - the operation associated with the span

* span.kind - the _span.kind_ tag associated with the span, by default if not specified, then the metrics
for the span will not be recorded

* error - the _error_ tag, by default the value will be _false_


### Adding Metric Labels

An application may want to add specific labels to help classify the metrics being reported by each
span.

For example, many `Tracer` implementations associate spans with a service name. This can also be achieved
for the span metrics, by specifying a `ConstMetricLabel` when creating the reporter.

### Customizing Metric Labels

When initializing the `MetricsReporter`, it would be possible to provide a `MetricLabel` for a default label,
to override its value.

For example, a `MetricLabel` implementation could be provided for the _error_ label, which could override the
standard boolean value and potentially provide an alternative set of values based on other tags or baggage
values associated with a span.


## Reporting Metrics with Prometheus

Prometheus metrics reporting is provided by a specific implementation of the _MetricsReporter_ interface.

For example,

```java
        PrometheusMetricsReporter reporter = PrometheusMetricsReporter.newMetricsReporter()
                .withCollectorRegistry(collectorRegistry)
                .withConstLabel("service", "...")
                .withTagLabel(Tags.HTTP_STATUS_CODE,"na")
                .build();
```

The builder can be used to directly specify a Prometheus _CollectorRegistry_ instance. If not provided, then
the default will be used.

Other methods are provided to enable new labels to be provided, or existing ones overridden.

The responsibility for identifying how Prometheus metrics are exposed is outside the scope of the _MetricsReporter_
allowing the application to decide whether to expose via HTTP endpoint, use the Push Gateway, etc. For example,
using the servlet with a Jetty server:

```java
	Server server = new Server(1234);
	ServletContextHandler context = new ServletContextHandler();
	context.setContextPath("/");
	server.setHandler(context);
	context.addServlet(new ServletHolder(new MetricsServlet()), "/metrics");
	server.start();
```

## Release
Follow instructions in [RELEASE](RELEASE.md)

   [ci-img]: https://travis-ci.org/opentracing-contrib/java-metrics.svg?branch=master
   [ci]: https://travis-ci.org/opentracing-contrib/java-metrics
   [maven-img]: https://img.shields.io/maven-central/v/io.opentracing.contrib/opentracing-metrics.svg?maxAge=2592000
   [maven]: http://search.maven.org/#search%7Cga%7C1%7Copentracing-metrics

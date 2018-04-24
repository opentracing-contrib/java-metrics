[![Build Status][ci-img]][ci] [![Released Version][maven-img]][maven]

# OpenTracing Metrics

The OpenTracing Metrics project enables any OpenTracing compliant Tracer to be decorated with support
for reporting span based application metrics.

The project currently has support for reporting metrics via:

* Micrometer
* Prometheus (deprecated in favor of Micrometer)

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

This label type returns a constant value, e.g. service name

* OperationMetricLabel

This label type returns the span's operation name

* TagMetricLabel

This label type attempts to obtain the value associated with the requested name from the span's tags,
and if not found uses a default value.

* BaggageMetricLabel

This label type attempts to obtain the value associated with the requested name from the span's baggage items,
and if not found uses a default value.


### Default Labels

By default, the metrics are reported using the following labels:

* operation - the operation associated with the span

* span.kind - the _span.kind_ tag associated with the span, by default if not specified, then the metrics
for the span will not be recorded

* error - the _error_ tag, by default the value will be _false_


### Adding Metric Labels

An application may want to add specific labels to help classify the metrics being reported for each
span.

For example, many `Tracer` implementations associate spans with a service name. This can also be achieved
for the span metrics, by specifying a `ConstMetricLabel` when creating the reporter.

### Customizing Metric Labels

When initializing the `MetricsReporter`, it would be possible to provide a `MetricLabel` for a default label,
to override its value.

For example, a `MetricLabel` implementation could be provided for the _error_ label, which could override the
standard boolean value and potentially provide an alternative set of values based on other tags or baggage
values associated with a span.

## Reporting Metrics

Micrometer metrics reporting is provided by a specific implementation of the _MetricsReporter_ interface.

For example,

```java

// Your application needs to setup a concrete Micrometer backend
io.micrometer.core.instrument.Metrics.addRegistry(new SimpleMeterRegistry());

// Prepare a concrete OpenTracing tracer
Tracer tracer = getTracer();

// A reporter can then be created like this:
MicrometerMetricsReporter reporter = MicrometerMetricsReporter.newMetricsReporter()
    .withName("MyName")
    .withConstLabel("span.kind", Tags.SPAN_KIND_CLIENT)
    .build();

// Wrap the concrete Tracer, so that we can record the metrics about the reported spans
Tracer metricsTracer = io.opentracing.contrib.metrics.Metrics.decorate(tracer, reporter);
```

Builder methods are provided to enable new labels to be provided, or existing ones overridden.

Refer to the Micrometer documentation on how to get the metrics into a concrete backend, such as JMX, StatsD or
Prometheus.

### Reporting metrics with a Prometheus backend

Auto-configuration for Spring Boot applications of a Prometheus backend is provided via the module
`opentracing-metrics-prometheus-spring-autoconfigure`. To auto-register an endpoint serving Prometheus metrics, export
the property `OPENTRACING_METRICS_EXPORTER_HTTP_PATH` with the path to be used - e.g. "/metrics".

### `TracerObserver` approach

Instead of decorating an OpenTracing tracer, it's also possible to combine the usage of Spring Boot's auto configuration
feature and the `TracerObserver` from
[`io.opentracing.contrib:opentracing-api-extensions-tracer`](https://github.com/opentracing-contrib/java-api-extensions).

Just include the artifact `io.opentracing.contrib:opentracing-metrics-spring-autoconfigure` into your Spring Boot
application and the `TracerObserver` will be registered automatically.


## Known Issues

### Only works with ActiveSpanSource implementations that don't require a tracer specific Span implementation

The current mechanism uses a wrapper tracer implementation to identify when a
[span has finished](https://github.com/opentracing/opentracing-java/issues/155). This requires
a wrapped `Span` to be passed to the `ActiveSpanSource.makeActive` method, and therefore will fail if the
`ActiveSpanSource` implementation has an expectation of receiving a particular `Span` implementation.

This wrapper approach is only being used as a short term workaround until a
[`TracerObserver`](https://github.com/opentracing/specification/issues/76) mechanism is available.


## Release
Follow instructions in [RELEASE](RELEASE.md)

   [ci-img]: https://travis-ci.org/opentracing-contrib/java-metrics.svg?branch=master
   [ci]: https://travis-ci.org/opentracing-contrib/java-metrics
   [maven-img]: https://img.shields.io/maven-central/v/io.opentracing.contrib/opentracing-metrics.svg?maxAge=2592000
   [maven]: http://search.maven.org/#search%7Cga%7C1%7Copentracing-metrics

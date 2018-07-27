package com.github.stefanbirkner.micrometer.jersey;

final class StatusCodeMetricsAnnotationChecker implements MetricsAnnotationChecker {
    @Override
    public boolean check(ResourceMetrics ann) {
        return ann.statusCodeCounter();
    }
}

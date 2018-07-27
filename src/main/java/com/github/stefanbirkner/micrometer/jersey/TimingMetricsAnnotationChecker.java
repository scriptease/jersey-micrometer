package com.github.stefanbirkner.micrometer.jersey;

class TimingMetricsAnnotationChecker implements MetricsAnnotationChecker {
    @Override
    public boolean check(ResourceMetrics ann) {
        return ann.timer();
    }
}

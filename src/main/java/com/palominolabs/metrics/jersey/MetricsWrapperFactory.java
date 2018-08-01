/*
 * Copyright (c) 2012 Palomino Labs, Inc.
 */

package com.palominolabs.metrics.jersey;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.inject.Inject;
import com.palominolabs.jersey.dispatchwrapper.ResourceMethodDispatchWrapper;
import com.palominolabs.jersey.dispatchwrapper.ResourceMethodDispatchWrapperChain;
import com.palominolabs.jersey.dispatchwrapper.ResourceMethodDispatchWrapperFactory;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.model.AbstractResourceMethod;

/**
 * Factory for dispatch wrappers that wrap request invocation to get timing info.
 */
final class MetricsWrapperFactory implements ResourceMethodDispatchWrapperFactory {

    private final JerseyMetricsConfig jerseyMetricsConfig;

    private final ResourceMetricNamer namer;
    private final MetricRegistry metricsRegistry;

    @Inject
    MetricsWrapperFactory(JerseyMetricsConfig jerseyMetricsConfig, ResourceMetricNamer namer,
        @JerseyResourceMetrics MetricRegistry metricsRegistry) {
        this.jerseyMetricsConfig = jerseyMetricsConfig;
        this.namer = namer;
        this.metricsRegistry = metricsRegistry;
    }

    @Override
    public ResourceMethodDispatchWrapper createDispatchWrapper(AbstractResourceMethod am) {
        EnabledState state = MetricAnnotationFeatureResolver.getState(am, new TimingMetricsAnnotationChecker());

        if (state == EnabledState.OFF ||
            (state == EnabledState.UNSPECIFIED && !jerseyMetricsConfig.isTimingEnabledByDefault())) {
            return null;
        }

        Class<?> resourceClass = am.getResource().getResourceClass();
        String metricId = namer.getMetricBaseName(am);
        Timer timer = metricsRegistry.timer(MetricRegistry.name(resourceClass, metricId + " timer"));
        return (resource, context, chain) -> {
            Timer.Context time = timer.time();
            try {
                chain.wrapDispatch(resource, context);
            } finally {
                time.stop();
            }
        };
    }
}

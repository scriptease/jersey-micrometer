/*
 * Copyright (c) 2012 Palomino Labs, Inc.
 */

package com.github.stefanbirkner.micrometer.jersey;

import com.google.inject.Inject;
import com.palominolabs.jersey.dispatchwrapper.ResourceMethodDispatchWrapper;
import com.palominolabs.jersey.dispatchwrapper.ResourceMethodDispatchWrapperFactory;
import com.sun.jersey.api.model.AbstractResourceMethod;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

/**
 * Factory for dispatch wrappers that wrap request invocation to get timing info.
 */
final class MicrometerWrapperFactory
    implements ResourceMethodDispatchWrapperFactory
{

    private final JerseyMicrometerConfig jerseyMicrometerConfig;

    private final ResourceMeterNamer namer;
    private final MeterRegistry meterRegistry;

    @Inject
    MicrometerWrapperFactory(JerseyMicrometerConfig jerseyMicrometerConfig, ResourceMeterNamer namer,
                             @JerseyResourceMicrometer MeterRegistry meterRegistry) {
        this.jerseyMicrometerConfig = jerseyMicrometerConfig;
        this.namer = namer;
        this.meterRegistry = meterRegistry;
    }

    @Override
    public ResourceMethodDispatchWrapper createDispatchWrapper(AbstractResourceMethod am) {
        EnabledState state = MetricAnnotationFeatureResolver.getState(am, new TimingMetricsAnnotationChecker());

        if (state == EnabledState.OFF ||
            (state == EnabledState.UNSPECIFIED && !jerseyMicrometerConfig.isTimingEnabledByDefault())) {
            return null;
        }

        Class<?> resourceClass = am.getResource().getResourceClass();
        String metricId = namer.getMeterBaseName(am);
        Timer timer = meterRegistry.timer(resourceClass.getName() + "." + metricId + " timer");
        return (resource, context, chain) -> timer.record(
            () -> chain.wrapDispatch(resource, context)
        );
    }
}

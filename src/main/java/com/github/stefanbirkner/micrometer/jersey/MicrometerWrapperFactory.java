/*
 * Copyright (c) 2012 Palomino Labs, Inc.
 */

package com.github.stefanbirkner.micrometer.jersey;

import com.google.inject.Inject;
import com.palominolabs.jersey.dispatchwrapper.ResourceMethodDispatchWrapper;
import com.palominolabs.jersey.dispatchwrapper.ResourceMethodDispatchWrapperFactory;
import com.sun.jersey.api.model.AbstractResourceMethod;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;

import java.lang.reflect.AnnotatedElement;
import java.util.function.Supplier;

import static java.lang.System.currentTimeMillis;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * Factory for dispatch wrappers that wrap request invocation to get timing info.
 */
final class MicrometerWrapperFactory
    implements ResourceMethodDispatchWrapperFactory
{

    private final JerseyMicrometerConfig jerseyMicrometerConfig;

    private final ResourceMeterNamer namer;
    private TagsProvider tagsProvider;
    private final MeterRegistry meterRegistry;

    @Inject
    MicrometerWrapperFactory(
        JerseyMicrometerConfig jerseyMicrometerConfig,
        ResourceMeterNamer namer,
        TagsProvider tagsProvider,
        @JerseyResourceMicrometer MeterRegistry meterRegistry
    ) {
        this.jerseyMicrometerConfig = jerseyMicrometerConfig;
        this.namer = namer;
        this.tagsProvider = tagsProvider;
        this.meterRegistry = meterRegistry;
    }

    @Override
    public ResourceMethodDispatchWrapper createDispatchWrapper(
        AbstractResourceMethod am
    ) {
        if (enabledForMethod(am)) {
            return recordStatistics(am);
        } else {
            return null; //don't wrap invocation
        }
    }

    private boolean enabledForMethod(
        AbstractResourceMethod method
    ) {
        return firstSpecified(
            () -> enabled(method),
            () -> enabled(method.getResource()),
            jerseyMicrometerConfig::isEnabledByDefault
        );
    }

    private boolean firstSpecified(
        Supplier<Boolean>... enabledCheckers
    ) {
        for (Supplier<Boolean> checker: enabledCheckers) {
            Boolean enabled = checker.get();
            if (enabled != null) {
                return enabled;
            }
        }

        throw new IllegalArgumentException(
            "None of the checkers provided a value for the enabled flag."
        );
    }

    private Boolean enabled(
        AnnotatedElement element
    ) {
        ResourceMetrics annotation = element.getAnnotation(
            ResourceMetrics.class
        );
        if (annotation == null) {
            return null;
        } else {
            return annotation.enabled();
        }
    }

    private ResourceMethodDispatchWrapper recordStatistics(
        AbstractResourceMethod method
    ) {
        Class<?> resourceClass = method.getResource().getResourceClass();
        String metricId = namer.getMeterBaseName(method);
        Tags tags = tagsProvider.tagsForMethod(method);
        return (resource, context, chain) -> {
            long start = currentTimeMillis();
            chain.wrapDispatch(resource, context);
            long duration = currentTimeMillis() - start;
            Timer timer = meterRegistry.timer(
                resourceClass.getName() + "." + metricId,
                tags.and(
                    "status",
                    Integer.toString(context.getResponse().getStatus())
                )
            );
            timer.record(duration, MILLISECONDS);
        };
    }
}

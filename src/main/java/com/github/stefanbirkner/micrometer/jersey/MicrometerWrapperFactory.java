/*
 * Copyright (c) 2012 Palomino Labs, Inc.
 */

package com.github.stefanbirkner.micrometer.jersey;

import com.google.inject.Inject;
import com.palominolabs.jersey.dispatchwrapper.ResourceMethodDispatchWrapper;
import com.palominolabs.jersey.dispatchwrapper.ResourceMethodDispatchWrapperFactory;
import com.sun.jersey.api.model.AbstractResourceMethod;
import com.sun.jersey.api.model.AbstractSubResourceMethod;
import com.sun.jersey.api.model.PathValue;
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
    private final Configuration configuration;
    private final MeterRegistry meterRegistry;

    @Inject
    MicrometerWrapperFactory(
        Configuration configuration,
        @JerseyResourceMicrometer MeterRegistry meterRegistry
    ) {
        this.configuration = configuration;
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
            configuration::isEnabledByDefault
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
        Tags tags = tagsForMethod(method);
        return (resource, context, chain) -> {
            long start = currentTimeMillis();
            chain.wrapDispatch(resource, context);
            int status = context.getResponse().getStatus();
            record(start, tags, status);
        };
    }

    private Tags tagsForMethod(
        AbstractResourceMethod method
    ) {
        return Tags.of(
            "method", method.getHttpMethod(),
            "uri", uri(method)
        );
    }

    private String uri(
        AbstractResourceMethod method
    ) {

        String metricId = getPathWithoutSurroundingSlashes(
            method.getResource().getPath()
        );

        if (!metricId.isEmpty()) {
            metricId = "/" + metricId;
        }

        if (method instanceof AbstractSubResourceMethod) {
            //if this is a sub resource, add on its path component
            AbstractSubResourceMethod asrm = (AbstractSubResourceMethod) method;
            metricId += "/" + getPathWithoutSurroundingSlashes(asrm.getPath());
        }

        if (metricId.isEmpty()) {
            metricId = "_no path_";
        }

        return metricId;
    }

    private String getPathWithoutSurroundingSlashes(
        PathValue pathValue
    ) {
        if (pathValue == null) {
            return "";
        }
        String value = pathValue.getValue();
        if (value.startsWith("/")) {
            value = value.substring(1);
        }
        if (value.endsWith("/")) {
            value = value.substring(0, value.length() - 1);
        }

        return value;
    }

    private void record(
        long start,
        Tags tags,
        int status
    ) {
        long duration = currentTimeMillis() - start;
        Timer timer = meterRegistry.timer(
            //We are using the same name like Spring Boot because it makes
            //it easier to build cross-application dashboards.
            "http.server.requests",
            tags.and(
                "status",
                Integer.toString(status)
            )
        );
        timer.record(duration, MILLISECONDS);
    }
}

/*
 * Copyright (c) 2012 Palomino Labs, Inc.
 */

package com.palominolabs.metrics.jersey;

import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseFilter;
import com.sun.jersey.spi.container.ResourceFilter;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

import javax.annotation.concurrent.ThreadSafe;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Filter that increments per-status-code counters.
 */
@ThreadSafe
final class HttpStatusCodeCounterResourceFilter implements ResourceFilter, ContainerResponseFilter {

    private final ConcurrentMap<Integer, Counter> counters = new ConcurrentHashMap<>();

    private final Class<?> resourceClass;

    private final String metricBaseName;

    private final MeterRegistry meterRegistry;

    HttpStatusCodeCounterResourceFilter(MeterRegistry meterRegistry, String metricBaseName, Class<?> resourceClass) {
        this.meterRegistry = meterRegistry;
        this.metricBaseName = metricBaseName;
        this.resourceClass = resourceClass;
    }

    @Override
    public ContainerRequestFilter getRequestFilter() {
        // don't filter requests
        return null;
    }

    @Override
    public ContainerResponseFilter getResponseFilter() {
        return this;
    }

    @Override
    public ContainerResponse filter(ContainerRequest request, ContainerResponse response) {
        Integer status = response.getStatus();

        Counter counter = counters.get(status);
        if (counter == null) {
            Counter potentiallyNewCounter = meterRegistry.counter(
                resourceClass.getName() + "." + metricBaseName + " " + status + " counter");
            Counter existingCounter = counters.putIfAbsent(status, potentiallyNewCounter);
            if (existingCounter != null) {
                // we lost the race to set that counter, but shouldn't create a duplicate since Metrics.newCounter will do the right thing
                counter = existingCounter;
            } else {
                counter = potentiallyNewCounter;
            }
        }

        counter.increment();

        return response;
    }
}

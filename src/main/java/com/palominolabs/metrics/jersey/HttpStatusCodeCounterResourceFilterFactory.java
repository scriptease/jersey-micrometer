/*
 * Copyright (c) 2012 Palomino Labs, Inc.
 */

package com.palominolabs.metrics.jersey;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sun.jersey.api.model.AbstractMethod;
import com.sun.jersey.api.model.AbstractResourceMethod;
import com.sun.jersey.api.model.AbstractSubResourceLocator;
import com.sun.jersey.spi.container.ResourceFilter;
import com.sun.jersey.spi.container.ResourceFilterFactory;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static java.util.Collections.singletonList;

@Singleton
public final class HttpStatusCodeCounterResourceFilterFactory implements ResourceFilterFactory {

    private static final Logger logger = LoggerFactory.getLogger(HttpStatusCodeCounterResourceFilterFactory.class);

    private final JerseyMicrometerConfig jerseyMicrometerConfig;

    private final ResourceMeterNamer namer;
    private final MeterRegistry meterRegistry;

    @Inject
    HttpStatusCodeCounterResourceFilterFactory(JerseyMicrometerConfig jerseyMicrometerConfig, ResourceMeterNamer namer,
        @JerseyResourceMicrometer MeterRegistry meterRegistry) {
        this.jerseyMicrometerConfig = jerseyMicrometerConfig;
        this.namer = namer;
        this.meterRegistry = meterRegistry;
    }

    @Override
    public List<ResourceFilter> create(AbstractMethod am) {

        // documented to only be AbstractSubResourceLocator, AbstractResourceMethod, or AbstractSubResourceMethod
        if (am instanceof AbstractSubResourceLocator) {
            // not actually invoked per request, nothing to do
            logger.debug("Ignoring AbstractSubResourceLocator " + am);
            return null;
        } else if (am instanceof AbstractResourceMethod) {

            EnabledState state = MetricAnnotationFeatureResolver
                .getState((AbstractResourceMethod) am, new StatusCodeMetricsAnnotationChecker());

            if (state == EnabledState.OFF ||
                (state == EnabledState.UNSPECIFIED && !jerseyMicrometerConfig.isStatusCodeCounterEnabledByDefault())) {
                return null;
            }

            String metricBaseName = namer.getMeterBaseName((AbstractResourceMethod) am);
            Class<?> resourceClass = am.getResource().getResourceClass();

            return singletonList(
                    new HttpStatusCodeCounterResourceFilter(meterRegistry, metricBaseName, resourceClass));
        } else {
            logger.warn("Got an unexpected instance of " + am.getClass().getName() + ": " + am);
            return null;
        }
    }
}

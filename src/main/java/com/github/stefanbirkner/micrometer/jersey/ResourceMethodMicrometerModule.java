/*
 * Copyright (c) 2012 Palomino Labs, Inc.
 */

package com.github.stefanbirkner.micrometer.jersey;

import com.google.inject.AbstractModule;
import com.palominolabs.config.ConfigModule;
import com.palominolabs.jersey.dispatchwrapper.ResourceMethodWrappedDispatchModule;

public final class ResourceMethodMicrometerModule extends AbstractModule{
    @Override
    protected void configure() {
        ConfigModule.bindConfigBean(binder(), JerseyMicrometerConfig.class);
        bind(ResourceMeterNamer.class).to(ResourceMeterNamerImpl.class);

        ResourceMethodWrappedDispatchModule.bindWrapperFactory(binder(), MicrometerWrapperFactory.class);
        bind(HttpStatusCodeCounterResourceFilterFactory.class);
    }
}

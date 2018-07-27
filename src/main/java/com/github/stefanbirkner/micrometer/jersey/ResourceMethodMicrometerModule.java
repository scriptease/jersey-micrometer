/*
 * Copyright (c) 2012 Palomino Labs, Inc.
 */

package com.github.stefanbirkner.micrometer.jersey;

import com.google.inject.AbstractModule;
import com.palominolabs.config.ConfigModule;

import static com.palominolabs.jersey.dispatchwrapper.ResourceMethodWrappedDispatchModule.bindWrapperFactory;

public final class ResourceMethodMicrometerModule
    extends AbstractModule
{
    @Override
    protected void configure() {
        ConfigModule.bindConfigBean(binder(), JerseyMicrometerConfig.class);
        bind(ResourceMeterNamer.class).to(ResourceMeterNamerImpl.class);
        bind(TagsProvider.class);

        bindWrapperFactory(binder(), MicrometerWrapperFactory.class);
        bind(HttpStatusCodeCounterResourceFilterFactory.class);
    }
}

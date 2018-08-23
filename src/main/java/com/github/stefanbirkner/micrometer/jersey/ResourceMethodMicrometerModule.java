package com.github.stefanbirkner.micrometer.jersey;

import com.google.inject.AbstractModule;

import static com.palominolabs.jersey.dispatchwrapper.ResourceMethodWrappedDispatchModule.bindWrapperFactory;

public final class ResourceMethodMicrometerModule
    extends AbstractModule
{
    @Override
    protected void configure() {
        bindWrapperFactory(binder(), MicrometerWrapperFactory.class);
    }
}

package com.github.stefanbirkner.micrometer.jersey;

import com.google.inject.Inject;
import com.sun.jersey.api.model.AbstractResourceMethod;
import io.micrometer.core.instrument.Tags;

class TagsProvider {
    private final ResourceMeterNamer meterNamer;

    @Inject
    TagsProvider(ResourceMeterNamer meterNamer) {
        this.meterNamer = meterNamer;
    }

    Tags tagsForMethod(AbstractResourceMethod method) {
        return Tags.of(
            "method", method.getHttpMethod(),
            "uri", meterNamer.getMeterBaseName(method)
        );
    }
}

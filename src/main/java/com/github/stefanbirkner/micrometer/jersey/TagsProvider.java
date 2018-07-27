package com.github.stefanbirkner.micrometer.jersey;

import com.sun.jersey.api.model.AbstractResourceMethod;
import io.micrometer.core.instrument.Tags;

class TagsProvider {
    Tags tagsForMethod(AbstractResourceMethod method) {
        return Tags.of("method", method.getHttpMethod());
    }
}

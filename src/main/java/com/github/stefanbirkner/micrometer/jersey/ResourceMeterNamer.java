/*
 * Copyright (c) 2012 Palomino Labs, Inc.
 */

package com.github.stefanbirkner.micrometer.jersey;

import com.sun.jersey.api.model.AbstractResourceMethod;

import javax.annotation.Nonnull;

public interface ResourceMeterNamer {

    /**
     * @param am resource method
     * @return a string name used as the prefix for all metrics about that resource method
     */
    @Nonnull
    String getMeterBaseName(
        AbstractResourceMethod am
    );
}

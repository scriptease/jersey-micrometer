/*
 * Copyright (c) 2012 Palomino Labs, Inc.
 */

package com.github.stefanbirkner.micrometer.jersey;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface ResourceMetrics {

    /**
     * @return true if timing and request count should be measured for the
     * annotated method (or all methods on the annotated class)
     */
    boolean enabled() default true;
}

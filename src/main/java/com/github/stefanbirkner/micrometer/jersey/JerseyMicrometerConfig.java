/*
 * Copyright (c) 2012 Palomino Labs, Inc.
 */

package com.github.stefanbirkner.micrometer.jersey;

import org.skife.config.Config;
import org.skife.config.Default;

public interface JerseyMicrometerConfig {

    /**
     * @return true if resource methods should have timer metrics captured by default
     */
    @Config("com.github.stefanbirkner.micrometer.jersey.resourceMethod.timer.enabledByDefault")
    @Default("true")
    boolean isTimingEnabledByDefault();

    /**
     * @return true if resource methods should have status code metrics captured by default
     */
    @Config("com.github.stefanbirkner.micrometer.jersey.resourceMethod.statusCodeCounter.enabledByDefault")
    @Default("true")
    boolean isStatusCodeCounterEnabledByDefault();
}

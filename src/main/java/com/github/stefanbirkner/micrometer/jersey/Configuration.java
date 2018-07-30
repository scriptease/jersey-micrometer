package com.github.stefanbirkner.micrometer.jersey;

/**
 * {@code Configuration} for {@link MicrometerWrapperFactory}.
 */
public class Configuration {
    private final boolean enabledByDefault;

    /**
     * Creates a {@code Configuration} where by default monitoring is enabled
     * for each resource method.
     * @see Configuration#disabledByDefault()
     */
    public Configuration() {
        this(true);
    }

    private Configuration(boolean enabledByDefault) {
        this.enabledByDefault = enabledByDefault;
    }

    /**
     * Returns {@code true} iff monitoring is enabled by default for each
     * resource method.
     * @return {@code true} iff monitoring is enabled by default for each
     * resource method.
     */
    boolean isEnabledByDefault() {
        return enabledByDefault;
    }

    /**
     * Creates a {@code Configuration} where by default monitoring is disabled
     * for each resource method.
     * @return a new {@code Configuration} instance.
     * @see Configuration#Configuration()
     */
    Configuration disabledByDefault() {
        return new Configuration(false);
    }
}

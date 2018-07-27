package com.github.stefanbirkner.micrometer.jersey;

import com.sun.jersey.api.model.AbstractResourceMethod;

final class MetricAnnotationFeatureResolver {

    /**
     * Check the method, then the class, for @ResourceMetrics and use the specified checker to see if the feature is
     * enabled or disabled.
     *
     * @param am      resource method
     * @return EnabledState.ON if feature enabled, EnabledState.OFF if feature disabled, EnabledState.UNSPECIFIED
     *         otherwise
     */
    static EnabledState getState(
        AbstractResourceMethod am
    ) {
        // check method, then class
        for (ResourceMetrics ann : new ResourceMetrics[]{am.getAnnotation(ResourceMetrics.class), am
            .getResource().getAnnotation(ResourceMetrics.class)}) {

            if (ann != null) {
                if (ann.enabled()) {
                    return EnabledState.ON;
                } else {
                    return EnabledState.OFF;
                }
            }
        }

        return EnabledState.UNSPECIFIED;
    }
}

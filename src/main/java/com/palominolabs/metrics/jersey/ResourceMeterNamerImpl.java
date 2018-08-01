/*
 * Copyright (c) 2012 Palomino Labs, Inc.
 */

package com.palominolabs.metrics.jersey;

import com.sun.jersey.api.model.AbstractResourceMethod;
import com.sun.jersey.api.model.AbstractSubResourceMethod;
import com.sun.jersey.api.model.PathValue;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

final class ResourceMeterNamerImpl implements ResourceMeterNamer {
    @Nonnull
    @Override
    public String getMeterBaseName(AbstractResourceMethod am) {

        String metricId = getPathWithoutSurroundingSlashes(am.getResource().getPath());

        if (!metricId.isEmpty()) {
            metricId = "/" + metricId;
        }

        String httpMethod;
        if (am instanceof AbstractSubResourceMethod) {
            // if this is a subresource, add on the subresource's path component
            AbstractSubResourceMethod asrm = (AbstractSubResourceMethod) am;
            metricId += "/" + getPathWithoutSurroundingSlashes(asrm.getPath());
            httpMethod = asrm.getHttpMethod();
        } else {
            httpMethod = am.getHttpMethod();
        }

        if (metricId.isEmpty()) {
            // this happens for WadlResource -- that case actually exists at "application.wadl" though
            metricId = "_no path_";
        }

        metricId += " " + httpMethod;

        return metricId;
    }

    @Nonnull
    static String getPathWithoutSurroundingSlashes(@Nullable PathValue pathValue) {
        if (pathValue == null) {
            return "";
        }
        String value = pathValue.getValue();
        if (value.startsWith("/")) {
            value = value.substring(1);
        }
        if (value.endsWith("/")) {
            value = value.substring(0, value.length() - 1);
        }

        return value;
    }
}

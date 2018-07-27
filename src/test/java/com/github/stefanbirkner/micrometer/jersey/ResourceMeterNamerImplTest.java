/*
 * Copyright (c) 2012 Palomino Labs, Inc.
 */

package com.github.stefanbirkner.micrometer.jersey;

import com.sun.jersey.api.model.AbstractResource;
import com.sun.jersey.api.model.AbstractResourceMethod;
import com.sun.jersey.api.model.AbstractSubResourceMethod;
import com.sun.jersey.api.model.PathValue;
import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.Path;
import java.lang.annotation.Annotation;

import static org.junit.Assert.assertEquals;

public final class ResourceMeterNamerImplTest {

    private final ResourceMeterNamer namer = new ResourceMeterNamerImpl();

    @Test
    public void testNullPathValue() {
        Assert.assertEquals("", ResourceMeterNamerImpl.getPathWithoutSurroundingSlashes(null));
    }

    @Test
    public void testPathValueLeadingSlash() {
        doPathValueTest("foo", "/foo");
    }

    @Test
    public void testPathValueTrailingSlash() {
        doPathValueTest("foo", "foo/");
    }

    @Test
    public void testPathValueLeadingAndTrailingSlash() {
        doPathValueTest("foo", "/foo/");
    }

    @Test
    public void testGetMetricIdClassWithPathMethodWithoutPath() {
        AbstractResource resource = new AbstractResource(FooResource.class, new PathValue("/res"));
        AbstractResourceMethod method =
            new AbstractResourceMethod(resource, null, Void.class, Void.class, "GET", new Annotation[]{});

        assertEquals("/res GET", namer.getMeterBaseName(method));
    }

    @Test
    public void testGetMetricIdClassWithPathMethodWithPath() {
        AbstractResource resource = new AbstractResource(FooResource.class, new PathValue("/res"));
        AbstractResourceMethod method =
            new AbstractSubResourceMethod(resource, null, Void.class, Void.class, new PathValue("/meth"), "GET",
                new Annotation[]{});

        assertEquals("/res/meth GET", namer.getMeterBaseName(method));
    }

    @Test
    public void testGetMetricIdClassWithoutPathMethodWithPath() {
        AbstractResource resource = new AbstractResource(FooResource.class, null);
        AbstractResourceMethod method =
            new AbstractSubResourceMethod(resource, null, Void.class, Void.class, new PathValue("/meth"), "GET",
                new Annotation[]{});

        assertEquals("/meth GET", namer.getMeterBaseName(method));
    }

    private static void doPathValueTest(String expected, String input) {
        Assert.assertEquals(expected, ResourceMeterNamerImpl.getPathWithoutSurroundingSlashes(new PathValue(input)));
    }

    @Path("/foo")
    private static class FooResource {

    }
}

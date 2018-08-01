package com.github.stefanbirkner.micrometer.jersey;

import com.sun.jersey.api.model.AbstractResource;
import com.sun.jersey.api.model.AbstractResourceMethod;
import org.junit.Test;

import javax.ws.rs.Path;
import java.lang.annotation.Annotation;

import static org.junit.Assert.assertEquals;

public class MetricAnnotationFeatureResolverTest {

    @Test
    public void testGetStateDefault() {
        doMethodStateTest(EnabledState.UNSPECIFIED, FooResource.class, new Annotation[0]);
    }

    @Test
    public void testGetStateMethodEnabled() {
        doMethodStateTest(EnabledState.ON, FooResource.class, new Annotation[]{new ResourceMetricsImpl(true)});
    }

    @Test
    public void testGetStateMethodDisabled() {
        doMethodStateTest(EnabledState.OFF, FooResource.class, new Annotation[]{new ResourceMetricsImpl(false)});
    }

    @Test
    public void testGetStateMethodEnabledClassDisabled() {
        doMethodStateTest(EnabledState.ON, FooResourceDisabled.class, new Annotation[]{new ResourceMetricsImpl(true)});
    }

    @Test
    public void testGetStateMethodDisabledClassEnabled() {
        doMethodStateTest(EnabledState.OFF, FooResourceEnabled.class, new Annotation[]{new ResourceMetricsImpl(false)});
    }

    @Test
    public void testGetStateClassEnabled() {
        doMethodStateTest(EnabledState.ON, FooResourceEnabled.class, new Annotation[]{});
    }

    @Test
    public void testGetStateClassDisabled() {
        doMethodStateTest(EnabledState.OFF, FooResourceDisabled.class, new Annotation[]{});
    }

    private static AbstractResourceMethod getAbstractMethod(
        Class<?> resourceClass,
        Annotation[] methodAnnotations
    ) {
        return new AbstractResourceMethod(new AbstractResource(resourceClass), null, Void.class, Void.class, "GET",
            methodAnnotations);
    }

    private static void doMethodStateTest(
        EnabledState expected,
        Class<?> resourceClass,
        Annotation[] methodAnnotations
    ) {
        assertEquals(expected,
            MetricAnnotationFeatureResolver
                .getState(getAbstractMethod(resourceClass, methodAnnotations)));
    }

    @Path("/foo")
    private static class FooResource {

    }

    @ResourceMetrics(enabled = false)
    private static class FooResourceDisabled {

    }

    @ResourceMetrics
    private static class FooResourceEnabled {

    }

    @SuppressWarnings("ClassExplicitlyAnnotation")
    private static class ResourceMetricsImpl implements ResourceMetrics {

        private final boolean timing;

        private ResourceMetricsImpl(
            boolean timing
        ) {
            this.timing = timing;
        }

        @Override
        public boolean enabled() {
            return timing;
        }

        @Override
        public Class<? extends Annotation> annotationType() {
            return ResourceMetrics.class;
        }
    }
}

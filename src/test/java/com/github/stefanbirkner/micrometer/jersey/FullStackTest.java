package com.github.stefanbirkner.micrometer.jersey;

import com.google.inject.AbstractModule;
import com.google.inject.servlet.GuiceFilter;
import com.google.inject.servlet.ServletModule;
import com.ning.http.client.AsyncHttpClient;
import com.palominolabs.config.ConfigModuleBuilder;
import com.palominolabs.jersey.dispatchwrapper.ResourceMethodWrappedDispatchModule;
import com.sun.jersey.guice.JerseyServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.junit.Test;

import javax.servlet.DispatcherType;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;

import static com.google.inject.Guice.createInjector;
import static java.util.Collections.singleton;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.stream.Collectors.toSet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FullStackTest {

    private static final int PORT = 18080;
    private final SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();

    @Test
    public void request_is_measured_when_enabled_on_class(
    ) throws Exception {
        Server server = startServer(EnabledOnClass.class);
        try {
            sendGetRequest("http://localhost:" + PORT + "/enabledOnClass");

            Set<String> meterNames = meterRegistry.getMeters()
                .stream()
                .map(Meter::getId)
                .map(Meter.Id::getName)
                .collect(toSet());

            assertEquals(
                singleton(
                    "com.github.stefanbirkner.micrometer.jersey.FullStackTest$EnabledOnClass./enabledOnClass"
                ),
                meterNames
            );

            Timer timer = meterRegistry.timer(
                "com.github.stefanbirkner.micrometer.jersey.FullStackTest$EnabledOnClass./enabledOnClass",
                "method", "GET",
                "status", "200");
            assertEquals(1, timer.count());
            assertTrue(timer.totalTime(MILLISECONDS) > 0D);
        } finally {
            server.stop();
        }
    }

    @Test
    public void request_is_not_measured_when_disabled_on_class(
    ) throws Exception {
        Server server = startServer(DisabledOnClass.class);
        try {
            sendGetRequest("http://localhost:" + PORT + "/disabledOnClass");

            assertNothingMeasured();
        } finally {
            server.stop();
        }
    }

    @Test
    public void request_is_not_measured_when_enabled_on_class_but_disabled_on_method(
    ) throws Exception {
        Server server = startServer(EnabledOnClassDisabledOnMethod.class);
        try {
            sendGetRequest("http://localhost:" + PORT + "/enabledOnClassDisabledOnMethod");

            assertNothingMeasured();
        } finally {
            server.stop();
        }
    }

    private Server startServer(
        Class<?> resource
    ) throws Exception {
        AbstractModule module = createModule(resource);
        GuiceFilter guiceFilter = createInjector(module)
            .getInstance(GuiceFilter.class);
        Server server = createServer(guiceFilter);
        server.start();
        return server;
    }

    private AbstractModule createModule(Class<?> resource) {
        return new AbstractModule() {
            @Override
            protected void configure() {
                binder().requireExplicitBindings();
                install(new ResourceMethodWrappedDispatchModule());
                install(new ServletModule() {
                    @Override
                    protected void configureServlets() {
                        serve("/*").with(GuiceContainer.class);
                    }
                });
                install(new JerseyServletModule());
                bind(GuiceFilter.class);
                bind(GuiceContainer.class);
                bind(resource);

                install(new ConfigModuleBuilder().build());
                install(new ResourceMethodMicrometerModule());
                bind(MeterRegistry.class).annotatedWith(JerseyResourceMicrometer.class).toInstance(meterRegistry);
            }
        };
    }

    private Server createServer(
        GuiceFilter filter
    ) {
        Server server = new Server(PORT);
        ServletContextHandler servletHandler = new ServletContextHandler();

        servletHandler.addServlet(new ServletHolder(new HttpServlet() {
            @Override
            protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.setContentType("text/plain");
                resp.setContentType("UTF-8");
                resp.getWriter().append("404");
            }
        }), "/*");

        // add guice servlet filter
        servletHandler.addFilter(new FilterHolder(filter), "/*", EnumSet.allOf(DispatcherType.class));

        server.setHandler(servletHandler);

        return server;
    }

    private void sendGetRequest(
        String url
    ) throws IOException, ExecutionException, InterruptedException {
        new AsyncHttpClient()
            .prepareGet(url)
            .execute()
            .get();
    }

    private void assertNothingMeasured() {
        assertEquals(
            0,
            meterRegistry.getMeters().size()
        );
    }

    @Path("enabledOnClass")
    @ResourceMetrics
    public static class EnabledOnClass {
        @GET
        public String get() throws Exception {
            Thread.sleep(10);
            return "ok";
        }
    }

    @Path("disabledOnClass")
    @ResourceMetrics(enabled = false)
    public static class DisabledOnClass {
        @GET
        public String get() {
            return "ok";
        }
    }

    @Path("enabledOnClassDisabledOnMethod")
    @ResourceMetrics
    public static class EnabledOnClassDisabledOnMethod {
        @GET
        @ResourceMetrics(enabled = false)
        public String get() {
            return "ok";
        }
    }
}

package com.github.stefanbirkner.micrometer.jersey;

import com.google.inject.AbstractModule;
import com.google.inject.servlet.GuiceFilter;
import com.google.inject.servlet.ServletModule;
import com.palominolabs.config.ConfigModule;
import com.palominolabs.config.ConfigModuleBuilder;
import com.palominolabs.jersey.dispatchwrapper.ResourceMethodWrappedDispatchModule;
import com.sun.jersey.guice.JerseyServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.search.RequiredSearch;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.apache.commons.configuration.MapConfiguration;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import javax.servlet.DispatcherType;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URL;
import java.util.*;

import static com.google.inject.Guice.createInjector;
import static java.util.Collections.singletonMap;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(Enclosed.class)
public class FullStackTest {

    private static final int PORT = 18080;

    public static class about_measurement {
        private final SimpleMeterRegistry registry = new SimpleMeterRegistry();

        @Test
        public void measurement_has_fixed_named(
        ) throws Exception {
            Server server = startServer();
            try {
                sendGetRequest();

                assertEquals(
                    "http.server.requests",
                    getTimer().getId().getName()
                );
            } finally {
                server.stop();
            }
        }

        @Test
        public void measurement_count_is_equal_to_number_of_requests(
        ) throws Exception {
            Server server = startServer();
            try {
                sendGetRequest();
                sendGetRequest();

                assertEquals(
                    2,
                    getTimer().count()
                );
            } finally {
                server.stop();
            }
        }

        @Test
        public void measurement_time_is_greater_than_zero(
        ) throws Exception {
            Server server = startServer();
            try {
                sendGetRequest();

                assertTrue(
                    getTimer().totalTime(MILLISECONDS) > 0d
                );
            } finally {
                server.stop();
            }
        }

        @Test
        public void measurement_is_tagged_with_HTTP_method(
        ) throws Exception {
            Server server = startServer();
            try {
                sendGetRequest();

                assertMeasurementPresentWithTag("method", "GET");
            } finally {
                server.stop();
            }
        }

        @Test
        public void measurement_is_tagged_with_status(
        ) throws Exception {
            Server server = startServer();
            try {
                sendGetRequest();

                assertMeasurementPresentWithTag("status", "200");
            } finally {
                server.stop();
            }
        }

        @Test
        public void measurement_is_tagged_with_path(
        ) throws Exception {
            Server server = startServer();
            try {
                sendGetRequest();

                assertMeasurementPresentWithTag(
                    "uri",
                    "/no-class-annotation/no-method-annotation"
                );
            } finally {
                server.stop();
            }
        }

        private Server startServer(
        ) throws Exception {
            return FullStackTest.startServer(
                registry
            );
        }


        private void sendGetRequest(
        ) throws IOException {
            FullStackTest.sendGetRequest(
                "/no-class-annotation/no-method-annotation"
            );
        }

        private Timer getTimer() {
            return (Timer) registry.getMeters().get(0);
        }

        private void assertMeasurementPresentWithTag(String key, String value) {
            RequiredSearch.in(registry)
                .tag(key, value)
                .meter(); //throws an exception if no matching meter is present
        }
    }

    public static class measurement_enabled_by_default {
        private final SimpleMeterRegistry registry = new SimpleMeterRegistry();

        @Test
        public void request_is_measured_when_no_annotation_is_present(
        ) throws Exception {
            Server server = startServer();
            try {
                sendGetRequest("/no-class-annotation/no-method-annotation");

                assertSingleMeasurement(registry);
            } finally {
                server.stop();
            }
        }

        @Test
        public void request_is_measured_when_enabled_on_class(
        ) throws Exception {
            Server server = startServer();
            try {
                sendGetRequest("/enabled-on-class/no-method-annotation");

                assertSingleMeasurement(registry);
            } finally {
                server.stop();
            }
        }

        @Test
        public void request_is_not_measured_when_disabled_on_class(
        ) throws Exception {
            Server server = startServer();
            try {
                sendGetRequest("/disabled-on-class/no-method-annotation");

                assertNoMeasurement(registry);
            } finally {
                server.stop();
            }
        }

        @Test
        public void request_is_measured_when_enabled_on_method(
        ) throws Exception {
            Server server = startServer();
            try {
                sendGetRequest("/no-class-annotation/enabled-on-method");

                assertSingleMeasurement(registry);
            } finally {
                server.stop();
            }
        }

        @Test
        public void request_is_not_measured_when_disabled_on_method(
        ) throws Exception {
            Server server = startServer();
            try {
                sendGetRequest("/no-class-annotation/disabled-on-method");

                assertNoMeasurement(registry);
            } finally {
                server.stop();
            }
        }

        @Test
        public void request_is_measured_when_enabled_on_class_and_method(
        ) throws Exception {
            Server server = startServer();
            try {
                sendGetRequest("/enabled-on-class/enabled-on-method");

                assertSingleMeasurement(registry);
            } finally {
                server.stop();
            }
        }

        @Test
        public void request_is_not_measured_when_enabled_on_class_but_disabled_on_method(
        ) throws Exception {
            Server server = startServer();
            try {
                sendGetRequest("/enabled-on-class/disabled-on-method");

                assertNoMeasurement(registry);
            } finally {
                server.stop();
            }
        }

        @Test
        public void request_is_measured_when_disabled_on_class_but_enabled_on_method(
        ) throws Exception {
            Server server = startServer();
            try {
                sendGetRequest("/disabled-on-class/enabled-on-method");

                assertSingleMeasurement(registry);
            } finally {
                server.stop();
            }
        }

        @Test
        public void request_is_not_measured_when_disabled_on_class_and_method(
        ) throws Exception {
            Server server = startServer();
            try {
                sendGetRequest("/disabled-on-class/disabled-on-method");

                assertNoMeasurement(registry);
            } finally {
                server.stop();
            }
        }

        private Server startServer(
        ) throws Exception {
            return FullStackTest.startServer(registry);
        }
    }

    public static class measurement_disabled_by_default {
        private final SimpleMeterRegistry registry = new SimpleMeterRegistry();

        @Test
        public void request_is_not_measured_when_no_annotation_is_present(
        ) throws Exception {
            Server server = startServer();
            try {
                sendGetRequest("/no-class-annotation/no-method-annotation");

                assertNoMeasurement(registry);
            } finally {
                server.stop();
            }
        }

        @Test
        public void request_is_measured_when_enabled_on_class(
        ) throws Exception {
            Server server = startServer();
            try {
                sendGetRequest("/enabled-on-class/no-method-annotation");

                assertSingleMeasurement(registry);
            } finally {
                server.stop();
            }
        }

        @Test
        public void request_is_not_measured_when_disabled_on_class(
        ) throws Exception {
            Server server = startServer();
            try {
                sendGetRequest("/disabled-on-class/no-method-annotation");

                assertNoMeasurement(registry);
            } finally {
                server.stop();
            }
        }

        @Test
        public void request_is_measured_when_enabled_on_method(
        ) throws Exception {
            Server server = startServer();
            try {
                sendGetRequest("/no-class-annotation/enabled-on-method");

                assertSingleMeasurement(registry);
            } finally {
                server.stop();
            }
        }

        @Test
        public void request_is_not_measured_when_disabled_on_method(
        ) throws Exception {
            Server server = startServer();
            try {
                sendGetRequest("/no-class-annotation/disabled-on-method");

                assertNoMeasurement(registry);
            } finally {
                server.stop();
            }
        }

        @Test
        public void request_is_measured_when_enabled_on_class_and_method(
        ) throws Exception {
            Server server = startServer();
            try {
                sendGetRequest("/enabled-on-class/enabled-on-method");

                assertSingleMeasurement(registry);
            } finally {
                server.stop();
            }
        }

        @Test
        public void request_is_not_measured_when_enabled_on_class_but_disabled_on_method(
        ) throws Exception {
            Server server = startServer();
            try {
                sendGetRequest("/enabled-on-class/disabled-on-method");

                assertNoMeasurement(registry);
            } finally {
                server.stop();
            }
        }

        @Test
        public void request_is_measured_when_disabled_on_class_but_enabled_on_method(
        ) throws Exception {
            Server server = startServer();
            try {
                sendGetRequest("/disabled-on-class/enabled-on-method");

                assertSingleMeasurement(registry);
            } finally {
                server.stop();
            }
        }

        @Test
        public void request_is_not_measured_when_disabled_on_class_and_method(
        ) throws Exception {
            Server server = startServer();
            try {
                sendGetRequest("/disabled-on-class/disabled-on-method");

                assertNoMeasurement(registry);
            } finally {
                server.stop();
            }
        }

        private Server startServer(
        ) throws Exception {
            Map<String, Object> config = singletonMap(
                "com.github.stefanbirkner.micrometer.jersey.resourceMethod.enabledByDefault",
                "false"
            );
            ConfigModule configModule = new ConfigModuleBuilder()
                .addConfiguration(new MapConfiguration(config))
                .build();

            AbstractModule module = createModule(configModule, registry);
            return startServerWithModule(module);
        }
    }

    private static Server startServer(
        MeterRegistry registry
    ) throws Exception {
        AbstractModule module = createModule(registry);
        return startServerWithModule(module);
    }

    private static Server startServerWithModule(
        AbstractModule module
    ) throws Exception {
        GuiceFilter guiceFilter = createInjector(module)
            .getInstance(GuiceFilter.class);
        Server server = createServer(guiceFilter);
        server.start();
        return server;
    }

    private static AbstractModule createModule(
        MeterRegistry registry
    ) {
        return createModule(
            new ConfigModuleBuilder().build(),
            registry
        );
    }

    private static AbstractModule createModule(
        ConfigModule configModule,
        MeterRegistry registry
    ) {
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
                bind(DisabledOnClass.class);
                bind(EnabledOnClass.class);
                bind(NoAnnotationOnClass.class);

                install(configModule);
                install(new ResourceMethodMicrometerModule());
                bind(MeterRegistry.class)
                    .annotatedWith(JerseyResourceMicrometer.class)
                    .toInstance(registry);
            }
        };
    }

    private static Server createServer(
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

    private static void sendGetRequest(
        String path
    ) throws IOException {
        new URL("http://localhost:" + PORT + path)
            .openStream()
            .close();
    }

    private static void assertNoMeasurement(
        MeterRegistry registry
    ) {
        assertEquals(
            0,
            registry.getMeters().size()
        );
    }

    private static void assertSingleMeasurement(
        MeterRegistry registry
    ) {
        assertEquals(
            1,
            registry.getMeters().size()
        );
    }
}

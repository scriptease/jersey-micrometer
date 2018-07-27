package com.github.stefanbirkner.micrometer.jersey;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("no-class-annotation")
public class NoAnnotationOnClass {
    @GET
    @Path("disabled-on-method")
    @ResourceMetrics(enabled = false)
    public String disabledOnMethod() {
        return "ok";
    }

    @GET
    @Path("enabled-on-method")
    @ResourceMetrics
    public String enabledOnMethod() {
        return "ok";
    }

    @GET
    @Path("no-method-annotation")
    public String noMethodAnnotation(
    ) throws InterruptedException {
        Thread.sleep(10);
        return "ok";
    }
}

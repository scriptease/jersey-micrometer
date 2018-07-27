package com.github.stefanbirkner.micrometer.jersey;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("disabled-on-class")
@ResourceMetrics(enabled = false)
public class DisabledOnClass {
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
    public String noMethodAnnotation() {
        return "ok";
    }
}

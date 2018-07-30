package com.github.stefanbirkner.micrometer.jersey;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

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

    @GET
    @Path("with-trailing-slash/")
    @ResourceMetrics
    public String withTailingSlash() {
        return "ok";
    }

    @GET
    @Path("with/{parameter}")
    @ResourceMetrics
    public String withTailingSlash(
        @PathParam("parameter") String parameter
    ) {
        return "ok";
    }

}

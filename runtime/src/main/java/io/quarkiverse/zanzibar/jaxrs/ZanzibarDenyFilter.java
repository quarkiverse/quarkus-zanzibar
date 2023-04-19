package io.quarkiverse.zanzibar.jaxrs;

import java.io.IOException;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;

public class ZanzibarDenyFilter implements ContainerRequestFilter {

    public static final ZanzibarDenyFilter INSTANCE = new ZanzibarDenyFilter();

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        ZanzibarAuthorizationFilter.log.debug("Denying access to unannotated resource method");
        requestContext.abortWith(Response.status(Response.Status.FORBIDDEN).build());
    }
}

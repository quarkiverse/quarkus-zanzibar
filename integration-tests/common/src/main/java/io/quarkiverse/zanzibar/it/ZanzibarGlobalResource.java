package io.quarkiverse.zanzibar.it;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

import io.quarkus.security.PermissionsAllowed;

@Path("/zanzibar-global")
@ApplicationScoped
public class ZanzibarGlobalResource {

    @GET
    @Path("things/{id}")
    @PermissionsAllowed(value = "reader", permission = GlobalThingPermission.class, params = "id")
    public String getThing(@PathParam("id") String id) {
        return "Thing " + id;
    }
}

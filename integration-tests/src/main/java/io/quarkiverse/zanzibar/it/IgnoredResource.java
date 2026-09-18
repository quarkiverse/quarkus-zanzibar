package io.quarkiverse.zanzibar.it;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import io.quarkiverse.zanzibar.annotations.FGAIgnore;

/** Resource whose endpoints explicitly skip FGA authorization. */
@Path("/ignored")
@ApplicationScoped
@FGAIgnore
public class IgnoredResource {

    /** Returns a response without requiring an FGA relationship. */
    @GET
    public String hello() {
        return "Ignored class";
    }
}

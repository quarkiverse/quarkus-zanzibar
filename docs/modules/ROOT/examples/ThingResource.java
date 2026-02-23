import java.security.Principal;
import java.util.List;
import java.util.Optional;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;

import io.quarkiverse.zanzibar.Relationship;
import io.quarkiverse.zanzibar.RelationshipManager;
import io.quarkiverse.zanzibar.ZanzibarPermission;
import io.quarkus.security.PermissionsAllowed;
import io.smallrye.mutiny.Uni;

@Path("/things")
class ThingResource {

    @Inject
    ThingRepository thingRepository;
    @Inject // <1>
    RelationshipManager relationshipManager;
    @Inject // <2>
    Principal principal;

    @POST
    public Uni<Thing> createThing(@QueryParam("name") String name) {
        return thingRepository.createThing(name)
                .flatMap((thing) -> {
                    // <3>
                    var relationship = new Relationship("thing", thing.getId(), "owner", "user", principal.getName());
                    // <4>
                    return relationshipManager.add(List.of(relationship))
                            .map((unused) -> thing);
                });
    }

    @GET
    @Path("{id}")
    @PermissionsAllowed(value = "owner", permission = ThingPermission.class) // <5>
    public Uni<Thing> getThing(String id) {
        return thingRepository.fetchThing(id);
    }
}

final class ThingPermission extends ZanzibarPermission {

    private final String id;

    ThingPermission(String name, String id) {
        super(name);
        this.id = id;
    }

    @Override
    public String getObjectType() { // <6>
        return "thing";
    }

    @Override
    public String getObjectId() { // <7>
        return id;
    }

    @Override
    public Optional<String> getUserType() { // <8>
        return Optional.of("user");
    }
}

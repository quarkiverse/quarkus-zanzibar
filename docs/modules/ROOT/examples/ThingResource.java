import java.security.Principal;
import java.util.List;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;

import io.quarkiverse.zanzibar.Relationship;
import io.quarkiverse.zanzibar.RelationshipManager;
import io.quarkiverse.zanzibar.annotations.FGAPathObject;
import io.quarkiverse.zanzibar.annotations.FGARelation;
import io.quarkiverse.zanzibar.annotations.FGAUserType;
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
    @FGARelation(FGARelation.ANY) // <3>
    public Uni<Thing> createThing(@QueryParam("name") String name) {
        return thingRepository.createThing(name)
                .flatMap((thing) -> {
                    // <4>
                    var relationship = new Relationship("thing", thing.getId(), "owner", "user:" + principal.getName());
                    // <5>
                    return relationshipManager.add(List.of(relationship))
                            .map((unused) -> thing);
                });
    }

    @GET
    @Path("{id}")
    @FGAPathObject(param = "id", type = "thing") // <6>
    @FGARelation("owner") // <7>
    @FGAUserType("user") // <8>
    public Uni<Thing> getThing(String id) {
        return thingRepository.fetchThing(id);
    }
}

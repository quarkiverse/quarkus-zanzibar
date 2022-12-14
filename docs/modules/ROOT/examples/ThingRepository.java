import io.smallrye.mutiny.Uni;

public interface ThingRepository {

    Uni<Thing> createThing(String name);

    Uni<Thing> fetchThing(String id);

}

package io.quarkiverse.zanzibar.openfga;

import static java.lang.String.format;

import java.util.List;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import io.quarkiverse.openfga.client.AuthorizationModelClient;
import io.quarkiverse.openfga.client.model.TupleKey;
import io.quarkiverse.zanzibar.Relationship;
import io.quarkiverse.zanzibar.RelationshipManager;
import io.smallrye.mutiny.Uni;

@ApplicationScoped
public class ZanzibarOpenFGARelationshipManager implements RelationshipManager {

    private final AuthorizationModelClient authorizationModelClient;

    @Inject
    public ZanzibarOpenFGARelationshipManager(AuthorizationModelClient authorizationModelClient) {
        this.authorizationModelClient = authorizationModelClient;
    }

    public Uni<Boolean> check(Relationship relationship) {

        return authorizationModelClient.check(tupleKeyFromRelationship(relationship), null);
    }

    @Override
    public Uni<Void> add(List<Relationship> relationships) {

        var tuples = relationships.stream()
                .map(this::tupleKeyFromRelationship)
                .collect(Collectors.toList());

        return authorizationModelClient.write(tuples, null)
                .replaceWithVoid();
    }

    @Override
    public Uni<Void> remove(List<Relationship> relationships) {

        var tuples = relationships.stream()
                .map(this::tupleKeyFromRelationship)
                .collect(Collectors.toList());

        return authorizationModelClient.write(null, tuples)
                .replaceWithVoid();
    }

    String object(String objectType, String objectId) {
        return format("%s:%s", objectType, objectId);
    }

    TupleKey tupleKeyFromRelationship(Relationship relationship) {
        return TupleKey.of(
                object(relationship.getObjectType(), relationship.getObjectId()),
                relationship.getRelation(),
                relationship.getUser());
    }
}

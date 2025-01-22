package io.quarkiverse.zanzibar.openfga;

import java.util.List;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import io.quarkiverse.openfga.client.AuthorizationModelClient;
import io.quarkiverse.openfga.client.model.RelObject;
import io.quarkiverse.openfga.client.model.RelTupleDefinition;
import io.quarkiverse.openfga.client.model.RelTupleKey;
import io.quarkiverse.openfga.client.model.RelUser;
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

        var relTupleKey = tupleKeyFromRelationship(relationship);

        return authorizationModelClient.check(relTupleKey);
    }

    @Override
    public Uni<Void> add(List<Relationship> relationships) {

        var tuples = relationships.stream()
                .map(this::tupleDefinitionFromRelationship)
                .toList();

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

    RelTupleKey tupleKeyFromRelationship(Relationship relationship) {
        return RelTupleKey.builder()
                .object(RelObject.of(relationship.objectType(), relationship.objectId()))
                .relation(relationship.relation())
                .user(RelUser.of(relationship.userType(), relationship.userId()))
                .build();
    }

    RelTupleDefinition tupleDefinitionFromRelationship(Relationship relationship) {
        return RelTupleDefinition.builder()
                .object(RelObject.of(relationship.objectType(), relationship.objectId()))
                .relation(relationship.relation())
                .user(RelUser.of(relationship.userType(), relationship.userId()))
                .build();
    }
}

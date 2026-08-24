package io.quarkiverse.zanzibar.openfga;

import static io.quarkiverse.openfga.client.model.WriteConflictBehavior.IGNORE;

import java.util.List;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import io.quarkiverse.openfga.client.AuthorizationModelClient;
import io.quarkiverse.openfga.client.AuthorizationModelClient.CheckOptions;
import io.quarkiverse.openfga.client.AuthorizationModelClient.WriteOptions;
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
    private final OpenFGAContextSupplier contextSupplier;

    @Inject
    public ZanzibarOpenFGARelationshipManager(AuthorizationModelClient authorizationModelClient,
            Instance<OpenFGAContextSupplier> contextSuppliers) {
        this(authorizationModelClient, contextSuppliers.isResolvable() ? contextSuppliers.get() : null);
    }

    ZanzibarOpenFGARelationshipManager(AuthorizationModelClient authorizationModelClient,
            OpenFGAContextSupplier contextSupplier) {
        this.authorizationModelClient = authorizationModelClient;
        this.contextSupplier = contextSupplier;
    }

    public Uni<Boolean> check(Relationship relationship) {

        var relTupleKey = tupleKeyFromRelationship(relationship);

        CheckOptions options = CheckOptions.DEFAULT;
        if (contextSupplier != null) {

            var supplied = contextSupplier.getContext(authorizationModelClient, relTupleKey);

            options = CheckOptions.withContext(supplied.context()).contextualTuples(supplied.contextualTuples());
        }

        return authorizationModelClient.check(relTupleKey, options);
    }

    @Override
    public Uni<Void> add(List<Relationship> relationships) {

        var tuples = relationships.stream()
                .map(this::tupleDefinitionFromRelationship)
                .toList();

        return authorizationModelClient.write(tuples, WriteOptions.withOnDuplicate(IGNORE))
                .replaceWithVoid();
    }

    @Override
    public Uni<Void> remove(List<Relationship> relationships) {

        var tuples = relationships.stream()
                .map(this::tupleKeyFromRelationship)
                .collect(Collectors.toList());

        return authorizationModelClient.write(null, tuples, WriteOptions.withOnMissing(IGNORE))
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

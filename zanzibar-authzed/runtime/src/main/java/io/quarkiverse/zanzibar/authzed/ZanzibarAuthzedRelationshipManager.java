package io.quarkiverse.zanzibar.authzed;

import java.util.List;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import com.authzed.api.v1.CheckPermissionRequest;
import com.authzed.api.v1.CheckPermissionResponse.Permissionship;
import com.authzed.api.v1.Consistency;
import com.authzed.api.v1.ObjectReference;
import com.authzed.api.v1.RelationshipUpdate;
import com.authzed.api.v1.RelationshipUpdate.Operation;
import com.authzed.api.v1.SubjectReference;
import com.authzed.api.v1.WriteRelationshipsRequest;
import com.authzed.api.v1.WriteRelationshipsResponse;

import io.quarkiverse.authzed.client.AuthzedClient;
import io.quarkiverse.zanzibar.Relationship;
import io.quarkiverse.zanzibar.RelationshipManager;
import io.smallrye.mutiny.Uni;

@ApplicationScoped
public class ZanzibarAuthzedRelationshipManager implements RelationshipManager {

    private static final Consistency CONSISTENCY = Consistency.newBuilder().setFullyConsistent(true).build();
    private final AuthzedClient client;

    @Inject
    public ZanzibarAuthzedRelationshipManager(AuthzedClient client) {
        this.client = client;
    }

    public Uni<Boolean> check(Relationship relationship) {
        var rel = toAutzedRelationship(relationship);
        return client.v1().permissionService().checkPermission(CheckPermissionRequest.newBuilder()
                .setConsistency(CONSISTENCY)
                .setPermission(rel.getRelation())
                .setResource(rel.getResource())
                .setSubject(rel.getSubject())
                .build())
                .map(r -> r.getPermissionship() == Permissionship.PERMISSIONSHIP_HAS_PERMISSION);
    }

    @Override
    public Uni<Void> add(List<Relationship> relationships) {
        return Uni.combine().all().unis(relationships.stream()
                .map(relationship -> client.v1().permissionService().writeRelationships(WriteRelationshipsRequest.newBuilder()
                        .addUpdates(RelationshipUpdate.newBuilder()
                                .setOperation(Operation.OPERATION_CREATE)
                                .setRelationship(toAutzedRelationship(relationship))
                                .build())
                        .build()))
                .collect(Collectors.toList())).with(WriteRelationshipsResponse.class, r -> null);
    }

    @Override
    public Uni<Void> remove(List<Relationship> relationships) {
        return Uni.combine().all().unis(relationships.stream()
                .map(relationship -> client.v1().permissionService().writeRelationships(WriteRelationshipsRequest.newBuilder()
                        .addUpdates(RelationshipUpdate.newBuilder()
                                .setOperation(Operation.OPERATION_DELETE)
                                .setRelationship(toAutzedRelationship(relationship))
                                .build())
                        .build()))
                .collect(Collectors.toList())).with(WriteRelationshipsResponse.class, r -> null);
    }

    static com.authzed.api.v1.Relationship toAutzedRelationship(Relationship relationship) {
        return com.authzed.api.v1.Relationship.newBuilder()
                .setRelation(relationship.relation())
                .setResource(ObjectReference.newBuilder()
                        .setObjectId(relationship.objectId())
                        .setObjectType(relationship.objectType())
                        .build())
                .setSubject(SubjectReference.newBuilder()
                        .setObject(ObjectReference.newBuilder()
                                .setObjectId(relationship.userId())
                                .setObjectType(relationship.userType())
                                .build())
                        .build())
                .build();
    }
}

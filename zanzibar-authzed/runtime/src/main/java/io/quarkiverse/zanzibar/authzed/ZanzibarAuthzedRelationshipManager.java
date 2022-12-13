package io.quarkiverse.zanzibar.authzed;

import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.authzed.api.v1.Core.ObjectReference;
import com.authzed.api.v1.Core.RelationshipUpdate;
import com.authzed.api.v1.Core.RelationshipUpdate.Operation;
import com.authzed.api.v1.Core.SubjectReference;
import com.authzed.api.v1.PermissionService.CheckPermissionRequest;
import com.authzed.api.v1.PermissionService.CheckPermissionResponse.Permissionship;
import com.authzed.api.v1.PermissionService.Consistency;
import com.authzed.api.v1.PermissionService.WriteRelationshipsRequest;
import com.authzed.api.v1.PermissionService.WriteRelationshipsResponse;

import io.quarkiverse.authzed.Tuples;
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
        String user = relationship.getUserId();
        SubjectReference subject = Tuples.parseUser(user);
        return client.v1().permissionService().checkPermission(CheckPermissionRequest.newBuilder()
                .setConsistency(CONSISTENCY)
                .setPermission(relationship.getRelation())
                .setResource(ObjectReference.newBuilder()
                        .setObjectId(relationship.getObjectId())
                        .setObjectType(relationship.getObjectType())
                        .build())
                .setSubject(subject)
                .build())
                .map(r -> r.getPermissionship() == Permissionship.PERMISSIONSHIP_HAS_PERMISSION);
    }

    @Override
    public Uni<Void> add(List<Relationship> relationships) {
        return Uni.combine().all().unis(relationships.stream().map(relationship -> {
            return client.v1().permissionService().writeRelationships(WriteRelationshipsRequest.newBuilder()
                    .addUpdates(RelationshipUpdate.newBuilder()
                            .setOperation(Operation.OPERATION_CREATE)
                            .setRelationship(toAutzedRelationship(relationship))
                            .build())
                    .build());

        }).collect(Collectors.toList())).combinedWith(WriteRelationshipsResponse.class, r -> null);
    }

    @Override
    public Uni<Void> remove(List<Relationship> relationships) {
        return Uni.combine().all().unis(relationships.stream().map(relationship -> {
            return client.v1().permissionService().writeRelationships(WriteRelationshipsRequest.newBuilder()
                    .addUpdates(RelationshipUpdate.newBuilder()
                            .setOperation(Operation.OPERATION_DELETE)
                            .setRelationship(toAutzedRelationship(relationship))
                            .build())
                    .build());
        }).collect(Collectors.toList())).combinedWith(WriteRelationshipsResponse.class, r -> null);
    }

    static com.authzed.api.v1.Core.Relationship toAutzedRelationship(Relationship relationship) {
        String user = relationship.getUserId();
        SubjectReference subject = Tuples.parseUser(user);
        return com.authzed.api.v1.Core.Relationship.newBuilder()
                .setRelation(relationship.getRelation())
                .setResource(ObjectReference.newBuilder()
                        .setObjectId(relationship.getObjectId())
                        .setObjectType(relationship.getObjectType())
                        .build())
                .setSubject(subject)
                .build();
    }
}

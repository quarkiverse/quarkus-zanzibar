package io.quarkiverse.zanzibar.authzed;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.jboss.logging.Logger;

import com.authzed.api.v1.CheckPermissionRequest;
import com.authzed.api.v1.CheckPermissionResponse.Permissionship;
import com.authzed.api.v1.Consistency;
import com.authzed.api.v1.ObjectReference;
import com.authzed.api.v1.RelationshipUpdate;
import com.authzed.api.v1.RelationshipUpdate.Operation;
import com.authzed.api.v1.SubjectReference;
import com.authzed.api.v1.WriteRelationshipsRequest;
import com.authzed.api.v1.WriteRelationshipsResponse;
import com.google.protobuf.ListValue;
import com.google.protobuf.NullValue;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;

import io.quarkiverse.authzed.client.AuthzedClient;
import io.quarkiverse.zanzibar.ContextAwareRelationshipManager;
import io.quarkiverse.zanzibar.Relationship;
import io.quarkiverse.zanzibar.RelationshipCheckContext;
import io.quarkiverse.zanzibar.RelationshipManager;
import io.smallrye.mutiny.Uni;

@ApplicationScoped
public class ZanzibarAuthzedRelationshipManager implements RelationshipManager, ContextAwareRelationshipManager {

    private static final Consistency CONSISTENCY = Consistency.newBuilder().setFullyConsistent(true).build();
    private static final Logger log = Logger.getLogger(ZanzibarAuthzedRelationshipManager.class);
    private final AuthzedClient client;

    @Inject
    public ZanzibarAuthzedRelationshipManager(AuthzedClient client) {
        this.client = client;
    }

    public Uni<Boolean> check(Relationship relationship) {
        return check(relationship, RelationshipCheckContext.empty());
    }

    @Override
    public Uni<Boolean> check(Relationship relationship, RelationshipCheckContext context) {
        var rel = toAutzedRelationship(relationship);
        var request = CheckPermissionRequest.newBuilder()
                .setConsistency(CONSISTENCY)
                .setPermission(rel.getRelation())
                .setResource(rel.getResource())
                .setSubject(rel.getSubject());
        if (context != null && !context.context().isEmpty()) {
            request.setContext(structFromContext(context.context()));
        }
        return client.v1().permissionService().checkPermission(request.build())
                .map(r -> {
                    if (r.getPermissionship() == Permissionship.PERMISSIONSHIP_HAS_PERMISSION) {
                        return true;
                    }
                    if (r.getPermissionship() == Permissionship.PERMISSIONSHIP_CONDITIONAL_PERMISSION) {
                        if (r.hasPartialCaveatInfo()) {
                            log.debugf("Conditional permission missing context: %s",
                                    r.getPartialCaveatInfo().getMissingRequiredContextList());
                        } else {
                            log.debug("Conditional permission missing context");
                        }
                    }
                    return false;
                });
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

    private static Struct structFromContext(Map<String, Object> context) {
        var builder = Struct.newBuilder();
        for (var entry : context.entrySet()) {
            builder.putFields(entry.getKey(), valueFromObject(entry.getValue()));
        }
        return builder.build();
    }

    @SuppressWarnings("unchecked")
    private static Value valueFromObject(Object value) {
        if (value == null) {
            return Value.newBuilder().setNullValue(NullValue.NULL_VALUE).build();
        }
        if (value instanceof Boolean bool) {
            return Value.newBuilder().setBoolValue(bool).build();
        }
        if (value instanceof Number number) {
            return Value.newBuilder().setNumberValue(number.doubleValue()).build();
        }
        if (value instanceof String str) {
            return Value.newBuilder().setStringValue(str).build();
        }
        if (value instanceof Map<?, ?> map) {
            return Value.newBuilder().setStructValue(structFromContext((Map<String, Object>) toStringKeyMap(map))).build();
        }
        if (value instanceof Iterable<?> list) {
            var listBuilder = ListValue.newBuilder();
            for (var item : list) {
                listBuilder.addValues(valueFromObject(item));
            }
            return Value.newBuilder().setListValue(listBuilder).build();
        }
        return Value.newBuilder().setStringValue(String.valueOf(value)).build();
    }

    private static Map<String, Object> toStringKeyMap(Map<?, ?> map) {
        var result = new java.util.LinkedHashMap<String, Object>();
        for (var entry : map.entrySet()) {
            result.put(String.valueOf(entry.getKey()), entry.getValue());
        }
        return result;
    }
}

package io.quarkiverse.zanzibar.openfga.it;

import java.util.List;
import java.util.Map;

import io.quarkiverse.zanzibar.Relationship;
import io.quarkiverse.zanzibar.ZanzibarPermission;

public class ContextMergePermission extends ZanzibarPermission {

    private final String objectId;

    public ContextMergePermission(String name, String objectId) {
        super(name);
        this.objectId = objectId;
    }

    @Override
    public String getObjectType() {
        return "thing";
    }

    @Override
    public String getObjectId() {
        return String.valueOf(objectId);
    }

    @Override
    public Map<String, Object> getContext() {
        return Map.of(
                "k1", "permission",
                "k2", "permission");
    }

    @Override
    public List<Relationship> getContextualTuples() {
        return List.of(Relationship.of("thing", "permission", "reader", "user", "permission"));
    }
}

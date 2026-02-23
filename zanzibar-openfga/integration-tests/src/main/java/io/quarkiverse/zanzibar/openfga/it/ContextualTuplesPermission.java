package io.quarkiverse.zanzibar.openfga.it;

import java.util.List;

import io.quarkiverse.zanzibar.Relationship;
import io.quarkiverse.zanzibar.ZanzibarPermission;

public class ContextualTuplesPermission extends ZanzibarPermission {

    private final String objectId;
    private final String userId;

    public ContextualTuplesPermission(String name, String objectId, String userId) {
        super(name);
        this.objectId = objectId;
        this.userId = userId;
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
    public List<Relationship> getContextualTuples() {
        return List.of(Relationship.of("thing", String.valueOf(objectId), getName(), "user", String.valueOf(userId)));
    }
}

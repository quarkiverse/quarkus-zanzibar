package io.quarkiverse.zanzibar.it;

import io.quarkiverse.zanzibar.ZanzibarPermission;

public class GlobalThingPermission extends ZanzibarPermission {

    private final String id;

    public GlobalThingPermission(String name, String id) {
        super(name);
        this.id = id;
    }

    @Override
    public String getObjectType() {
        return "thing";
    }

    @Override
    public String getObjectId() {
        return id;
    }
}

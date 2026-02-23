package io.quarkiverse.zanzibar.authzed.it;

import java.util.Map;

import io.quarkiverse.zanzibar.ZanzibarPermission;

public class CaveatedThingPermission extends ZanzibarPermission {

    private final String id;

    public CaveatedThingPermission(String name, String id) {
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

    @Override
    public Map<String, Object> getContext() {
        return Map.of("region", "us");
    }
}

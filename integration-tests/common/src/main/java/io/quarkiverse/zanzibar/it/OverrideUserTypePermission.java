package io.quarkiverse.zanzibar.it;

import java.util.Optional;

import io.quarkiverse.zanzibar.ZanzibarPermission;

public class OverrideUserTypePermission extends ZanzibarPermission {

    private final String id;

    public OverrideUserTypePermission(String name, String id) {
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
    public Optional<String> getUserType() {
        return Optional.of("perm");
    }
}

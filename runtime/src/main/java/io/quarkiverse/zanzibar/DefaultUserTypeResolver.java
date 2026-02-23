package io.quarkiverse.zanzibar;

import java.util.Map;
import java.util.Optional;

public class DefaultUserTypeResolver {

    private final Map<String, String> permissionDefaults;

    public DefaultUserTypeResolver(Map<String, String> permissionDefaults) {
        this.permissionDefaults = permissionDefaults;
    }

    public Optional<String> resolve(ZanzibarPermission permission) {
        if (permission == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(permissionDefaults.get(permission.getClass().getName()));
    }
}

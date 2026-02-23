package io.quarkiverse.zanzibar.it;

import java.util.Optional;

public class ThingUserPermission extends ThingPermission {

    public ThingUserPermission(String name, String id) {
        super(name, id);
    }

    @Override
    public Optional<String> getUserType() {
        return Optional.of("user");
    }
}

package io.quarkiverse.zanzibar;

import java.security.Principal;
import java.util.Optional;

import javax.annotation.Nullable;

/**
 * Extracts the user type & id from the principal and/or other available sources.
 */
public interface UserExtractor {

    record User(String type, String id) {
    }

    Optional<User> extractUser(Principal principal, @Nullable String discoveredUserType);

}

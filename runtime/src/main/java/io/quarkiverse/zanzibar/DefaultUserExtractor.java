package io.quarkiverse.zanzibar;

import java.security.Principal;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nullable;

import io.quarkus.arc.Arc;
import io.quarkus.security.identity.SecurityIdentity;

public class DefaultUserExtractor implements UserExtractor {

    private record UserInfo(Optional<String> userType, Optional<String> userId) {
        Optional<User> getUser() {
            if (userType.isPresent() && userId.isPresent()) {
                return Optional.of(new User(userType.get(), userId.get()));
            }
            return Optional.empty();
        }
    }

    private final boolean extractUserTypeFromName;
    private final String userTypeSeparator;
    private final boolean extractUserTypeFromRoles;

    public DefaultUserExtractor(boolean extractUserTypeFromName, String userTypeSeparator, boolean extractUserTypeFromRoles) {
        this.extractUserTypeFromName = extractUserTypeFromName;
        this.userTypeSeparator = userTypeSeparator;
        this.extractUserTypeFromRoles = extractUserTypeFromRoles;
    }

    @Override
    public Optional<User> extractUser(@Nullable Principal principal, @Nullable String discoveredUserType) {
        if (principal == null) {
            return Optional.empty();
        }

        var info = extractUserInfoFromPrincipal(principal);

        if (info.userType.isEmpty() && extractUserTypeFromRoles) {
            var userType = extractUserTypeFromRoles();
            info = new UserInfo(userType, info.userId);
        }

        if (info.userType.isEmpty()) {
            info = new UserInfo(Optional.ofNullable(discoveredUserType), info.userId);
        }

        return info.getUser();
    }

    UserInfo extractUserInfoFromPrincipal(Principal principal) {

        var name = principal.getName();
        if (extractUserTypeFromName) {
            var parts = name.split(userTypeSeparator);
            if (parts.length != 2) {
                return new UserInfo(Optional.empty(), Optional.of(name));
            }
            return new UserInfo(Optional.of(parts[0]), Optional.of(parts[1]));
        }

        return new UserInfo(Optional.empty(), Optional.of(name));
    }

    Optional<String> extractUserTypeFromRoles() {
        var roles = discoverRoles();
        if (roles.size() == 1) {
            return Optional.of(roles.iterator().next());
        }
        return Optional.empty();
    }

    Collection<String> discoverRoles() {
        try (var identity = Arc.container().instance(SecurityIdentity.class)) {
            if (!identity.isAvailable()) {
                return Set.of();
            }
            return identity.get().getRoles();
        }
    }

}

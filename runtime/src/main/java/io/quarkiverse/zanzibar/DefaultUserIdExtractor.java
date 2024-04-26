package io.quarkiverse.zanzibar;

import java.security.Principal;
import java.util.Optional;

import jakarta.inject.Singleton;

import io.quarkus.arc.DefaultBean;

@DefaultBean
@Singleton
public class DefaultUserIdExtractor implements UserIdExtractor {

    @Override
    public Optional<String> extractUserId(Principal principal) {
        if (principal == null || principal.getName() == null) {
            return Optional.empty();
        } else {
            return Optional.of(principal.getName());
        }
    }
}

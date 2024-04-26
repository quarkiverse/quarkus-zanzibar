package io.quarkiverse.zanzibar;

import java.security.Principal;
import java.util.Optional;

public interface UserIdExtractor {

    Optional<String> extractUserId(Principal principal);

}

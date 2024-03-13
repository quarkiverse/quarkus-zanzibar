package io.quarkiverse.zanzibar;

import java.security.Principal;
import java.util.Optional;

public interface ZanzibarUserIdExtractor {

    Optional<String> extractUserId(Principal principal);

}

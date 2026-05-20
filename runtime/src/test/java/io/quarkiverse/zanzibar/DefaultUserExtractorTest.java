package io.quarkiverse.zanzibar;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.security.Principal;

import org.junit.jupiter.api.Test;

class DefaultUserExtractorTest {

    @Test
    void returnsEmptyUserWhenPrincipalNameIsNullAndUserTypeIsExtractedFromName() {
        var extractor = new DefaultUserExtractor(true, ":", false);

        var user = extractor.extractUser(new NullNamePrincipal(), null);

        assertTrue(user.isEmpty());
    }

    private static final class NullNamePrincipal implements Principal {

        @Override
        public String getName() {
            return null;
        }
    }
}

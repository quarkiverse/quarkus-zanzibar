package io.quarkiverse.zanzibar.deployment;

import java.security.Principal;

import io.quarkiverse.zanzibar.UserExtractor;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigRoot(phase = ConfigPhase.BUILD_TIME)
@ConfigMapping(prefix = "quarkus.zanzibar")
public interface ZanzibarConfig {

    /**
     * Configuration for JAX-RS authorization filter.
     */
    JAXRSFilterConfig filter();

    /**
     * Enable extraction of the user type from the {@link Principal#getName()}.
     */
    @WithDefault("true")
    boolean extractUserTypeFromName();

    /**
     * The separator used to separate the user type from the user id, when extracting the type from a
     * {@link Principal#getName()} or similar value.
     * <p>
     * Only applies when using the default {@link UserExtractor} bean.
     */
    @WithDefault(":")
    String userTypeSeparator();

    /**
     * Whether to extract the user type from the roles of the {@link Principal}.
     * <p>
     * If multiple roles are present, no user type will be extracted, which generally results
     * in the user being considered unauthenticated.
     * <p>
     * Roles will only be considered if the user type has not been extracted from the
     * {@link Principal#getName()} (either because it was not present in then name
     * or because name extraction was disabled).
     * <p>
     * Only applies when using the default {@link UserExtractor} bean.
     */
    @WithDefault("true")
    boolean extractUserTypeFromRoles();
}

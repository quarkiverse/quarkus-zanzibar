package io.quarkiverse.zanzibar.deployment;

import java.security.Principal;
import java.util.Map;

import io.quarkiverse.zanzibar.UserExtractor;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithDefaults;

@ConfigRoot(phase = ConfigPhase.BUILD_TIME)
@ConfigMapping(prefix = "quarkus.zanzibar")
public interface ZanzibarConfig {

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

    /**
     * Default user type used when none can be extracted from the principal.
     */
    @WithDefault("user")
    String defaultUserType();

    /**
     * Per-resource default user type configuration.
     * <p>
     * Example: quarkus.zanzibar.default-user-types."com.acme.MyResource".user-type=customer
     */
    @WithDefaults
    Map<String, DefaultUserType> defaultUserTypes();

    interface DefaultUserType {
        /**
         * Default user type for a specific resource class.
         */
        @WithDefault("${quarkus.zanzibar.default-user-type}")
        String userType();
    }
}

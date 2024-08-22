package io.quarkiverse.zanzibar.deployment;

import java.time.Duration;
import java.util.Optional;

import io.quarkiverse.zanzibar.annotations.FGADynamicObject;
import io.quarkiverse.zanzibar.annotations.FGARelation;
import io.smallrye.config.WithDefault;

/**
 * Configuration for JAX-RS authorization filter.
 */
public interface JAXRSFilterConfig {

    /**
     * Whether the filter is enabled.
     * <p>
     * When enabled all endpoints must have a resolvable {@link FGADynamicObject} and
     * {@link FGARelation} otherwise a FORBIDDEN will be returns to clients.
     */
    @WithDefault("true")
    boolean enabled();

    /**
     * Should access to resource methods without FGA annotations be denied.
     */
    @WithDefault("true")
    boolean denyUnannotatedResourceMethods();

    /**
     * User-id used for authorization when the request is unauthenticated.
     */
    Optional<String> unauthenticatedUser();

    /**
     * Maximum time an authorization check is allowed to take.
     */
    @WithDefault("5s")
    Duration timeout();

}

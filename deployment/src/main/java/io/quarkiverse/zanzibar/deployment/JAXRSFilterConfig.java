package io.quarkiverse.zanzibar.deployment;

import java.time.Duration;
import java.util.Optional;

import io.quarkiverse.zanzibar.jaxrs.annotations.FGADynamicObject;
import io.quarkiverse.zanzibar.jaxrs.annotations.FGARelation;
import io.quarkus.runtime.annotations.ConfigGroup;
import io.quarkus.runtime.annotations.ConfigItem;

/**
 * Configuration for JAX-RS authorization filter.
 */
@ConfigGroup
public class JAXRSFilterConfig {

    /**
     * Whether the filter is enabled.
     * <p>
     * When enabled all endpoints must have a resolvable {@link FGADynamicObject} and
     * {@link FGARelation} otherwise a FORBIDDEN will be returns to clients.
     */
    @ConfigItem(defaultValue = "true")
    public boolean enabled;

    /**
     * Should access to resource methods without FGA annotations be denied.
     */
    @ConfigItem(defaultValue = "true")
    public boolean denyUnannotatedResourceMethods;

    /**
     * Name used for authorization when the request is unauthenticated.
     */
    @ConfigItem
    public Optional<String> unauthenticatedUser;

    /**
     * Maximum time an authorization check is allowed to take.
     */
    @ConfigItem(defaultValue = "5s")
    public Duration timeout;

}

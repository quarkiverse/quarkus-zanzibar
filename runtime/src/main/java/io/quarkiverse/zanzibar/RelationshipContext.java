package io.quarkiverse.zanzibar;

import java.util.Optional;

/**
 * Injectable context for accessing relationship-based authorization information.
 * <p>
 * For authorized requests, all the methods will return a non-empty value. For requests that do not require
 * authorization (e.g., when using {@link io.quarkiverse.zanzibar.annotations.FGARelation#ANY}, some methods may
 * return an empty value.
 * <p>
 * The provided properties can be used to make dynamic authorization updates or checks without duplicating the
 * configuration. For example, adding a relationship based on the current user and/or object and/or relation that
 * the user is accessing.
 */
public interface RelationshipContext {

    /**
     * The discovered object type, if available.
     *
     * @return the object type, if available
     */
    Optional<String> objectType();

    /**
     * The discovered object ID, if available.
     *
     * @return the object ID, if available
     */
    Optional<String> objectId();

    /**
     * The discovered relation, if available.
     *
     * @return the relation, if available
     */
    Optional<String> relation();

    /**
     * The discovered user type, if available.
     *
     * @return the user type, if available
     */
    Optional<String> userType();

    /**
     * The discovered user ID, if available.
     *
     * @return the user ID, if available
     */
    Optional<String> userId();

}

package io.quarkiverse.zanzibar;

import java.util.List;

import io.smallrye.mutiny.Uni;

/**
 * Simplified interface for managing relationships and executing authorization checks.
 * <p>
 *
 * @apiNote This interface is not meant to cover all functionality provided by the FGA backend; it is expected that
 *          direct use of the backend client will be required for advanced functionality.
 */
public interface RelationshipManager {

    /**
     * Check if a relationship is authorized by the FGA backend.
     * <p>
     *
     * @param relationship Relationship to check.
     * @return Boolean status of the authorization result; true denotes the relationship is authorized.
     */
    Uni<Boolean> check(Relationship relationship);

    /**
     * Add relationships to the FGA backend.
     * <p>
     *
     * @param relationships List of relationships to add to the FGA backend.
     * @return Reactive result of operation.
     */
    Uni<Void> add(List<Relationship> relationships);

    /**
     * Remove relationships from the FGA backend.
     * <p>
     *
     * @param relationships List of relationships to remove from the FGA backend.
     * @return Reactive result of operation.
     */
    Uni<Void> remove(List<Relationship> relationships);

}

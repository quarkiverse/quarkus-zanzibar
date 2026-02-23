package io.quarkiverse.zanzibar;

import io.smallrye.mutiny.Uni;

/**
 * Optional relationship manager that can use additional check context.
 */
public interface ContextAwareRelationshipManager extends RelationshipManager {

    Uni<Boolean> check(Relationship relationship, RelationshipCheckContext context);
}

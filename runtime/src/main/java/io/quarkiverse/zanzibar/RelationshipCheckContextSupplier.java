package io.quarkiverse.zanzibar;

/**
 * Supplies additional context for relationship checks (conditions, caveats, contextual tuples).
 */
public interface RelationshipCheckContextSupplier {

    RelationshipCheckContext get();
}

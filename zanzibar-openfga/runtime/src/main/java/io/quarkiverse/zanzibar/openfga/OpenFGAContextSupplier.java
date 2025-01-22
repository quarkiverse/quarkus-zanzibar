package io.quarkiverse.zanzibar.openfga;

import java.util.Collection;
import java.util.Map;

import io.quarkiverse.openfga.client.AuthorizationModelClient;
import io.quarkiverse.openfga.client.model.RelTupleDefinition;
import io.quarkiverse.openfga.client.model.RelTupleKey;

/**
 * Supplies the context for a given relationship tuple.
 * <p>
 * Application's wishing to use OpenFGA context capabilities (e.g., conditions) must provide an implementation
 * via CDI. The {@link #getContext(AuthorizationModelClient, RelTupleKey)} method will be called before each
 * authorization check to obtain the context for the given relationship tuple. This only manages the context
 * for authorization checks, not the relationship tuples themselves, which must be created/updated via the
 * OpenFGA API externally.
 */
public interface OpenFGAContextSupplier {

    record Result(Map<String, Object> context, Collection<RelTupleDefinition> contextualTuples) {
    }

    /**
     * Supplies the context for the given relationship tuple.
     *
     * @param client the OpenFGA client
     * @param relTupleKey the relationship tuple key
     * @return the context to use for the given relationship tuple
     */
    Result getContext(AuthorizationModelClient client, RelTupleKey relTupleKey);

}

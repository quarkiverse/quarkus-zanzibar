package io.quarkiverse.zanzibar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Context data provided to authorization checks for conditions, caveats, and contextual tuples.
 */
public record RelationshipCheckContext(Map<String, Object> context,
        List<Relationship> contextualTuples,
        Map<String, Object> metadata) {

    public RelationshipCheckContext {
        context = context == null ? Map.of() : Collections.unmodifiableMap(new java.util.LinkedHashMap<>(context));
        contextualTuples = contextualTuples == null ? List.of()
                : Collections.unmodifiableList(new ArrayList<>(contextualTuples));
        metadata = metadata == null ? Map.of() : Collections.unmodifiableMap(new java.util.LinkedHashMap<>(metadata));
    }

    public static RelationshipCheckContext empty() {
        return new RelationshipCheckContext(Map.of(), List.of(), Map.of());
    }
}

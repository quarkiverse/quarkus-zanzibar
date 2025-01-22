package io.quarkiverse.zanzibar.runtime;

import java.time.Duration;
import java.util.Optional;
import java.util.function.Supplier;

import io.quarkiverse.zanzibar.DefaultUserExtractor;
import io.quarkiverse.zanzibar.RelationshipContextManager;
import io.quarkiverse.zanzibar.RelationshipManager;
import io.quarkiverse.zanzibar.UserExtractor;
import io.quarkiverse.zanzibar.jaxrs.ZanzibarDynamicFeature;
import io.quarkiverse.zanzibar.jaxrs.ZanzibarReactiveAuthorizationFilter;
import io.quarkiverse.zanzibar.jaxrs.ZanzibarSynchronousAuthorizationFilter;
import io.quarkus.arc.Arc;
import io.quarkus.runtime.RuntimeValue;
import io.quarkus.runtime.annotations.Recorder;

@Recorder
public class ZanzibarRecorder {

    public RuntimeValue<ZanzibarDynamicFeature.FilterFactory> createSynchronousFilterFactory() {
        return new RuntimeValue<>(ZanzibarSynchronousAuthorizationFilter::new);
    }

    public RuntimeValue<ZanzibarDynamicFeature.FilterFactory> createReactiveFilterFactory() {
        return new RuntimeValue<>(ZanzibarReactiveAuthorizationFilter::new);
    }

    public Supplier<ZanzibarDynamicFeature> createDynamicFeature(Optional<String> unauthenticatedUserId, Duration duration,
            boolean denyUnannotated, RuntimeValue<ZanzibarDynamicFeature.FilterFactory> filterFactory,
            Supplier<DefaultUserExtractor> defaultUserExtractor) {
        return () -> {
            try (var relationshipManager = Arc.container().instance(RelationshipManager.class);
                    var relationshipContextManager = Arc.container().instance(RelationshipContextManager.class);
                    var userExtractor = Arc.container().instance(UserExtractor.class)) {

                var selectedUserExtractor = userExtractor.isAvailable() ? userExtractor.get() : defaultUserExtractor.get();

                return new ZanzibarDynamicFeature(relationshipManager.get(), relationshipContextManager.get(),
                        selectedUserExtractor, unauthenticatedUserId, duration, denyUnannotated,
                        filterFactory.getValue());
            }
        };
    }

    public Supplier<DefaultUserExtractor> createUserExtractor(boolean extractUserTypeFromName, String userTypeSeparator,
            boolean extractUserTypeFromRoles) {
        return () -> new DefaultUserExtractor(extractUserTypeFromName, userTypeSeparator, extractUserTypeFromRoles);
    }
}

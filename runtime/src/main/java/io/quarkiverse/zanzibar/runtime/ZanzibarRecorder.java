package io.quarkiverse.zanzibar.runtime;

import java.time.Duration;
import java.util.Optional;
import java.util.function.Supplier;

import io.quarkiverse.zanzibar.RelationshipManager;
import io.quarkiverse.zanzibar.ZanzibarUserIdExtractor;
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
            boolean denyUnannotated, RuntimeValue<ZanzibarDynamicFeature.FilterFactory> filterFactory) {
        return () -> {
            try (var relationshipManager = Arc.container().instance(RelationshipManager.class);
                    var zanzibarUserIdExtractor = Arc.container().instance(ZanzibarUserIdExtractor.class)) {

                return new ZanzibarDynamicFeature(relationshipManager.get(),
                        zanzibarUserIdExtractor.get(),
                        unauthenticatedUserId, duration, denyUnannotated, filterFactory.getValue());
            }
        };
    }
}

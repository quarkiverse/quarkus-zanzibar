package io.quarkiverse.zanzibar.jaxrs;

import static org.mockito.Mockito.*;

import java.time.Duration;
import java.util.Optional;

import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.FeatureContext;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import io.quarkiverse.zanzibar.RelationshipContextManager;
import io.quarkiverse.zanzibar.RelationshipManager;
import io.quarkiverse.zanzibar.UserExtractor;
import io.quarkiverse.zanzibar.annotations.FGAIgnore;
import io.quarkiverse.zanzibar.annotations.FGAObject;
import io.quarkiverse.zanzibar.annotations.FGARelation;

class ZanzibarDynamicFeatureTest {

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void ignoredResourcesNeverRegisterFilters(boolean denyUnannotated) throws NoSuchMethodException {
        for (var resourceClass : new Class<?>[] { MethodIgnoredResource.class, ClassIgnoredResource.class,
                InheritedIgnoredResource.class, InterfaceIgnoredResource.class }) {
            var context = mock(FeatureContext.class);
            var factory = mock(ZanzibarDynamicFeature.FilterFactory.class);
            var feature = feature(denyUnannotated, factory);

            feature.configure(resourceInfo(resourceClass), context);

            verifyNoInteractions(context, factory);
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void unannotatedResourcesFollowDenyPolicy(boolean denyUnannotated) throws NoSuchMethodException {
        var context = mock(FeatureContext.class);
        var factory = mock(ZanzibarDynamicFeature.FilterFactory.class);

        feature(denyUnannotated, factory).configure(resourceInfo(UnannotatedResource.class), context);

        if (denyUnannotated) {
            verify(context).register(ZanzibarDenyFilter.INSTANCE);
        }
        verifyNoMoreInteractions(context);
        verifyNoInteractions(factory);
    }

    private ZanzibarDynamicFeature feature(boolean denyUnannotated, ZanzibarDynamicFeature.FilterFactory factory) {
        return new ZanzibarDynamicFeature(mock(RelationshipManager.class), new RelationshipContextManager(),
                mock(UserExtractor.class), Optional.empty(), Duration.ofSeconds(1), denyUnannotated, factory);
    }

    private ResourceInfo resourceInfo(Class<?> resourceClass) throws NoSuchMethodException {
        var resourceInfo = mock(ResourceInfo.class);
        doReturn(resourceClass).when(resourceInfo).getResourceClass();
        when(resourceInfo.getResourceMethod()).thenReturn(resourceClass.getMethod("endpoint"));
        return resourceInfo;
    }

    @FGAObject(type = "document", id = "1")
    @FGARelation("reader")
    static class MethodIgnoredResource {
        /** Endpoint that opts out of class-level authorization. */
        @FGAIgnore
        public void endpoint() {
        }
    }

    @FGAIgnore
    static class ClassIgnoredResource {
        /** Endpoint ignored through its resource class. */
        public void endpoint() {
        }
    }

    static class InheritedIgnoredResource extends ClassIgnoredResource {
    }

    interface IgnoredEndpoint {
        /** Endpoint ignored through its interface declaration. */
        @FGAIgnore
        void endpoint();
    }

    static class InterfaceIgnoredResource implements IgnoredEndpoint {
        @Override
        public void endpoint() {
        }
    }

    static class UnannotatedResource {
        /** Endpoint governed by the unannotated-resource policy. */
        public void endpoint() {
        }
    }
}

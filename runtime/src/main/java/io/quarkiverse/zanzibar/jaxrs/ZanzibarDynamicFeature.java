package io.quarkiverse.zanzibar.jaxrs;

import static io.quarkiverse.zanzibar.annotations.FGADynamicObject.Source.*;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.DynamicFeature;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.FeatureContext;

import org.jboss.logging.Logger;

import io.quarkiverse.zanzibar.RelationshipContextManager;
import io.quarkiverse.zanzibar.RelationshipManager;
import io.quarkiverse.zanzibar.UserExtractor;
import io.quarkiverse.zanzibar.annotations.*;
import io.quarkiverse.zanzibar.jaxrs.ZanzibarAuthorizationFilter.Action;

public class ZanzibarDynamicFeature implements DynamicFeature {

    private static final Logger log = Logger.getLogger(ZanzibarDynamicFeature.class);

    public interface FilterFactory {
        ContainerRequestFilter create(Action annotations, RelationshipManager relationshipManager,
                RelationshipContextManager relationshipContextManager, UserExtractor userExtractor,
                Optional<String> userType, Optional<String> unauthenticatedUserId, Duration timeout);
    }

    static class AnnotationQuery {
        public Object source;
        public Class<? extends Annotation> annotationType;

        public AnnotationQuery(Object source, Class<? extends Annotation> annotationType) {
            this.source = source;
            this.annotationType = annotationType;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof AnnotationQuery that))
                return false;
            return source.equals(that.source) && annotationType.equals(that.annotationType);
        }

        @Override
        public int hashCode() {
            return Objects.hash(source, annotationType);
        }
    }

    record Annotations(Optional<FGAIgnore> ignore, Optional<FGADynamicObject> dynamicObject,
            Optional<FGAObject> constantObject, Optional<FGARelation> relationAllowed,
            Optional<FGAUserType> userType) {

        boolean isIgnored() {
            return ignore.isPresent();
        }

        boolean isEmpty() {
            return dynamicObject.isEmpty() && constantObject.isEmpty() && relationAllowed.isEmpty();
        }
    }

    RelationshipManager relationshipManager;
    RelationshipContextManager relationshipContextManager;
    UserExtractor userExtractor;
    Optional<String> unauthenticatedUserId;
    Duration timeout;
    boolean denyUnannotated;
    FilterFactory filterFactory;

    Map<Action, ContainerRequestFilter> filterCache = new ConcurrentHashMap<>();
    Map<Method, Annotations> authorizationAnnotationsCache = new ConcurrentHashMap<>();
    Map<AnnotationQuery, Optional<Annotation>> annotationQueryCache = new ConcurrentHashMap<>();

    public ZanzibarDynamicFeature(RelationshipManager relationshipManager,
            RelationshipContextManager relationshipContextManager, UserExtractor userExtractor,
            Optional<String> unauthenticatedUserId,
            Duration timeout, boolean denyUnannotated, FilterFactory filterFactory) {
        this.relationshipContextManager = Objects.requireNonNull(relationshipContextManager,
                "relationshipContextManager must not be null");
        this.relationshipManager = Objects.requireNonNull(relationshipManager, "relationshipManager must not be null");
        this.userExtractor = Objects.requireNonNull(userExtractor, "userIdExtractor must not be null");
        this.unauthenticatedUserId = unauthenticatedUserId;
        this.timeout = timeout;
        this.denyUnannotated = denyUnannotated;
        this.filterFactory = filterFactory;
    }

    @Override
    public void configure(ResourceInfo resourceInfo, FeatureContext context) {
        var annotations = findAuthorizationAnnotations(resourceInfo);
        if (annotations.isEmpty()) {
            if (denyUnannotated) {
                context.register(ZanzibarDenyFilter.INSTANCE);
            }
            return;
        }

        if (annotations.isIgnored()) {
            log.debugf("Excluding authorization check for %f, marked as ignored", resourceInfo.getResourceMethod());
            return;
        }

        var action = getAction(resourceInfo, annotations);

        Optional<String> userType = annotations.userType.map(FGAUserType::value);

        var filter = filterCache.computeIfAbsent(action,
                key -> filterFactory.create(key, relationshipManager, relationshipContextManager,
                        userExtractor, userType, unauthenticatedUserId,
                        timeout));

        context.register(filter, Priorities.AUTHORIZATION);
    }

    private static Action getAction(ResourceInfo resourceInfo, Annotations annotations) {

        if (annotations.relationAllowed.isEmpty()) {
            String message = "No FGA relation specifier found for method " + resourceInfo.getResourceMethod();
            throw new IllegalStateException(message);
        }
        var relation = annotations.relationAllowed.get();

        Action action;
        if (annotations.constantObject.isPresent()) {
            action = new Action(annotations.constantObject.get(), relation.value());
        } else if (annotations.dynamicObject.isPresent()) {
            action = new Action(annotations.dynamicObject.get(), relation.value());
        } else {
            String message = "No FGA object specifier found for method " + resourceInfo.getResourceMethod();
            throw new IllegalStateException(message);
        }
        return action;
    }

    Annotations findAuthorizationAnnotations(ResourceInfo resourceInfo) {
        return authorizationAnnotationsCache.computeIfAbsent(resourceInfo.getResourceMethod(), key -> {

            var ignoreAnn = findAnnotation(resourceInfo, FGAIgnore.class);
            if (ignoreAnn.isPresent()) {
                return new Annotations(ignoreAnn, Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
            }

            var userTypeAnn = findAnnotation(resourceInfo, FGAUserType.class);
            var relationAllowedAnn = findAnnotation(resourceInfo, FGARelation.class);
            var constantObjectAnn = findAnnotation(resourceInfo, FGAObject.class);
            var dynamicObjectAnn = findAnnotation(resourceInfo, FGADynamicObject.class);

            if (dynamicObjectAnn.isEmpty()) {
                var pathObjectAnn = findAnnotation(resourceInfo, FGAPathObject.class);
                if (pathObjectAnn.isPresent()) {
                    dynamicObjectAnn = Optional.of(
                            new FGADynamicObject.Literal(PATH, pathObjectAnn.get().param(), pathObjectAnn.get().type()));
                }
            }
            if (dynamicObjectAnn.isEmpty()) {
                var queryObjectAnn = findAnnotation(resourceInfo, FGAQueryObject.class);
                if (queryObjectAnn.isPresent()) {
                    dynamicObjectAnn = Optional.of(
                            new FGADynamicObject.Literal(QUERY, queryObjectAnn.get().param(), queryObjectAnn.get().type()));
                }
            }
            if (dynamicObjectAnn.isEmpty()) {
                var headerObjectAnn = findAnnotation(resourceInfo, FGAHeaderObject.class);
                if (headerObjectAnn.isPresent()) {
                    dynamicObjectAnn = Optional.of(
                            new FGADynamicObject.Literal(HEADER, headerObjectAnn.get().name(), headerObjectAnn.get().type()));
                }
            }
            if (dynamicObjectAnn.isEmpty()) {
                var requestObjectAnn = findAnnotation(resourceInfo, FGARequestObject.class);
                if (requestObjectAnn.isPresent()) {
                    dynamicObjectAnn = Optional.of(
                            new FGADynamicObject.Literal(REQUEST, requestObjectAnn.get().property(),
                                    requestObjectAnn.get().type()));
                }
            }

            return new Annotations(Optional.empty(), dynamicObjectAnn, constantObjectAnn, relationAllowedAnn, userTypeAnn);
        });
    }

    <A extends Annotation> Optional<A> findAnnotation(ResourceInfo resourceInfo, Class<A> annotationType) {
        return findAnnotation(resourceInfo.getResourceMethod(), annotationType)
                .or(() -> findAnnotation(resourceInfo.getResourceClass(), annotationType));
    }

    <A extends Annotation> Optional<A> findAnnotation(Method method, Class<A> annotationType) {
        return annotationQueryCache.computeIfAbsent(new AnnotationQuery(method, annotationType),
                query -> searchHierarchyForAnnotation(method, annotationType).map(Function.identity()))
                .map(annotationType::cast);
    }

    <A extends Annotation> Optional<A> searchHierarchyForAnnotation(Method method, Class<A> annotationType) {
        return ofNullable(method.getAnnotation(annotationType))
                .or(() -> {

                    var declClass = method.getDeclaringClass();

                    // Check superclass
                    try {
                        var superMethod = declClass.getSuperclass().getDeclaredMethod(method.getName(),
                                method.getParameterTypes());
                        var ann = searchHierarchyForAnnotation(superMethod, annotationType);
                        if (ann.isPresent()) {
                            return ann;
                        }
                    } catch (NullPointerException | NoSuchMethodException e) {
                        // Ignore
                    }

                    // Check interfaces
                    for (var iface : declClass.getInterfaces()) {
                        try {
                            var ifaceMethod = iface.getDeclaredMethod(method.getName(), method.getParameterTypes());
                            var ann = searchHierarchyForAnnotation(ifaceMethod, annotationType);
                            if (ann.isPresent()) {
                                return ann;
                            }
                        } catch (NoSuchMethodException e) {
                            // Ignore
                        }
                    }

                    return empty();
                });
    }

    <A extends Annotation> Optional<A> findAnnotation(Class<?> cls, Class<A> annotationType) {
        return annotationQueryCache.computeIfAbsent(new AnnotationQuery(cls, annotationType),
                key -> searchHierarchyForAnnotation(cls, annotationType).map(Function.identity()))
                .map(annotationType::cast);
    }

    <A extends Annotation> Optional<A> searchHierarchyForAnnotation(Class<?> cls, Class<A> annotationType) {
        // Class superclasses handled by @Inherited
        return ofNullable(cls.getAnnotation(annotationType))
                .or(() -> {

                    // Check interfaces
                    for (var iface : cls.getInterfaces()) {
                        var ann = searchHierarchyForAnnotation(iface, annotationType);
                        if (ann.isPresent()) {
                            return ann;
                        }
                    }

                    return empty();
                });
    }

}

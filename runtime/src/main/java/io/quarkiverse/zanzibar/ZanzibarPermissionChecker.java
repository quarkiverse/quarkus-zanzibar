package io.quarkiverse.zanzibar;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import org.jboss.logging.Logger;

import io.quarkiverse.zanzibar.UserExtractor.User;
import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.mutiny.Uni;

@ApplicationScoped
public class ZanzibarPermissionChecker {

    private static final Logger log = Logger.getLogger(ZanzibarPermissionChecker.class);

    @Inject
    Instance<RelationshipManager> relationshipManager;

    @Inject
    Instance<ContextAwareRelationshipManager> contextAwareRelationshipManager;

    @Inject
    RelationshipContextManager relationshipContextManager;

    @Inject
    Instance<UserExtractor> userExtractor;

    @Inject
    Instance<DefaultUserTypeResolver> defaultUserTypeResolver;

    @Inject
    Instance<RelationshipCheckContextSupplier> contextSuppliers;

    public Uni<Boolean> isGranted(ZanzibarPermission permission, SecurityIdentity identity) {
        if (!userExtractor.isResolvable()) {
            log.warn("No UserExtractor bean available; denying permission check");
            return Uni.createFrom().item(false);
        }
        if (!relationshipManager.isResolvable()) {
            log.warn("No RelationshipManager bean available; denying permission check");
            return Uni.createFrom().item(false);
        }

        var userType = permission.getUserType().orElseGet(() -> {
            if (!defaultUserTypeResolver.isResolvable()) {
                return null;
            }
            return defaultUserTypeResolver.get().resolve(permission).orElse(null);
        });
        var user = userExtractor.get().extractUser(identity.getPrincipal(), userType);
        if (user.isEmpty()) {
            log.debug("No user extracted for permission check");
            return Uni.createFrom().item(false);
        }

        String objectType = permission.getObjectType();
        String objectId = permission.getObjectId();
        if (objectType == null || objectId == null) {
            log.debug("Missing object type or object id for permission check");
            return Uni.createFrom().item(false);
        }

        User extractedUser = user.get();
        var relationship = Relationship.of(objectType, objectId, permission.getName(),
                extractedUser.type(), extractedUser.id());
        if (log.isDebugEnabled()) {
            log.debugf("Checking relation=%s object=%s:%s user=%s:%s", permission.getName(), objectType, objectId,
                    extractedUser.type(), extractedUser.id());
        }

        try {
            relationshipContextManager.initialize(relationship);
        } catch (IllegalStateException e) {
            log.debug("Relationship context already initialized", e);
        }

        RelationshipCheckContext mergedContext = mergeContext(permission);

        if (contextAwareRelationshipManager.isResolvable()) {
            return contextAwareRelationshipManager.get()
                    .check(relationship, mergedContext)
                    .onItem()
                    .invoke(granted -> {
                        if (log.isDebugEnabled()) {
                            log.debugf("Context-aware relationship check result=%s", granted);
                        }
                    });
        }
        RelationshipManager baseManager = relationshipManager.get();
        if (baseManager instanceof ContextAwareRelationshipManager contextAware) {
            return contextAware.check(relationship, mergedContext)
                    .onItem()
                    .invoke(granted -> {
                        if (log.isDebugEnabled()) {
                            log.debugf("Context-aware relationship check result=%s", granted);
                        }
                    });
        }

        return baseManager.check(relationship)
                .onItem()
                .invoke(granted -> {
                    if (log.isDebugEnabled()) {
                        log.debugf("Relationship check result=%s", granted);
                    }
                });
    }

    private RelationshipCheckContext mergeContext(ZanzibarPermission permission) {
        Map<String, Object> context = new LinkedHashMap<>();
        List<Relationship> tuples = new ArrayList<>();
        Map<String, Object> metadata = new LinkedHashMap<>();

        for (var supplier : contextSuppliers) {
            if (supplier == null) {
                continue;
            }
            var supplied = supplier.get();
            if (supplied == null) {
                continue;
            }
            context.putAll(supplied.context());
            tuples.addAll(supplied.contextualTuples());
            metadata.putAll(supplied.metadata());
        }

        Map<String, Object> permissionContext = permission.getContext();
        if (permissionContext != null) {
            context.putAll(permissionContext);
        }

        List<Relationship> permissionTuples = permission.getContextualTuples();
        if (permissionTuples != null) {
            tuples.addAll(permissionTuples);
        }

        return new RelationshipCheckContext(context, tuples, metadata);
    }
}

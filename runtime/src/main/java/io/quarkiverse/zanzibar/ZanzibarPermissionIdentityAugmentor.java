package io.quarkiverse.zanzibar;

import java.security.Permission;
import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import io.quarkus.arc.Unremovable;
import io.quarkus.security.identity.AuthenticationRequestContext;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.identity.SecurityIdentityAugmentor;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import io.smallrye.mutiny.Uni;

@ApplicationScoped
@Unremovable
public class ZanzibarPermissionIdentityAugmentor implements SecurityIdentityAugmentor {

    @Inject
    ZanzibarPermissionChecker checker;

    @Override
    public Uni<SecurityIdentity> augment(SecurityIdentity identity, AuthenticationRequestContext context,
            Map<String, Object> attributes) {
        return Uni.createFrom().item(QuarkusSecurityIdentity.builder(identity)
                .addPermissionChecker(permission -> checkPermission(permission, identity))
                .build());
    }

    @Override
    public Uni<SecurityIdentity> augment(SecurityIdentity identity, AuthenticationRequestContext context) {
        return augment(identity, context, Map.of());
    }

    private Uni<Boolean> checkPermission(Permission permission, SecurityIdentity identity) {
        if (permission instanceof ZanzibarPermission zanzibarPermission) {
            return checker.isGranted(zanzibarPermission, identity);
        }
        return null;
    }
}

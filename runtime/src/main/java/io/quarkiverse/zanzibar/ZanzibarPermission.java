package io.quarkiverse.zanzibar;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.runtime.QuarkusPermission;
import io.smallrye.mutiny.Uni;

/**
 * Base permission type for Zanzibar authorization checks.
 * <p>
 * The permission name represents the relation. Subclasses supply object type and id.
 */
public abstract class ZanzibarPermission extends QuarkusPermission<ZanzibarPermissionChecker> {

    protected ZanzibarPermission(String relation) {
        super(relation);
    }

    /**
     * @return the object type for the Zanzibar relationship.
     */
    public abstract String getObjectType();

    /**
     * @return the object id for the Zanzibar relationship.
     */
    public abstract String getObjectId();

    /**
     * @return optional user type override.
     */
    public Optional<String> getUserType() {
        return Optional.empty();
    }

    /**
     * @return contextual data for condition/caveat evaluation.
     */
    public Map<String, Object> getContext() {
        return Map.of();
    }

    /**
     * @return contextual tuples to include with the check.
     */
    public List<Relationship> getContextualTuples() {
        return List.of();
    }

    @Override
    protected Class<ZanzibarPermissionChecker> getBeanClass() {
        return ZanzibarPermissionChecker.class;
    }

    @Override
    protected boolean isBlocking() {
        return false;
    }

    @Override
    protected boolean isReactive() {
        return true;
    }

    @Override
    protected boolean isGranted(SecurityIdentity securityIdentity) {
        return isGrantedUni(securityIdentity).await().indefinitely();
    }

    @Override
    protected Uni<Boolean> isGrantedUni(SecurityIdentity securityIdentity) {
        return getBean().isGranted(this, securityIdentity);
    }

}

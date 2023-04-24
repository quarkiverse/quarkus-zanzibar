package io.quarkiverse.zanzibar.jaxrs;

import java.time.Duration;
import java.util.Optional;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.UriInfo;

import org.jboss.logging.Logger;

import io.quarkiverse.zanzibar.RelationshipManager;
import io.quarkiverse.zanzibar.annotations.FGADynamicObject;
import io.quarkiverse.zanzibar.annotations.FGAObject;

public class ZanzibarAuthorizationFilter {

    public static final Logger log = Logger.getLogger(ZanzibarAuthorizationFilter.class);

    public static class Action {

        enum ObjectSource {
            PATH,
            QUERY,
            HEADER,
            REQUEST,
            CONSTANT;

            static ObjectSource from(FGADynamicObject.Source dynamicSource) {
                switch (dynamicSource) {
                    case PATH:
                        return ObjectSource.PATH;
                    case QUERY:
                        return ObjectSource.QUERY;
                    case HEADER:
                        return ObjectSource.HEADER;
                    case REQUEST:
                        return ObjectSource.REQUEST;
                    default:
                        throw new IllegalStateException();
                }
            }
        }

        final String objectType;
        final ObjectSource objectIdSource;
        final String objectIdSourceId;
        final String relation;

        Action(String objectType, ObjectSource objectIdSource, String objectIdSourceId, String relation) {
            this.objectType = objectType;
            this.objectIdSource = objectIdSource;
            this.objectIdSourceId = objectIdSourceId;
            this.relation = relation;
        }

        Action(FGADynamicObject object, String relation) {
            this(object.type(), ObjectSource.from(object.source()), object.sourceProperty(), relation);
        }

        Action(FGAObject object, String relation) {
            this(object.type(), ObjectSource.CONSTANT, object.id(), relation);
        }
    }

    static class Check {
        final String objectType;
        final String objectId;
        final String relation;
        final String user;

        Check(String objectType, String objectId, String relation, String user) {
            this.objectType = objectType;
            this.objectId = objectId;
            this.relation = relation;
            this.user = user;
        }
    }

    Action action;
    RelationshipManager relationshipManager;
    Optional<String> userType;
    Optional<String> unauthenticatedUser;
    Duration timeout;

    protected ZanzibarAuthorizationFilter(Action action, RelationshipManager relationshipManager,
            Optional<String> userType, Optional<String> unauthenticatedUser, Duration timeout) {
        this.action = action;
        this.relationshipManager = relationshipManager;
        this.userType = userType;
        this.unauthenticatedUser = unauthenticatedUser;
        this.timeout = timeout;
    }

    protected Optional<Check> prepare(ContainerRequestContext context) {

        // Determine object id

        Optional<String> objectId;

        UriInfo uriInfo = context.getUriInfo();

        switch (action.objectIdSource) {
            case PATH:
                objectId = Optional.ofNullable(uriInfo.getPathParameters().getFirst(action.objectIdSourceId));
                break;
            case QUERY:
                objectId = Optional.ofNullable(uriInfo.getQueryParameters().getFirst(action.objectIdSourceId));
                break;
            case HEADER:
                objectId = Optional.ofNullable(context.getHeaderString(action.objectIdSourceId));
                break;
            case REQUEST:
                objectId = Optional.ofNullable(context.getProperty(action.objectIdSourceId)).map(Object::toString);
                break;
            case CONSTANT:
                objectId = Optional.of(action.objectIdSourceId);
                break;
            default:
                throw new IllegalStateException("Unsupported ObjectId Source");
        }

        if (objectId.isEmpty()) {
            log.error("Failed to resolve object id");
            return Optional.empty();
        }

        // Determine user

        var principal = context.getSecurityContext().getUserPrincipal();

        String userId;
        if (principal == null || principal.getName() == null) {

            // No principal... map to unauthenticated (if available)

            if (unauthenticatedUser.isEmpty()) {

                log.debug("No use principal and unauthenticated users are disallowed");

                return Optional.empty();
            } else {

                log.debug("No use principal or name, authorizing the unauthenticated user");

                userId = unauthenticatedUser.get();
            }

        } else {

            userId = principal.getName();
        }

        String user = userType.map(type -> type + ":").orElse("") + userId;

        return Optional.of(new Check(action.objectType, objectId.get(), action.relation, user));
    }
}

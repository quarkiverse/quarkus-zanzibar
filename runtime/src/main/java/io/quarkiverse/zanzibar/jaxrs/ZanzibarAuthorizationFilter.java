package io.quarkiverse.zanzibar.jaxrs;

import java.time.Duration;
import java.util.Optional;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.UriInfo;

import org.jboss.logging.Logger;

import io.quarkiverse.zanzibar.RelationshipManager;
import io.quarkiverse.zanzibar.UserIdExtractor;
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
                return switch (dynamicSource) {
                    case PATH -> ObjectSource.PATH;
                    case QUERY -> ObjectSource.QUERY;
                    case HEADER -> ObjectSource.HEADER;
                    case REQUEST -> ObjectSource.REQUEST;
                };
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

    protected static class Check {
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
    UserIdExtractor userIdExtractor;
    Optional<String> userType;
    Optional<String> unauthenticatedUserId;
    Duration timeout;

    protected ZanzibarAuthorizationFilter(Action action,
            RelationshipManager relationshipManager,
            UserIdExtractor userIdExtractor,
            Optional<String> userType,
            Optional<String> unauthenticatedUserId,
            Duration timeout) {
        this.action = action;
        this.relationshipManager = relationshipManager;
        this.userIdExtractor = userIdExtractor;
        this.userType = userType;
        this.unauthenticatedUserId = unauthenticatedUserId;
        this.timeout = timeout;
    }

    protected Optional<Check> prepare(ContainerRequestContext context) {

        // Determine object id

        Optional<String> objectId;

        UriInfo uriInfo = context.getUriInfo();

        objectId = switch (action.objectIdSource) {
            case PATH -> Optional.ofNullable(uriInfo.getPathParameters().getFirst(action.objectIdSourceId));
            case QUERY -> Optional.ofNullable(uriInfo.getQueryParameters().getFirst(action.objectIdSourceId));
            case HEADER -> Optional.ofNullable(context.getHeaderString(action.objectIdSourceId));
            case REQUEST -> Optional.ofNullable(context.getProperty(action.objectIdSourceId)).map(Object::toString);
            case CONSTANT -> Optional.of(action.objectIdSourceId);
        };

        if (objectId.isEmpty()) {
            log.error("Failed to resolve object id");
            return Optional.empty();
        }

        // Determine user

        var principal = context.getSecurityContext().getUserPrincipal();

        return userIdExtractor.extractUserId(principal)
                .or(() -> {
                    // No user-id extracted... map to unauthenticated (if available)
                    return unauthenticatedUserId
                            .map(userId -> {
                                log.debug("No user-id extracted, authorizing the unauthenticated user");
                                return userId;
                            })
                            .or(() -> {
                                log.debug("No user-id extracted and unauthenticated users are disallowed");
                                return Optional.empty();
                            });
                })
                .map(userId -> {
                    // Add user-type (if available) to the user-id
                    var user = userType.map(type -> type + ":").orElse("") + userId;
                    return new Check(action.objectType, objectId.get(), action.relation, user);
                });
    }
}

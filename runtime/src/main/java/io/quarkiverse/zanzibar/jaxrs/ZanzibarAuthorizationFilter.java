package io.quarkiverse.zanzibar.jaxrs;

import java.time.Duration;
import java.util.Optional;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.UriInfo;

import org.jboss.logging.Logger;

import io.quarkiverse.zanzibar.Relationship;
import io.quarkiverse.zanzibar.RelationshipContextManager;
import io.quarkiverse.zanzibar.RelationshipManager;
import io.quarkiverse.zanzibar.UserExtractor;
import io.quarkiverse.zanzibar.UserExtractor.User;
import io.quarkiverse.zanzibar.annotations.FGADynamicObject;
import io.quarkiverse.zanzibar.annotations.FGAObject;
import io.quarkiverse.zanzibar.annotations.FGARelation;

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

    protected sealed interface PrepareResult {
        record Check(Relationship relationship) implements PrepareResult {
        }

        record Pass() implements PrepareResult {
        }

        record Deny() implements PrepareResult {
        }
    }

    Action action;
    RelationshipManager relationshipManager;
    RelationshipContextManager relationshipContextManager;
    UserExtractor userExtractor;
    Optional<String> userType;
    Optional<String> unauthenticatedUserId;
    Duration timeout;

    protected ZanzibarAuthorizationFilter(Action action,
            RelationshipManager relationshipManager,
            RelationshipContextManager relationshipContextManager,
            UserExtractor userExtractor,
            Optional<String> userType,
            Optional<String> unauthenticatedUserId,
            Duration timeout) {
        this.action = action;
        this.relationshipManager = relationshipManager;
        this.relationshipContextManager = relationshipContextManager;
        this.userExtractor = userExtractor;
        this.userType = userType;
        this.unauthenticatedUserId = unauthenticatedUserId;
        this.timeout = timeout;
    }

    protected PrepareResult prepare(ContainerRequestContext context) {

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

        // Determine user
        var extractedUser = userExtractor.extractUser(context.getSecurityContext().getUserPrincipal(), userType.orElse(null))
                .or(() -> {
                    // No user-id extracted... map to unauthenticated (if available)
                    return unauthenticatedUserId
                            .map(userId -> {
                                log.debug("No user-id extracted, authorizing the unauthenticated user");
                                return new User(userType.orElse(""), userId);
                            })
                            .or(() -> {
                                log.debug("No user-id extracted and no unauthenticated user-id provided");
                                return Optional.empty();
                            });
                });

        if (action.relation.equals(FGARelation.ANY)) {
            log.debug("Any relation specified, allowing access");
            relationshipContextManager.initialize(Optional.of(action.objectType), objectId,
                    Optional.of(action.relation), extractedUser.map(User::type).or(() -> userType),
                    extractedUser.map(User::id));
            return new PrepareResult.Pass();
        }

        if (extractedUser.isEmpty() || objectId.isEmpty()) {
            log.debugf("%s not available, denying access", extractedUser.isEmpty() ? "User" : "Object id");
            return new PrepareResult.Deny();
        }

        var user = extractedUser.get();
        var relationship = new Relationship(action.objectType, objectId.get(), action.relation, user.type(), user.id());
        relationshipContextManager.initialize(relationship);

        return new PrepareResult.Check(relationship);
    }
}

package io.quarkiverse.zanzibar.jaxrs;

import java.time.Duration;
import java.util.Optional;

import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.InternalServerErrorException;

import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.server.spi.ResteasyReactiveContainerRequestContext;
import org.jboss.resteasy.reactive.server.spi.ResteasyReactiveContainerRequestFilter;

import io.quarkiverse.zanzibar.Relationship;
import io.quarkiverse.zanzibar.RelationshipManager;
import io.quarkiverse.zanzibar.UserIdExtractor;

public class ZanzibarReactiveAuthorizationFilter extends ZanzibarAuthorizationFilter
        implements ResteasyReactiveContainerRequestFilter {

    private static final Logger log = Logger.getLogger(ZanzibarReactiveAuthorizationFilter.class);

    public ZanzibarReactiveAuthorizationFilter(Action annotations, RelationshipManager relationshipManager,
            UserIdExtractor userIdExtractor, Optional<String> userType,
            Optional<String> unauthenticatedUserId, Duration timeout) {
        super(annotations, relationshipManager, userIdExtractor, userType, unauthenticatedUserId, timeout);
    }

    @Override
    public void filter(ResteasyReactiveContainerRequestContext context) {

        var checkOpt = prepare(context);

        if (checkOpt.isEmpty()) {
            context.resume(new ForbiddenException());
            return;
        }

        context.suspend();

        var check = checkOpt.get();

        log.debugf("Authorizing object-type=%s, object-id=%s, relation=%s, user=%s",
                check.objectType, check.objectId, check.relation, check.user);

        var relationship = Relationship.of(check.objectType, check.objectId, check.relation, check.user);

        relationshipManager.check(relationship)
                .ifNoItem().after(timeout).fail()
                .subscribe().with((allowed) -> {

                    log.debugf("Authorization %s", allowed ? "allowed" : "disallowed");

                    if (!allowed) {

                        context.resume(new ForbiddenException());
                    } else {

                        context.resume();
                    }

                }, (x) -> {
                    log.error("Authorization check failed", x);

                    context.resume(new InternalServerErrorException(x));
                });
    }
}

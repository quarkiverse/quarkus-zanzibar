package io.quarkiverse.zanzibar.jaxrs;

import static javax.ws.rs.core.Response.Status.FORBIDDEN;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;

import java.time.Duration;
import java.util.Optional;

import javax.ws.rs.core.Response;

import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.server.spi.ResteasyReactiveContainerRequestContext;
import org.jboss.resteasy.reactive.server.spi.ResteasyReactiveContainerRequestFilter;

import io.quarkiverse.zanzibar.Relationship;
import io.quarkiverse.zanzibar.RelationshipManager;

public class ZanzibarReactiveAuthorizationFilter extends ZanzibarAuthorizationFilter
        implements ResteasyReactiveContainerRequestFilter {

    private static final Logger log = Logger.getLogger(ZanzibarReactiveAuthorizationFilter.class);

    public ZanzibarReactiveAuthorizationFilter(Action annotations, RelationshipManager relationshipManager,
            Optional<String> unauthenticatedUser, Duration timeout) {
        super(annotations, relationshipManager, unauthenticatedUser, timeout);
    }

    @Override
    public void filter(ResteasyReactiveContainerRequestContext context) {

        var checkOpt = prepare(context);

        if (checkOpt.isEmpty()) {
            context.abortWith(Response.status(FORBIDDEN).build());
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

                        context.abortWith(Response.status(FORBIDDEN).build());
                    }

                    context.resume();

                }, (x) -> {
                    log.error("Authorization check failed", x);

                    context.abortWith(Response.status(INTERNAL_SERVER_ERROR).build());

                    context.resume();
                });
    }
}

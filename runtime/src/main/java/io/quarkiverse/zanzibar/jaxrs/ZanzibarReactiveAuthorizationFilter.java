package io.quarkiverse.zanzibar.jaxrs;

import java.time.Duration;
import java.util.Optional;

import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.InternalServerErrorException;

import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.server.spi.ResteasyReactiveContainerRequestContext;
import org.jboss.resteasy.reactive.server.spi.ResteasyReactiveContainerRequestFilter;

import io.quarkiverse.zanzibar.RelationshipContextManager;
import io.quarkiverse.zanzibar.RelationshipManager;
import io.quarkiverse.zanzibar.UserExtractor;

public class ZanzibarReactiveAuthorizationFilter extends ZanzibarAuthorizationFilter
        implements ResteasyReactiveContainerRequestFilter {

    private static final Logger log = Logger.getLogger(ZanzibarReactiveAuthorizationFilter.class);

    public ZanzibarReactiveAuthorizationFilter(Action annotations, RelationshipManager relationshipManager,
            RelationshipContextManager relationshipContextManager, UserExtractor userExtractor,
            Optional<String> userType, Optional<String> unauthenticatedUserId, Duration timeout) {
        super(annotations, relationshipManager, relationshipContextManager, userExtractor, userType, unauthenticatedUserId,
                timeout);
    }

    @Override
    public void filter(ResteasyReactiveContainerRequestContext context) {

        var prepared = prepare(context);

        if (prepared instanceof PrepareResult.Deny) {
            context.resume(new ForbiddenException());
            return;
        } else if (prepared instanceof PrepareResult.Pass) {
            log.debugf("Authorization allowed, skipping check");
            context.resume();
            return;
        }

        var relationship = ((PrepareResult.Check) prepared).relationship();

        context.suspend();

        log.debugf("Authorizing %s", relationship);

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

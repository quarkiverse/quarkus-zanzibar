package io.quarkiverse.zanzibar.jaxrs;

import java.time.Duration;
import java.util.Optional;

import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;

import org.jboss.logging.Logger;

import io.quarkiverse.zanzibar.RelationshipContextManager;
import io.quarkiverse.zanzibar.RelationshipManager;
import io.quarkiverse.zanzibar.UserExtractor;

public class ZanzibarSynchronousAuthorizationFilter extends ZanzibarAuthorizationFilter implements ContainerRequestFilter {

    private static final Logger log = Logger.getLogger(ZanzibarSynchronousAuthorizationFilter.class);

    public ZanzibarSynchronousAuthorizationFilter(Action action, RelationshipManager relationshipManager,
            RelationshipContextManager relationshipContextManager, UserExtractor userExtractor,
            Optional<String> userType, Optional<String> unauthenticatedUserId, Duration timeout) {
        super(action, relationshipManager, relationshipContextManager, userExtractor, userType, unauthenticatedUserId,
                timeout);
    }

    @Override
    public void filter(ContainerRequestContext context) {

        var prepared = prepare(context);

        if (prepared instanceof PrepareResult.Deny) {
            throw new ForbiddenException();
        } else if (prepared instanceof PrepareResult.Pass) {
            log.debugf("Authorization allowed, skipping check");
            return;
        }

        var relationship = ((PrepareResult.Check) prepared).relationship();

        log.debugf("Authorizing %s", relationship);

        try {

            var allowed = relationshipManager.check(relationship)
                    .await().atMost(Duration.ofSeconds(10));

            log.debugf("Authorization %s", allowed ? "allowed" : "disallowed");

            if (!allowed) {
                throw new ForbiddenException();
            }

        } catch (Throwable x) {

            log.error("Authorization check failed", x);

            throw new InternalServerErrorException(x);
        }
    }
}

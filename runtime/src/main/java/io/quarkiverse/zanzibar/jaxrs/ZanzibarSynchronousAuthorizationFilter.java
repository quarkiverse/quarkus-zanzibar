package io.quarkiverse.zanzibar.jaxrs;

import static jakarta.ws.rs.core.Response.Status.FORBIDDEN;
import static jakarta.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;

import java.time.Duration;
import java.util.Optional;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;

import org.jboss.logging.Logger;

import io.quarkiverse.zanzibar.Relationship;
import io.quarkiverse.zanzibar.RelationshipManager;
import io.quarkiverse.zanzibar.ZanzibarUserIdExtractor;

public class ZanzibarSynchronousAuthorizationFilter extends ZanzibarAuthorizationFilter implements ContainerRequestFilter {

    private static final Logger log = Logger.getLogger(ZanzibarSynchronousAuthorizationFilter.class);

    public ZanzibarSynchronousAuthorizationFilter(Action action, RelationshipManager relationshipManager,
            ZanzibarUserIdExtractor userIdExtractor,
            Optional<String> userType, Optional<String> unauthenticatedUserId, Duration timeout) {
        super(action, relationshipManager, userIdExtractor, userType, unauthenticatedUserId, timeout);
    }

    @Override
    public void filter(ContainerRequestContext context) {

        var checkOpt = prepare(context);

        if (checkOpt.isEmpty()) {
            context.abortWith(Response.status(FORBIDDEN).build());
            return;
        }
        var check = checkOpt.get();

        try {

            var relationship = Relationship.of(check.objectType, check.objectId, check.relation, check.user);

            var allowed = relationshipManager.check(relationship)
                    .await().atMost(Duration.ofSeconds(10));

            log.debugf("Authorization %s", allowed ? "allowed" : "disallowed");

            if (!allowed) {
                context.abortWith(Response.status(FORBIDDEN).build());
            }

        } catch (Throwable x) {

            log.error("Authorization check failed", x);

            context.abortWith(Response.status(INTERNAL_SERVER_ERROR).build());

        }
    }
}

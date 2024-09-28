package io.quarkiverse.zanzibar.jaxrs;

import java.time.Duration;
import java.util.Optional;

import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;

import org.jboss.logging.Logger;

import io.quarkiverse.zanzibar.Relationship;
import io.quarkiverse.zanzibar.RelationshipManager;
import io.quarkiverse.zanzibar.UserIdExtractor;

public class ZanzibarSynchronousAuthorizationFilter extends ZanzibarAuthorizationFilter implements ContainerRequestFilter {

    private static final Logger log = Logger.getLogger(ZanzibarSynchronousAuthorizationFilter.class);

    public ZanzibarSynchronousAuthorizationFilter(Action action, RelationshipManager relationshipManager,
            UserIdExtractor userIdExtractor,
            Optional<String> userType, Optional<String> unauthenticatedUserId, Duration timeout) {
        super(action, relationshipManager, userIdExtractor, userType, unauthenticatedUserId, timeout);
    }

    @Override
    public void filter(ContainerRequestContext context) {

        var checkOpt = prepare(context);

        if (checkOpt.isEmpty()) {
            throw new ForbiddenException();
        }
        var check = checkOpt.get();

        try {

            var relationship = Relationship.of(check.objectType, check.objectId, check.relation, check.user);

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

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.quarkiverse.zanzibar.openfga.it;

import java.util.List;
import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;

import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.RestPath;
import org.jboss.resteasy.reactive.RestQuery;

import io.quarkiverse.zanzibar.Relationship;
import io.quarkiverse.zanzibar.RelationshipCheckContext;
import io.quarkiverse.zanzibar.RelationshipCheckContextSupplier;
import io.quarkiverse.zanzibar.RelationshipManager;
import io.quarkiverse.zanzibar.UserExtractor;
import io.quarkus.security.PermissionsAllowed;
import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.mutiny.Uni;

@Path("/openfga")
@ApplicationScoped
public class ZanzibarOpenFGAResource {

    @Inject
    RelationshipManager relationshipManager;
    @Inject
    UserExtractor userExtractor;
    @Inject
    SecurityIdentity identity;

    @GET
    @Path("contextual/things/{objectId}")
    @PermissionsAllowed(value = "reader", permission = ContextualTuplesPermission.class)
    public String contextualThing(@RestPath String objectId, @RestQuery String userId) {
        return "Thing " + objectId;
    }

    @GET
    @Path("context/things/{objectId}")
    @PermissionsAllowed(value = "reader", permission = ContextMergePermission.class)
    public String contextThing(@RestPath String objectId) {
        return "Thing " + objectId;
    }

    @POST
    @Path("jwt/authorize")
    public Uni<Void> jwtAuthorize(@QueryParam("relation") String relation, @QueryParam("object") String objectId) {
        var user = userExtractor.extractUser(identity.getPrincipal(), null)
                .orElseThrow(() -> new IllegalStateException("User not available"));
        var relationship = Relationship.of("thing", objectId, relation, user.type(), user.id());
        return relationshipManager.add(List.of(relationship));
    }
}

@ApplicationScoped
class ContextSupplier implements RelationshipCheckContextSupplier {

    @Inject
    Logger logger;

    @Override
    public RelationshipCheckContext get() {
        logger.info("getContext called");
        return new RelationshipCheckContext(
                Map.of(
                        "k1", "supplier",
                        "k3", "supplier"),
                List.of(Relationship.of("thing", "supplier", "reader", "user", "supplier")),
                Map.of());
    }

}

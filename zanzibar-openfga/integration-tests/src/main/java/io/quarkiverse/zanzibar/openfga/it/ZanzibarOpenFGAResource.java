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

import static io.quarkiverse.zanzibar.annotations.FGADynamicObject.Source.PATH;
import static io.quarkiverse.zanzibar.annotations.FGARelation.ANY;

import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;

import org.jboss.logging.Logger;

import io.quarkiverse.openfga.client.AuthorizationModelClient;
import io.quarkiverse.openfga.client.model.RelTupleKey;
import io.quarkiverse.zanzibar.Relationship;
import io.quarkiverse.zanzibar.RelationshipContext;
import io.quarkiverse.zanzibar.RelationshipManager;
import io.quarkiverse.zanzibar.annotations.FGADynamicObject;
import io.quarkiverse.zanzibar.annotations.FGARelation;
import io.quarkiverse.zanzibar.annotations.FGAUserType;
import io.quarkiverse.zanzibar.openfga.OpenFGAContextSupplier;
import io.smallrye.mutiny.Uni;

@FGADynamicObject(source = PATH, sourceProperty = "id", type = "thing")
interface Things {
    @FGARelation(ANY)
    @POST
    @Path("ann/authorize")
    @FGAUserType("user")
    Uni<Void> annotationAuthorize(@QueryParam("relation") String relation, @QueryParam("object") String objectId);

    @FGARelation(ANY)
    @POST
    @Path("jwt/authorize")
    Uni<Void> jwtAuthorize(@QueryParam("relation") String relation, @QueryParam("object") String objectId);
}

@Path("/openfga")
@ApplicationScoped
@FGARelation("reader")
public class ZanzibarOpenFGAResource implements Things {

    @Inject
    RelationshipManager relationshipManager;
    @Inject
    RelationshipContext relationshipContext;

    @Override
    public Uni<Void> annotationAuthorize(String relation, String objectId) {
        return authorize(relation, objectId);
    }

    @Override
    public Uni<Void> jwtAuthorize(String relation, String objectId) {
        return authorize(relation, objectId);
    }

    public Uni<Void> authorize(String relation, String objectId) {
        String objectType = relationshipContext.objectType()
                .orElseThrow(() -> new IllegalStateException("Object type not available in context"));
        String userType = relationshipContext.userType()
                .orElseThrow(() -> new IllegalStateException("User type not available in context"));
        String userId = relationshipContext.userId()
                .orElseThrow(() -> new IllegalStateException("User ID not available in context"));
        var relationship = Relationship.of(objectType, objectId, relation, userType, userId);
        return relationshipManager.add(List.of(relationship));
    }

    @GET
    @Path("ann/things/{id}")
    @FGAUserType("user")
    public String annotationGetThing(@PathParam("id") String id) {
        return "Thing " + id;
    }

    @GET
    @Path("jwt/things/{id}")
    public String jwtGetThing(@PathParam("id") String id) {
        return "Thing " + id;
    }
}

@ApplicationScoped
class ContextSupplier implements OpenFGAContextSupplier {

    @Inject
    Logger logger;

    @Override
    public Result getContext(@Nonnull AuthorizationModelClient client, @Nonnull RelTupleKey relTupleKey) {
        logger.info("getContext called");
        return new Result(Map.of(), List.of());
    }
}

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
package io.quarkiverse.zanzibar.authzed.it;

import static io.quarkiverse.zanzibar.annotations.FGADynamicObject.Source.PATH;
import static io.quarkiverse.zanzibar.annotations.FGARelation.ANY;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;

import io.quarkiverse.zanzibar.Relationship;
import io.quarkiverse.zanzibar.RelationshipContext;
import io.quarkiverse.zanzibar.RelationshipManager;
import io.quarkiverse.zanzibar.annotations.FGADynamicObject;
import io.quarkiverse.zanzibar.annotations.FGARelation;
import io.quarkiverse.zanzibar.annotations.FGAUserType;
import io.smallrye.mutiny.Uni;

@FGADynamicObject(source = PATH, sourceProperty = "id", type = "thing")
@FGAUserType("user")
interface Things {
    @FGARelation(ANY)
    @POST
    @Path("authorize")
    Uni<Void> authorize(@QueryParam("relation") String relation, @QueryParam("object") String objectId);
}

@Path("/authzed")
@ApplicationScoped
@FGARelation("reader")
public class ZanzibarAuthzedResource implements Things {

    @Inject
    RelationshipManager relationshipManager;
    @Inject
    RelationshipContext relationshipContext;

    public Uni<Void> authorize(String relation, String objectId) {
        String objectType = relationshipContext.objectType()
                .orElseThrow(() -> new IllegalStateException("Object type not available in context"));
        String userType = relationshipContext.userType()
                .orElseThrow(() -> new IllegalStateException("User type not available in context"));
        String userId = relationshipContext.userId()
                .orElseThrow(() -> new IllegalStateException("User ID not available in context"));
        return relationshipManager.add(List.of(Relationship.of(objectType, objectId, relation, userType, userId)));
    }

    @GET
    @Path("things/{id}")
    public String getThing(@PathParam("id") String id) {
        return "Thing " + id;
    }
}

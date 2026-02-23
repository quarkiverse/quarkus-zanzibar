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

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;

import com.authzed.api.v1.ContextualizedCaveat;
import com.authzed.api.v1.ObjectReference;
import com.authzed.api.v1.RelationshipUpdate;
import com.authzed.api.v1.RelationshipUpdate.Operation;
import com.authzed.api.v1.SubjectReference;
import com.authzed.api.v1.WriteRelationshipsRequest;

import io.quarkiverse.authzed.client.AuthzedClient;
import io.quarkus.security.PermissionsAllowed;
import io.smallrye.mutiny.Uni;

@Path("/authzed")
@ApplicationScoped
public class ZanzibarAuthzedResource {

    @Inject
    AuthzedClient authzedClient;

    @POST
    @Path("caveat/authorize")
    public Uni<Void> authorizeCaveat(@QueryParam("relation") String relation, @QueryParam("object") String objectId,
            @QueryParam("user") String userId) {
        var relationship = com.authzed.api.v1.Relationship.newBuilder()
                .setRelation(relation)
                .setResource(ObjectReference.newBuilder()
                        .setObjectType("thing")
                        .setObjectId(objectId)
                        .build())
                .setSubject(SubjectReference.newBuilder()
                        .setObject(ObjectReference.newBuilder()
                                .setObjectType("user")
                                .setObjectId(userId)
                                .build())
                        .build())
                .setOptionalCaveat(ContextualizedCaveat.newBuilder()
                        .setCaveatName("region_is_us")
                        .build())
                .build();

        var request = WriteRelationshipsRequest.newBuilder()
                .addUpdates(RelationshipUpdate.newBuilder()
                        .setOperation(Operation.OPERATION_CREATE)
                        .setRelationship(relationship)
                        .build())
                .build();

        return authzedClient.v1().permissionService().writeRelationships(request)
                .replaceWithVoid();
    }

    @GET
    @Path("caveat/things/{id}")
    @PermissionsAllowed(value = "caveated_reader", permission = CaveatedThingPermission.class)
    public String getCaveatedThing(@PathParam("id") String id) {
        return "Thing " + id;
    }

    @GET
    @Path("caveat-missing/things/{id}")
    @PermissionsAllowed(value = "caveated_reader", permission = MissingContextThingPermission.class)
    public String getCaveatedThingMissingContext(@PathParam("id") String id) {
        return "Thing " + id;
    }
}

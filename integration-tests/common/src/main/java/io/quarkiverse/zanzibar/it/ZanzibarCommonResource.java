package io.quarkiverse.zanzibar.it;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import org.jboss.resteasy.reactive.RestCookie;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.RestHeader;
import org.jboss.resteasy.reactive.RestMatrix;
import org.jboss.resteasy.reactive.RestPath;
import org.jboss.resteasy.reactive.RestQuery;

import io.quarkiverse.zanzibar.Relationship;
import io.quarkiverse.zanzibar.RelationshipContext;
import io.quarkiverse.zanzibar.RelationshipManager;
import io.quarkiverse.zanzibar.UserExtractor;
import io.quarkus.security.PermissionsAllowed;
import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.mutiny.Uni;

@Path("/zanzibar")
@ApplicationScoped
public class ZanzibarCommonResource {

    @Inject
    RelationshipManager relationshipManager;
    @Inject
    UserExtractor userExtractor;
    @Inject
    SecurityIdentity identity;
    @Inject
    RelationshipContext relationshipContext;

    @POST
    @Path("authorize")
    public Uni<Void> authorize(@QueryParam("relation") String relation, @QueryParam("object") String objectId,
            @QueryParam("userType") String userType) {
        var user = userExtractor.extractUser(identity.getPrincipal(), userType)
                .orElseThrow(() -> new IllegalStateException("User not available"));
        var relationship = Relationship.of("thing", objectId, relation, user.type(), user.id());
        return relationshipManager.add(List.of(relationship));
    }

    @GET
    @Path("ann/things/{id}")
    @PermissionsAllowed(value = "reader", permission = ThingUserPermission.class, params = "id")
    public String annotationGetThing(@PathParam("id") String id) {
        return "Thing " + id;
    }

    @GET
    @Path("jwt/things/{id}")
    @PermissionsAllowed(value = "reader", permission = ThingPermission.class, params = "id")
    public String jwtGetThing(@PathParam("id") String id) {
        return "Thing " + id;
    }

    @GET
    @Path("rest/things/{pathId}")
    @PermissionsAllowed(value = "reader", permission = RestParamsPermission.class)
    public String restGetThing(@RestPath String pathId,
            @RestQuery("qid") String queryId,
            @RestHeader("X-Thing-Id") String headerId,
            @RestCookie("thing-id") String cookieId,
            @RestMatrix("m") String matrixId) {
        return "Thing " + pathId;
    }

    @POST
    @Path("rest/forms")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @PermissionsAllowed(value = "reader", permission = RestFormPermission.class)
    public String restFormThing(@RestForm("formId") String formId) {
        return "Thing " + formId;
    }

    @GET
    @Path("relationship/reader/{id}")
    @PermissionsAllowed(value = "reader", permission = ThingPermission.class, params = "id")
    public String readerRelationshipContext(@PathParam("id") String id) {
        return relationshipContextSummary();
    }

    @GET
    @Path("relationship/writer/{id}")
    @PermissionsAllowed(value = "writer", permission = ThingPermission.class, params = "id")
    public String writerRelationshipContext(@PathParam("id") String id) {
        return relationshipContextSummary();
    }

    private String relationshipContextSummary() {
        return "relation=" + relationshipContext.relation().orElse("<missing>")
                + ";object=" + relationshipContext.objectType().orElse("<missing>") + ":"
                + relationshipContext.objectId().orElse("<missing>")
                + ";user=" + relationshipContext.userType().orElse("<missing>") + ":"
                + relationshipContext.userId().orElse("<missing>");
    }
}

package io.quarkiverse.zanzibar.it;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

import io.quarkiverse.zanzibar.ZanzibarPermissionIdentityAugmentor;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.smallrye.jwt.build.Jwt;

@QuarkusTest
public class ZanzibarResourceTest {

    @Test
    public void testUsingAllAnnotations() {
        var jwt = Jwt.subject("ann-some-guy").audience("everybody").sign();

        given()
                .auth().preemptive().oauth2(jwt)
                .when().get("/zanzibar/ann/things/1")
                .then()
                .statusCode(403);

        given()
                .auth().preemptive().oauth2(jwt)
                .when().post("/zanzibar/authorize?object=1&relation=reader&userType=user")
                .then()
                .statusCode(204);

        given()
                .auth().preemptive().oauth2(jwt)
                .when().get("/zanzibar/ann/things/1")
                .then()
                .statusCode(200)
                .body(is("Thing 1"));
    }

    @Test
    public void testUsingUserTypeFromJwt() {
        var jwt = Jwt.subject("jwt-some-guy").groups("user").audience("everybody").sign();

        given()
                .auth().preemptive().oauth2(jwt)
                .when().get("/zanzibar/jwt/things/1")
                .then()
                .statusCode(403);

        given()
                .auth().preemptive().oauth2(jwt)
                .when().post("/zanzibar/authorize?object=1&relation=reader")
                .then()
                .statusCode(204);

        given()
                .auth().preemptive().oauth2(jwt)
                .when().get("/zanzibar/jwt/things/1")
                .then()
                .statusCode(200)
                .body(is("Thing 1"));
    }

    @Test
    @TestSecurity(user = "test-guy", augmentors = ZanzibarPermissionIdentityAugmentor.class)
    public void testTestSecurityPermissionsAllowed() {
        String objectId = "test-security-1";

        given()
                .when().get("/zanzibar/jwt/things/" + objectId)
                .then()
                .statusCode(403);

        given()
                .when().post("/zanzibar/authorize?object=" + objectId + "&relation=reader&userType=resource")
                .then()
                .statusCode(204);

        given()
                .when().get("/zanzibar/jwt/things/" + objectId)
                .then()
                .statusCode(200)
                .body(is("Thing " + objectId));
    }

    @Test
    public void testRestParameterMapping() {
        var jwt = Jwt.subject("rest-some-guy").groups("user").audience("everybody").sign();

        String pathId = "path-1";
        String queryId = "query-1";
        String headerId = "header-1";
        String cookieId = "cookie-1";
        String matrixId = "matrix-1";
        String objectId = String.join("|", pathId, queryId, headerId, cookieId, matrixId);

        given()
                .urlEncodingEnabled(false)
                .auth().preemptive().oauth2(jwt)
                .header("X-Thing-Id", headerId)
                .cookie("thing-id", cookieId)
                .when().get("/zanzibar/rest/things/" + pathId + ";m=" + matrixId + "?qid=" + queryId)
                .then()
                .statusCode(403);

        given()
                .auth().preemptive().oauth2(jwt)
                .when().post("/zanzibar/authorize?object=" + objectId + "&relation=reader")
                .then()
                .statusCode(204);

        given()
                .urlEncodingEnabled(false)
                .auth().preemptive().oauth2(jwt)
                .header("X-Thing-Id", headerId)
                .cookie("thing-id", cookieId)
                .when().get("/zanzibar/rest/things/" + pathId + ";m=" + matrixId + "?qid=" + queryId)
                .then()
                .statusCode(200)
                .body(is("Thing " + pathId));
    }

    @Test
    public void testRestFormParameterMapping() {
        var jwt = Jwt.subject("form-some-guy").groups("user").audience("everybody").sign();

        String formId = "form-1";

        given()
                .auth().preemptive().oauth2(jwt)
                .contentType("application/x-www-form-urlencoded")
                .formParam("formId", formId)
                .when().post("/zanzibar/rest/forms")
                .then()
                .statusCode(403);

        given()
                .auth().preemptive().oauth2(jwt)
                .when().post("/zanzibar/authorize?object=" + formId + "&relation=reader")
                .then()
                .statusCode(204);

        given()
                .auth().preemptive().oauth2(jwt)
                .contentType("application/x-www-form-urlencoded")
                .formParam("formId", formId)
                .when().post("/zanzibar/rest/forms")
                .then()
                .statusCode(200)
                .body(is("Thing " + formId));
    }

    @Test
    public void testRelationshipContextMultiplePermissions() {
        var jwt = Jwt.subject("rel-context-guy").groups("user").audience("everybody").sign();
        String readerObjectId = "rel-a";
        String writerObjectId = "rel-b";

        given()
                .auth().preemptive().oauth2(jwt)
                .when().post("/zanzibar/authorize?object=" + readerObjectId + "&relation=reader")
                .then()
                .statusCode(204);

        given()
                .auth().preemptive().oauth2(jwt)
                .when().post("/zanzibar/authorize?object=" + writerObjectId + "&relation=writer")
                .then()
                .statusCode(204);

        given()
                .auth().preemptive().oauth2(jwt)
                .when().get("/zanzibar/relationship/reader/" + readerObjectId)
                .then()
                .statusCode(200)
                .body(is("relation=reader;object=thing:" + readerObjectId + ";user=user:rel-context-guy"));

        given()
                .auth().preemptive().oauth2(jwt)
                .when().get("/zanzibar/relationship/writer/" + writerObjectId)
                .then()
                .statusCode(200)
                .body(is("relation=writer;object=thing:" + writerObjectId + ";user=user:rel-context-guy"));
    }

    @Test
    public void testGlobalDefaultUserType() {
        var jwt = Jwt.subject("global-guy").audience("everybody").sign();
        String objectId = "global-1";

        given()
                .auth().preemptive().oauth2(jwt)
                .when().get("/zanzibar-global/things/" + objectId)
                .then()
                .statusCode(403);

        given()
                .auth().preemptive().oauth2(jwt)
                .when().post("/zanzibar/authorize?object=" + objectId + "&relation=reader&userType=global")
                .then()
                .statusCode(204);

        given()
                .auth().preemptive().oauth2(jwt)
                .when().get("/zanzibar-global/things/" + objectId)
                .then()
                .statusCode(200)
                .body(is("Thing " + objectId));
    }

    @Test
    public void testResourceDefaultUserType() {
        var jwt = Jwt.subject("resource-guy").audience("everybody").sign();
        String objectId = "resource-1";

        given()
                .auth().preemptive().oauth2(jwt)
                .when().get("/zanzibar/jwt/things/" + objectId)
                .then()
                .statusCode(403);

        given()
                .auth().preemptive().oauth2(jwt)
                .when().post("/zanzibar/authorize?object=" + objectId + "&relation=reader&userType=resource")
                .then()
                .statusCode(204);

        given()
                .auth().preemptive().oauth2(jwt)
                .when().get("/zanzibar/jwt/things/" + objectId)
                .then()
                .statusCode(200)
                .body(is("Thing " + objectId));
    }

    @Test
    public void testPermissionOverrideUserType() {
        var jwt = Jwt.subject("perm-guy").audience("everybody").sign();
        String objectId = "perm-1";

        given()
                .auth().preemptive().oauth2(jwt)
                .when().get("/zanzibar-override/things/" + objectId)
                .then()
                .statusCode(403);

        given()
                .auth().preemptive().oauth2(jwt)
                .when().post("/zanzibar/authorize?object=" + objectId + "&relation=reader&userType=perm")
                .then()
                .statusCode(204);

        given()
                .auth().preemptive().oauth2(jwt)
                .when().get("/zanzibar-override/things/" + objectId)
                .then()
                .statusCode(200)
                .body(is("Thing " + objectId));
    }
}

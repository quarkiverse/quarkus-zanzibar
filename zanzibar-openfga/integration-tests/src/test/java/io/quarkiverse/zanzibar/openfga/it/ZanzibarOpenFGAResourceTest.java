package io.quarkiverse.zanzibar.openfga.it;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.quarkiverse.zanzibar.Relationship;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.jwt.build.Jwt;

@QuarkusTest
public class ZanzibarOpenFGAResourceTest {

    @Test
    public void testContextualTuples() {
        var jwt = Jwt.subject("ctx-user").groups("user").audience("everybody").sign();
        String objectId = "ctx-1";

        TestOpenFGARelationshipManager.reset();

        given()
                .auth().preemptive().oauth2(jwt)
                .when().get("/openfga/contextual/things/" + objectId + "?userId=ctx-user")
                .then()
                .statusCode(200)
                .body(is("Thing " + objectId));

        var context = TestOpenFGARelationshipManager.lastContext;
        Assertions.assertNotNull(context);
        Assertions.assertTrue(context.contextualTuples().stream()
                .anyMatch(tuple -> tuple.equals(Relationship.of("thing", objectId, "reader", "user", "ctx-user"))));
    }

    @Test
    public void testContextMerge() {
        var jwt = Jwt.subject("ctx-merge-guy").groups("user").audience("everybody").sign();
        String objectId = "merge-1";

        given()
                .auth().preemptive().oauth2(jwt)
                .when().get("/openfga/context/things/" + objectId)
                .then()
                .statusCode(403);

        given()
                .auth().preemptive().oauth2(jwt)
                .when().post("/openfga/jwt/authorize?object=" + objectId + "&relation=reader")
                .then()
                .statusCode(204);

        TestOpenFGARelationshipManager.reset();

        given()
                .auth().preemptive().oauth2(jwt)
                .when().get("/openfga/context/things/" + objectId)
                .then()
                .statusCode(200)
                .body(is("Thing " + objectId));

        var context = TestOpenFGARelationshipManager.lastContext;
        Assertions.assertNotNull(context);
        Assertions.assertEquals("permission", context.context().get("k1"));
        Assertions.assertEquals("permission", context.context().get("k2"));
        Assertions.assertEquals("supplier", context.context().get("k3"));

        var tuples = context.contextualTuples();
        Assertions.assertEquals(2, tuples.size());
        Assertions.assertEquals(Relationship.of("thing", "supplier", "reader", "user", "supplier"), tuples.get(0));
        Assertions.assertEquals(Relationship.of("thing", "permission", "reader", "user", "permission"), tuples.get(1));
    }

}

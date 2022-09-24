package io.quarkiverse.zanzibar.authzed.it;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.jwt.build.Jwt;

@QuarkusTest
public class ZanzibarAuthzedResourceTest {

    @Test
    public void testHelloEndpoint() {
        var jwt = Jwt.subject("user:some-guy").audience("everybody").sign();

        given()
                .auth().preemptive().oauth2(jwt)
                .when().get("/authzed/things/1")
                .then()
                .statusCode(403);

        given()
                .auth().preemptive().oauth2(jwt)
                .when().post("/authzed/authorize/user:some-guy?object=1&relation=reader")
                .then()
                .statusCode(204);

        given()
                .auth().preemptive().oauth2(jwt)
                .when().get("/authzed/things/1")
                .then()
                .statusCode(200)
                .body(is("Thing 1"));
    }
}

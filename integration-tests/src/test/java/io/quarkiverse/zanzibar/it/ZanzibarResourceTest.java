package io.quarkiverse.zanzibar.it;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class ZanzibarResourceTest {

    /** Method-level ignores allow requests without authentication. */
    @Test
    public void testIgnoredMethod() {
        given().when().get("/zanzibar/ignored").then().statusCode(200).body(is("Ignored method"));
    }

    /** Class-level ignores allow requests without authentication. */
    @Test
    public void testIgnoredClass() {
        given().when().get("/ignored").then().statusCode(200).body(is("Ignored class"));
    }

    /** Endpoints without FGA annotations remain denied by default. */
    @Test
    public void testUnannotatedMethod() {
        given().when().get("/zanzibar").then().statusCode(403);
    }
}

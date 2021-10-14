package ru.backend.tests;

import org.junit.jupiter.api.Test;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class AccountTests extends BaseTest {
    @Test
    void getAccountInfoTest() {
        given()
                .spec(requestSpecificationWithAuth)
                .when()
                .get("https://api.imgur.com/3/account/{username}", username);
    }

    @Test
    void getAccountInfoWithLoggingTest() {
        given()
                .header("Authorization", "Bearer e6c67f69b5d5ca42ec9f1c7b45e057ddd5c4269b")
                .log()
                .method()
                .log()
                .uri()
                .when()
                .get("https://api.imgur.com/3/account/{username}", username)
                .prettyPeek()
                .then()
                .statusCode(200);
    }

    @Test
    void getAccountInfoWithAssertionsInGivenTest() {
        given()
                .header("Authorization", token)
                .log()
                .method()
                .log()
                .uri()
                .expect()
                .statusCode(200)
                .body("data.url", equalTo(username))
                .body("success", equalTo(true))
                .body("status", equalTo(200))
                .contentType("application/json")
                .when()
                .get("https://api.imgur.com/3/account/{username}", username)
                .prettyPeek();
    }
}

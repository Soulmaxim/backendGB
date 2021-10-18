package ru.backend.tests;

import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.junit.jupiter.api.BeforeAll;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static org.hamcrest.Matchers.*;

public abstract class BaseTest {
    static ResponseSpecification positiveResponseSpecification;
    static ResponseSpecification notFoundResponseSpecification;
    static ResponseSpecification badResponseSpecification;
    static RequestSpecification requestSpecificationWithAuth;
    static RequestSpecification requestSpecificationWithAuthAndForm;

    static Properties properties = new Properties();
    static String token;
    static String username;

    @BeforeAll
    static void beforeAll() {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        RestAssured.filters(new AllureRestAssured());
        RestAssured.baseURI = "https://api.imgur.com/3";
        getProperties();
        token = properties.getProperty("token");
        username = properties.getProperty("username");

        positiveResponseSpecification = new ResponseSpecBuilder()
                .expectBody("status", equalTo(200))
                .expectBody("success", is(true))
                .expectContentType(ContentType.JSON)
                .expectStatusCode(200)
                .build();

        notFoundResponseSpecification = new ResponseSpecBuilder()
                .expectStatusCode(404)
                .expectContentType(ContentType.HTML)
                .build();

        requestSpecificationWithAuth = new RequestSpecBuilder()
                .addHeader("Authorization", token)
                .build();

        requestSpecificationWithAuthAndForm = new RequestSpecBuilder()
                .addRequestSpecification(requestSpecificationWithAuth)
                .addFormParam("title", "File title")
                .addFormParam("description", "File description")
                .build();

        badResponseSpecification = new ResponseSpecBuilder()
                .expectBody("status", equalTo(400))
                .expectBody("success", is(false))
                .expectContentType(ContentType.JSON)
                .expectStatusCode(400)
                .build();

//        RestAssured.responseSpecification = positiveResponseSpecification;
    }

    private static void getProperties() {
        try (InputStream output = new FileInputStream("src/test/resources/application.properties")) {
            properties.load(output);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

package ru.backend.tests;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Base64;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

// не понятно как правильно делать сценарии - вызывать функции-тесты или писать заново
// не понятно как получить json при 404, а не html
// как сохранить одновременно и id и deletehash

public class ImageTests extends BaseTest {
    private final String PATH_TO_IMAGE = "src/test/resources/image1.jpg";
    private final String PATH_TO_NOT_AN_IMAGE = "src/test/resources/not an image.txt";
    private final String PATH_TO_EMPTY_IMAGE = "src/test/resources/empty.jpg";
    private final String PATH_TO_VIDEO = "src/test/resources/video.mp4";
    static String encodedFile;
    String uploadedFileId;

    @BeforeEach
    void beforeTest() {
        byte[] byteArray = getFileContent();
        encodedFile = Base64.getEncoder().encodeToString(byteArray);
    }

    @Test
    void uploadImageTest() {
        uploadedFileId = given()
                .header("Authorization", token)
                .multiPart("image", encodedFile)
                .formParam("title", "ImageTitle")
                .expect()
                .body("success", is(true))
                .body("status", equalTo(200))
                .body("data.id", is(notNullValue()))
                .contentType("application/json")
                .when()
                .post("https://api.imgur.com/3/image")
                .prettyPeek()
                .then()
                .extract()
                .response()
                .jsonPath()
                .getString("data.deletehash");
    }

    @Test
    void uploadNothing() {
        uploadedFileId = given()
                .header("Authorization", token)
                .formParam("title", "ImageTitle")
                .expect()
                .statusCode(400)
                .body("success", is(false))
                .body("data.error", equalTo("Bad Request"))
                .contentType("application/json")
                .when()
                .post("https://api.imgur.com/3/upload")
                .prettyPeek()
                .then()
                .extract()
                .response()
                .jsonPath()
                .getString("data.deletehash");
    }

    @Test
    void uploadNotAnImageTest() {
        uploadedFileId = given()
                .header("Authorization", token)
                .multiPart("image", new File(PATH_TO_NOT_AN_IMAGE))
                .formParam("title", "ImageTitle")
                .expect()
                .statusCode(400)
                .body("success", is(false))
                .body("data.error", equalTo("We don't support that file type!"))
                .contentType("application/json")
                .when()
                .post("https://api.imgur.com/3/upload")
                .prettyPeek()
                .then()
                .extract()
                .response()
                .jsonPath()
                .getString("data.deletehash");
    }

    @Test
    void uploadEmptyImageTest() {
        uploadedFileId = given()
                .header("Authorization", token)
                .multiPart("image", new File(PATH_TO_EMPTY_IMAGE))
                .formParam("title", "ImageTitle")
                .expect()
                .statusCode(400)
                .body("success", is(false))
                .body("data.error", equalTo("Bad request"))
                .contentType("application/json")
                .when()
                .post("https://api.imgur.com/3/upload")
                .prettyPeek()
                .then()
                .extract()
                .response()
                .jsonPath()
                .getString("data.deletehash");
    }

    @Test
    void uploadVideoTest() {
        uploadedFileId = given()
                .header("Authorization", token)
                .multiPart("video", new File(PATH_TO_VIDEO))
                .formParam("title", "VideoTitle")
                .expect()
                .statusCode(200)
                .body("success", is(true))
                .body("status", equalTo(200))
                .body("data.id", is(notNullValue()))
                .contentType("application/json")
                .when()
                .post("https://api.imgur.com/3/upload")
                .prettyPeek()
                .then()
                .extract()
                .response()
                .jsonPath()
                .getString("data.deletehash");
    }

    @Test
    void favoriteImageTest() {
        uploadedFileId = given()
                .header("Authorization", token)
                .multiPart("image", new File(PATH_TO_IMAGE))
                .formParam("title", "ImageTitle")
                .expect()
                .statusCode(200)
                .when()
                .post("https://api.imgur.com/3/upload")
                .then()
                .extract()
                .response()
                .jsonPath()
                .getString("data.deletehash");
        given()
                .header("Authorization", token)
                .expect()
                .statusCode(200)
                .body("success", is(true))
                .body("data", equalTo("favorited"))
                .contentType("application/json")
                .when()
                .post("https://api.imgur.com/3/image/{imageHash}/favorite", uploadedFileId)
                .prettyPeek();
    }

    @Test
    void unfavoriteImageTest() {
        favoriteImageTest();
        given()
                .header("Authorization", token)
                .expect()
                .statusCode(200)
                .body("success", is(true))
                .body("data", equalTo("unfavorited"))
                .contentType("application/json")
                .when()
                .post("https://api.imgur.com/3/image/{imageHash}/favorite", uploadedFileId)
                .prettyPeek();
    }

    @Test
    void favoriteUnexistingImageTest() {
        String randomId = RandomStringUtils.randomAlphanumeric(7);
        given()
                .header("Authorization", token)
                .expect()
                .statusCode(404)
                .when()
                .post("https://api.imgur.com/3/image/{randomId}/favorite", randomId)
                .prettyPeek();
    }

    @Test
    void updateAndGetImageTest() {
        String fileId = given()
                .header("Authorization", token)
                .multiPart("image", encodedFile)
                .formParam("title", "ImageTitle")
                .expect()
                .body("status", equalTo(200))
                .when()
                .post("https://api.imgur.com/3/image")
                .then()
                .extract()
                .response()
                .jsonPath()
                .getString("data.id");
        String title = "A new title";
        String description = "A new description";
        given()
                .header("Authorization", token)
                .formParam("title", title)
                .formParam("description", description)
                .expect()
                .statusCode(200)
                .body("success", is(true))
                .contentType("application/json")
                .when()
                .post("https://api.imgur.com/3/image/{imageDeleteHash1}", fileId);
        given()
                .header("Authorization", token)
                .expect()
                .statusCode(200)
                .body("success", is(true))
                .body("data.title", equalTo(title))
                .body("data.description", equalTo(description))
                .contentType("application/json")
                .when()
                .get("https://api.imgur.com/3/image/{imageHash}", fileId)
                .prettyPeek();
    }

    @Test
    void favoriteAfterDeleteImageTest() {
        uploadImageTest();
        given()
                .header("Authorization", token)
                .expect()
                .statusCode(200)
                .when()
                .delete("https://api.imgur.com/3/image/{imageDeleteHash}", uploadedFileId);
        given()
                .header("Authorization", token)
                .expect()
                .statusCode(404)
                .when()
                .post("https://api.imgur.com/3/image/{imageHash}/favorite", uploadedFileId)
                .prettyPeek();
    }

    @AfterEach
    void tearDown() {
        try {
            given()
                    .header("Authorization", token)
                    .when()
                    .delete("https://api.imgur.com/3/image/{imageDeleteHash}", uploadedFileId)
                    .prettyPeek()
                    .then()
                    .statusCode(200);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

    }

    private byte[] getFileContent() {
        byte[] byteArray = new byte[0];
        try {
            byteArray = FileUtils.readFileToByteArray(new File(PATH_TO_IMAGE));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return byteArray;
    }
}

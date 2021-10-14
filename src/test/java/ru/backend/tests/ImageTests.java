package ru.backend.tests;

import io.restassured.builder.MultiPartSpecBuilder;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.MultiPartSpecification;
import io.restassured.specification.RequestSpecification;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.backend.dto.PostImageResponce;

import java.io.File;
import java.io.IOException;
import java.util.Base64;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static ru.backend.Endpoints.*;

// не понятно как правильно делать сценарии - вызывать функции-тесты или писать заново
// не понятно как получить json при 404, а не html
// как сохранить одновременно и id и deletehash

public class ImageTests extends BaseTest {
    private final String PATH_TO_IMAGE = "src/test/resources/image1.jpg";
    private final String PATH_TO_NOT_AN_IMAGE = "src/test/resources/not an image.txt";
    private final String PATH_TO_EMPTY_IMAGE = "src/test/resources/empty.jpg";
    private final String PATH_TO_VIDEO = "src/test/resources/video.mp4";
    static String encodedFile;
    String fileId;
    String fileDeleteHash;
    MultiPartSpecification base64MultiPartSpec;
    MultiPartSpecification multiPartSpecWithImage;
    MultiPartSpecification multiPartSpecWithEmptyImage;
    MultiPartSpecification multiPartSpecWithNotImage;
    MultiPartSpecification multiPartSpecWithVideo;
    static RequestSpecification requestSpecificationWithAuthAndMultiPartImage;
    static RequestSpecification requestSpecificationWithAuthAndBase64;
    static RequestSpecification requestSpecificationWithAuthAndForm;
    static RequestSpecification requestSpecificationWithAuthAndmultiPartSpecWithNotImage;
    static RequestSpecification requestSpecificationWithAuthAndmultiPartSpecWithEmptyImage;
    static RequestSpecification requestSpecificationWithAuthAndmultiPartSpecWithVideo;
    PostImageResponce postImageResponce;

    @BeforeEach
    void beforeTest() {
        byte[] byteArray = getFileContent();
        encodedFile = Base64.getEncoder().encodeToString(byteArray);

        base64MultiPartSpec = new MultiPartSpecBuilder(encodedFile)
                .controlName("image")
                .build();

        multiPartSpecWithImage = new MultiPartSpecBuilder(new File(PATH_TO_IMAGE))
                .controlName("image")
                .build();

        multiPartSpecWithNotImage = new MultiPartSpecBuilder(new File(PATH_TO_NOT_AN_IMAGE))
                .controlName("image")
                .build();

        multiPartSpecWithEmptyImage = new MultiPartSpecBuilder(new File(PATH_TO_EMPTY_IMAGE))
                .controlName("image")
                .build();

        multiPartSpecWithVideo = new MultiPartSpecBuilder(new File(PATH_TO_IMAGE))
                .controlName("video")
                .build();

        requestSpecificationWithAuthAndForm = new RequestSpecBuilder()
                .addRequestSpecification(requestSpecificationWithAuth)
                .addFormParam("title", "File title")
                .addFormParam("description", "File description")
                .build();

        requestSpecificationWithAuthAndMultiPartImage = new RequestSpecBuilder()
                .addRequestSpecification(requestSpecificationWithAuthAndForm)
                .addMultiPart(multiPartSpecWithImage)
                .build();

        requestSpecificationWithAuthAndBase64 = new RequestSpecBuilder()
                .addRequestSpecification(requestSpecificationWithAuth)
                .addFormParam("title", "Base64 image title")
                .addFormParam("description", "Base64 image description")
                .addMultiPart(base64MultiPartSpec)
                .build();

        requestSpecificationWithAuthAndmultiPartSpecWithNotImage = new RequestSpecBuilder()
                .addRequestSpecification(requestSpecificationWithAuthAndForm)
                .addMultiPart(multiPartSpecWithNotImage)
                .build();

        requestSpecificationWithAuthAndmultiPartSpecWithEmptyImage = new RequestSpecBuilder()
                .addRequestSpecification(requestSpecificationWithAuthAndForm)
                .addMultiPart(multiPartSpecWithEmptyImage)
                .build();

        requestSpecificationWithAuthAndmultiPartSpecWithVideo = new RequestSpecBuilder()
                .addRequestSpecification(requestSpecificationWithAuthAndForm)
                .addMultiPart(multiPartSpecWithVideo)
                .build();
    }

    @Test
    void uploadImageWithDeserializeTest() {
        postImageResponce = given(requestSpecificationWithAuthAndMultiPartImage, positiveResponseSpecification)
                .post(UPLOAD_FILE)
                .prettyPeek()
                .then()
                .extract()
                .body()
                .as(PostImageResponce.class);
        fileDeleteHash = postImageResponce.getData().getDeletehash();
        fileId = postImageResponce.getData().getId();
    }

    @Test
    void deleteImageTest() {
        // как сделать чтобы после этого теста не выполнялся aftereach?
        uploadImageWithDeserializeTest();
        given(requestSpecificationWithAuth, positiveResponseSpecification)
                .delete(DELETE_FILE, fileDeleteHash)
                .prettyPeek();
    }

    @Test
    void deleteAfterDeleteImageTest() {
        deleteImageTest();
        given(requestSpecificationWithAuth, notFoundResponseSpecification)
                .delete(DELETE_FILE, fileDeleteHash)
                .prettyPeek();
    }

    @Test
    void uploadNothing() {
        // можно ли дать на вход given только responce specification
        postImageResponce = given()
                .expect()
                .statusCode(400)
                .body("success", is(false))
                .body("data.error", equalTo("Bad Request"))
                .contentType("application/json")
                .when()
                .post(UPLOAD_FILE)
                .prettyPeek()
                .then()
                .extract()
                .body()
                .as(PostImageResponce.class);
        fileDeleteHash = postImageResponce.getData().getDeletehash();
    }

    @Test
    void uploadNotAnImageTest() {
        postImageResponce = given(requestSpecificationWithAuthAndmultiPartSpecWithNotImage, badRequestResponseSpecification)
                .post(UPLOAD_FILE)
                .prettyPeek()
                .then()
                .extract()
                .body()
                .as(PostImageResponce.class);
        fileDeleteHash = postImageResponce.getData().getDeletehash();
    }

    @Test
    void uploadEmptyImageTest() {
        postImageResponce = given(requestSpecificationWithAuthAndmultiPartSpecWithEmptyImage, badRequestResponseSpecification)
                .post(UPLOAD_FILE)
                .prettyPeek()
                .then()
                .extract()
                .body()
                .as(PostImageResponce.class);
        fileDeleteHash = postImageResponce.getData().getDeletehash();
    }

    @Test
    void uploadVideoTest() {
        postImageResponce = given(requestSpecificationWithAuthAndmultiPartSpecWithVideo, positiveResponseSpecification)
                .post(UPLOAD_FILE)
                .prettyPeek()
                .then()
                .extract()
                .body()
                .as(PostImageResponce.class);
        fileDeleteHash = postImageResponce.getData().getDeletehash();
    }

    @Test
    void favoriteImageTest() {
        uploadImageWithDeserializeTest();
        // можно ли добавить .body, если указана responce specification?
        given(requestSpecificationWithAuth)
                .expect()
                .statusCode(200)
                .body("success", is(true))
                .body("data", equalTo("favorited"))
                .contentType("application/json")
                .when()
                .post(FAVORITE_FILE, fileId)
                .prettyPeek();
    }

    @Test
    void unfavoriteImageTest() {
        favoriteImageTest();
        given(requestSpecificationWithAuth)
                .expect()
                .statusCode(200)
                .body("success", is(true))
                .body("data", equalTo("unfavorited"))
                .contentType("application/json")
                .when()
                .post(FAVORITE_FILE, fileId)
                .prettyPeek();
    }

    @Test
    void favoriteNonexistentImageTest() {
        String randomId = RandomStringUtils.randomAlphanumeric(7);
        given(requestSpecificationWithAuth, notFoundResponseSpecification)
                .post(FAVORITE_FILE, randomId)
                .prettyPeek();
    }

    @Test
    void updateAndGetImageTest() {
        uploadImageWithDeserializeTest();
        String newTitle = "A new title";
        String newDescription = "A new description";
        given(requestSpecificationWithAuth)
                .formParam("title", newTitle)
                .formParam("description", newDescription)
                .expect()
                .statusCode(200)
                .body("success", is(true))
                .contentType("application/json")
                .when()
                .post(UPDATE_FILE, fileId);
        given(requestSpecificationWithAuth)
                .expect()
                .statusCode(200)
                .body("success", is(true))
                .body("data.title", equalTo(newTitle))
                .body("data.description", equalTo(newDescription))
                .contentType("application/json")
                .when()
                .get(GET_FILE, fileId)
                .prettyPeek();
    }

    @Test
    void updateNothingAndGetImageTest() {
        postImageResponce = given(requestSpecificationWithAuthAndMultiPartImage)
                .expect()
                .body("status", equalTo(200))
                .when()
                .post(UPLOAD_IMAGE)
                .then()
                .extract()
                .body()
                .as(PostImageResponce.class);
        fileDeleteHash = postImageResponce.getData().getDeletehash();
        fileId = postImageResponce.getData().getId();
        String title = postImageResponce.getData().getTitle();
        String description = postImageResponce.getData().getDescription();
        given(requestSpecificationWithAuth, positiveResponseSpecification)
                .post(UPDATE_FILE, fileId);
        given(requestSpecificationWithAuth)
                .expect()
                .statusCode(200)
                .body("success", is(true))
                .body("data.title", equalTo(title))
                .body("data.description", equalTo(description))
                .contentType("application/json")
                .when()
                .get(GET_FILE, fileId)
                .prettyPeek();
    }

    @Test
    void favoriteAfterDeleteImageTest() {
        deleteImageTest();
        given(requestSpecificationWithAuth, notFoundResponseSpecification)
                .post(FAVORITE_FILE, fileDeleteHash)
                .prettyPeek();
    }

    @Test
    void updateAfterDeleteImageTest() {
        deleteImageTest();
        given(requestSpecificationWithAuth, notFoundResponseSpecification)
                .post(UPDATE_FILE, fileId)
                .prettyPeek();
    }

    @Test
    void getAfterDeleteImageTest() {
        deleteImageTest();
        given(requestSpecificationWithAuth, notFoundResponseSpecification)
                .get(GET_FILE, fileId)
                .prettyPeek();
    }

    @AfterEach
    void tearDown() {
        try {
            given(requestSpecificationWithAuth)
                    .when()
                    .delete(DELETE_FILE, fileDeleteHash)
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

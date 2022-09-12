package so.hau.dmsapi.controller;

import io.restassured.RestAssured;
import io.restassured.http.Method;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import so.hau.dmsapi.DmsApiBoot;
import so.hau.dmsapi.domain.DocumentOperation;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = { DmsApiBoot.class, ApiController.class })
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ApiControllerTests {

    @LocalServerPort
    private int port;

    @BeforeEach
    public void beforeEach() {
        RestAssured.port = port;
    }

    private File createTestFile() throws Exception {
        File file = new File("testfile.txt");
        IOUtils.write("Some text", new FileOutputStream(file), StandardCharsets.UTF_8);
        return file;
    }

    @Test
    public void testImportOneDocument() throws Exception {
        DocumentOperation operation = given()
                .multiPart("file", createTestFile())
                .param("documentName", "test document")
                .expect()
                .statusCode(200)
                .when()
                .request(Method.POST, "/dms/import")
                .body()
                .as(DocumentOperation.class);

        assertEquals("pending", operation.getStatus());

        operation = waitUntilCompleted(operation);

        assertEquals("completed", operation.getStatus());
    }

    @Test
    public void testImportSixDocuments() throws Exception {
        DocumentOperation operation = null;
        for (int i = 1; i < 7; i++) {
            operation = given()
                    .multiPart("file", createTestFile())
                    .param("documentName", String.format("test document %d", i))
                    .expect()
                    .statusCode(200)
                    .when()
                    .request(Method.POST, "/dms/import")
                    .body()
                    .as(DocumentOperation.class);

            if (i < 6) {
                assertEquals("pending", operation.getStatus());
            } else {
                assertEquals("not_started", operation.getStatus());
            }
        }

        // Wait until the last operation is completed and give it 5 more seconds, just to be safe
        waitUntilCompleted(operation);
        Thread.sleep(5000);
    }

    @Test
    public void testImportNoDocument() {
        given()
                .param("documentName", "test document")
                .expect()
                .statusCode(500)
                .when()
                .request(Method.POST, "/dms/import");
    }

    @Test
    public void testImportNoDocumentName() throws Exception {
        given()
                .multiPart("file", createTestFile())
                .expect()
                .statusCode(400)
                .when()
                .request(Method.POST, "/dms/import");
    }

    @Test
    public void testNotValidOperation() {
        given()
                .expect()
                .statusCode(404)
                .when()
                .get("/dms/status/bad_id");
    }

    private DocumentOperation waitUntilCompleted(DocumentOperation operation) throws InterruptedException {
        while (!operation.getStatus().equals("completed")) {
            operation = given()
                    .expect()
                    .statusCode(200)
                    .when()
                    .get(String.format("/dms/status/%s", operation.getOperationId()))
                    .body()
                    .as(DocumentOperation.class);

            Thread.sleep(1000);
        }

        return operation;
    }
}

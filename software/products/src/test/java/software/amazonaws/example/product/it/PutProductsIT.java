// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package software.amazonaws.example.product.it;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

public class PutProductsIT {

    private static final String BASE_URL = System.getenv("API_URL");

    @Test
    @DisplayName("Test put")
    public void testPutProduct() {
        given()
            .pathParam("id", "ABCDEF")
            .body("""
                    {
                    "id": "ABCDEF",
                    "name": "pink shorts",
                    "price": 12.34
                    }
                    """)
            .contentType("application/json")
        .when()
            .put(BASE_URL + "/{id}")
        .then()
            .statusCode(201);
    }

    @Test
    @DisplayName("Test put - incorrect path param")
    public void testPutProductWithIncorrentPathParam() {
        given()
            .pathParam("id", "incorrect")
            .body("""
                    {
                    "id": "ABCDEF",
                    "name": "pink shorts",
                    "price": 12.34
                    }
                    """)
            .contentType("application/json")
        .when()
            .put(BASE_URL + "/{id}")
        .then()
            .statusCode(400);
    }

    @Test
    @DisplayName("Test put - no body")
    public void testPutProductWithNoBody() {
        given()
            .pathParam("id", "incorrect")
            .contentType("application/json")
        .when()
            .put(BASE_URL + "/{id}")
        .then()
            .statusCode(400)
            .body(equalTo("{\"message\": \"Empty request body\"}"));
    }

    @Test
    @DisplayName("Test put - empty body")
    public void testPutProductWithEmptyBody() {
        given()
                .pathParam("id", "incorrect")
                .body("")
                .contentType("application/json")
        .when()
                .put(BASE_URL + "/{id}")
        .then()
                .statusCode(400)
                .body(equalTo("{\"message\": \"Empty request body\"}"));
    }
}

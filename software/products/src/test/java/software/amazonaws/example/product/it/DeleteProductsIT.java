// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package software.amazonaws.example.product.it;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import software.amazonaws.example.product.model.Product;

import java.math.BigDecimal;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class DeleteProductsIT {

    private static final String BASE_URL = System.getenv("API_URL");

    @Test
    @DisplayName("Test delete")
    public void testDeleteProduct() {
        given()
                .pathParam("id", "ABCDEF")
                .body(new Product("ABCDEF", "pink shorts", new BigDecimal("12.34")))
                .contentType("application/json")
        .when()
                .put(BASE_URL + "/{id}")
        .then()
                .statusCode(201);

        given()
                .pathParam("id", "ABCDEF")
        .when()
                .delete(BASE_URL + "/{id}")
        .then()
                .statusCode(200);
    }

    @Test
    @DisplayName("Test delete - no path param")
    public void testDeleteProductNoPathParam() {
        given()
                .pathParam("incorrect", "ABCDEF")
        .when()
                .delete(BASE_URL + "/{incorrect}")
        .then()
                .statusCode(400)
                .body(equalTo("{ \"message\": \"Missing 'id' parameter in path\" }"));
    }
}

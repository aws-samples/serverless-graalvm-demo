// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package software.amazonaws.example.product.it;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import software.amazonaws.example.product.model.Product;

import java.math.BigDecimal;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;

public class GetAllProductsIT {

    private static final String BASE_URL = System.getenv("API_URL");

    @Test
    @DisplayName("Test GetAll")
    public void testGetAllProducts() {
        given()
                .pathParam("id", "ABCDEF")
                .body(new Product("ABCDEF", "pink shorts", new BigDecimal("12.34")))
                .contentType("application/json")
        .when()
                .put(BASE_URL + "/{id}")
        .then()
                .statusCode(201);

        when()
                .get(BASE_URL )
        .then()
                .statusCode(200)
                .body("products", hasSize(greaterThanOrEqualTo(1)));
    }
}

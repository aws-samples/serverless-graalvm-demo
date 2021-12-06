// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package software.amazonaws.example.product.entrypoints;

import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Put;
import jakarta.inject.Inject;
import software.amazonaws.example.product.model.Product;
import software.amazonaws.example.product.model.ProductSaved;
import software.amazonaws.example.product.store.ProductStore;
import software.amazonaws.example.product.store.dynamodb.DynamoDbProductStore;

import javax.validation.Valid;

@Controller
public class ProductController {

    private final ProductStore productStore = new DynamoDbProductStore();

    @Put
    public ProductSaved save(@Valid @Body Product product) {

        productStore.putProduct(product);

        ProductSaved productSaved = new ProductSaved();
        productSaved.setId(product.getId());
        productSaved.setName(product.getName());
        productSaved.setPrice(product.getPrice());

        return productSaved;
    }
}

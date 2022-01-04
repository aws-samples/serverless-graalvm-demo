// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package software.amazonaws.example.product.store;

import software.amazonaws.example.product.model.Product;
import software.amazonaws.example.product.model.Products;

import java.util.Optional;

public interface ProductStore {

    Optional<Product> getProduct(String id);

    void putProduct(Product product);

    void deleteProduct(String id);

    Optional<Products> getAllProduct();
}

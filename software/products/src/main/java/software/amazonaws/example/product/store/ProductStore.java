// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package software.amazonaws.example.product.store;

import software.amazonaws.example.product.model.Product;

import java.util.List;

public interface ProductStore {

    Product getProduct(String id);

    void putProduct(Product product);

    void deleteProduct(String id);

    List<Product> getAllProduct();
}

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package software.amazonaws.example.product.store.dynamodb;

import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazonaws.example.product.model.Product;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class ProductMapper {

    private static final String PK = "PK";
    private static final String NAME = "name";
    private static final String PRICE = "price";

    public static Product productFromDynamoDB(Map<String, AttributeValue> items) {
        Product product = new Product();
        product.setId(items.get(PK).s());
        product.setName(items.get(NAME).s());
        product.setPrice(new BigDecimal(items.get(PRICE).n()));

        return product;
    }

    public static Map<String, AttributeValue> productToDynamoDb(Product product) {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put(PK, AttributeValue.builder().s(product.getId()).build());
        item.put(NAME, AttributeValue.builder().s(product.getName()).build());
        item.put(PRICE, AttributeValue.builder().n(product.getPrice().toString()).build());

        return item;
    }
}

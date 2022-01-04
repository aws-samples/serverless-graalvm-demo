// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package software.amazonaws.example.product;

import software.amazonaws.example.product.store.dynamodb.DynamoDbProductStore;

public class Test {

    public static void main(String[] args) {
        DynamoDbProductStore dynamoDbProductStore = new DynamoDbProductStore();
        dynamoDbProductStore.getAllProduct();
    }
}

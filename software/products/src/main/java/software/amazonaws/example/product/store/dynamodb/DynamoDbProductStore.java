// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package software.amazonaws.example.product.store.dynamodb;

import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.core.SdkSystemSetting;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanResponse;
import software.amazonaws.example.product.model.Product;
import software.amazonaws.example.product.store.ProductStore;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DynamoDbProductStore implements ProductStore {

    private static final String PRODUCT_TABLE_NAME = System.getenv("PRODUCT_TABLE_NAME");

    private final DynamoDbClient dynamoDbClient = DynamoDbClient.builder()
            .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
            .region(Region.of(System.getenv(SdkSystemSetting.AWS_REGION.environmentVariable())))
            .build();

    @Override
    public Product getProduct(String id) {
        GetItemResponse getItemResponse = dynamoDbClient.getItem(GetItemRequest.builder()
                .key(Map.of("PK", AttributeValue.builder().s(id).build()))
                .tableName(PRODUCT_TABLE_NAME)
                .build());

        return ProductMapper.productFromDynamoDB(getItemResponse.item());
    }

    @Override
    public void putProduct(Product product) {
        dynamoDbClient.putItem(PutItemRequest.builder()
                .tableName(PRODUCT_TABLE_NAME)
                .item(ProductMapper.productToDynamoDb(product))
                .build());
    }

    @Override
    public void deleteProduct(String id) {
        dynamoDbClient.deleteItem(DeleteItemRequest.builder()
                .tableName(PRODUCT_TABLE_NAME)
                .key(Map.of("PK", AttributeValue.builder().s(id).build()))
                .build());
    }

    @Override
    public List<Product> getAllProduct() {
        ScanResponse scanResponse = dynamoDbClient.scan(ScanRequest.builder()
                .tableName(PRODUCT_TABLE_NAME)
                .limit(20)
                .build());

        return scanResponse.items().stream()
                .map(ProductMapper::productFromDynamoDB)
                .collect(Collectors.toList());
    }
}

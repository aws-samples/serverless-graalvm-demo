// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package software.amazonaws.example.product.store.dynamodb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.core.SdkSystemSetting;
import software.amazon.awssdk.http.crt.AwsCrtAsyncHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanResponse;
import software.amazonaws.example.product.model.Product;
import software.amazonaws.example.product.model.Products;
import software.amazonaws.example.product.store.ProductStore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class DynamoDbProductStore implements ProductStore {

    private static final Logger logger = LoggerFactory.getLogger(DynamoDbProductStore.class);
    private static final String PRODUCT_TABLE_NAME = System.getenv("PRODUCT_TABLE_NAME");

    private final DynamoDbAsyncClient dynamoDbClient = DynamoDbAsyncClient.builder()
            .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
            .region(Region.of(System.getenv(SdkSystemSetting.AWS_REGION.environmentVariable())))
            .httpClient(AwsCrtAsyncHttpClient.builder()
                    .maxConcurrency(50)
                    .build())
            .build();

    @Override
    public Optional<Product> getProduct(String id) {
        CompletableFuture<GetItemResponse> future = dynamoDbClient.getItem(GetItemRequest.builder()
                .key(Map.of("PK", AttributeValue.builder().s(id).build()))
                .tableName(PRODUCT_TABLE_NAME)
                .build());
        GetItemResponse getItemResponse = null;
        try {
            getItemResponse = future.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        if (getItemResponse.hasItem()) {
            return Optional.of(ProductMapper.productFromDynamoDB(getItemResponse.item()));
        } else {
            return Optional.empty();
        }

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
    public Products getAllProduct() {
        CompletableFuture<ScanResponse> future = dynamoDbClient.scan(ScanRequest.builder()
                .tableName(PRODUCT_TABLE_NAME)
                .limit(20)
                .build());

        ScanResponse scanResponse = null;
        try {
            scanResponse = future.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        logger.info("Scan returned: {} item(s)", scanResponse.count());

        List<Product> productList = new ArrayList<>();

        for (Map<String, AttributeValue> item : scanResponse.items()) {
            productList.add(ProductMapper.productFromDynamoDB(item));
        }

        return new Products(productList);
    }
}

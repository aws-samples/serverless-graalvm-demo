// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package software.amazonaws.example.product.store.dynamodb;

import com.amazonaws.xray.interceptors.TracingInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.core.SdkSystemSetting;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

public class DynamoDbProductStore implements ProductStore {

    private static final Logger logger = LoggerFactory.getLogger(DynamoDbProductStore.class);
    private static final String PRODUCT_TABLE_NAME = System.getenv("PRODUCT_TABLE_NAME");

    private final DynamoDbAsyncClient dynamoDbClient = DynamoDbAsyncClient.builder()
            .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
            .region(Region.of(System.getenv(SdkSystemSetting.AWS_REGION.environmentVariable())))
            .overrideConfiguration(ClientOverrideConfiguration.builder()
                    .addExecutionInterceptor(new TracingInterceptor())
                    .build())
            .httpClientBuilder(AwsCrtAsyncHttpClient.builder())
            .build();

    @Override
    public Optional<Product> getProduct(String id) {
        try {
            GetItemResponse getItemResponse = dynamoDbClient.getItem(GetItemRequest.builder()
                            .key(Map.of("PK", AttributeValue.builder().s(id).build()))
                            .tableName(PRODUCT_TABLE_NAME)
                            .build())
                    .get();
            if (getItemResponse.hasItem()) {
                return Optional.of(ProductMapper.productFromDynamoDB(getItemResponse.item()));
            } else {
                return Optional.empty();
            }
        } catch (InterruptedException | ExecutionException e) {
            logger.error("getItem failed with message {}", e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public void putProduct(Product product) {
        try {
            dynamoDbClient.putItem(PutItemRequest.builder()
                    .tableName(PRODUCT_TABLE_NAME)
                    .item(ProductMapper.productToDynamoDb(product))
                    .build()).get();
        } catch (InterruptedException | ExecutionException e) {
            logger.error("putItem failed with message {}", e.getMessage());
        }
    }

    @Override
    public void deleteProduct(String id) {
        try {
            dynamoDbClient.deleteItem(DeleteItemRequest.builder()
                    .tableName(PRODUCT_TABLE_NAME)
                    .key(Map.of("PK", AttributeValue.builder().s(id).build()))
                    .build()).get();
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Deleting item with Id {} failed with message {}", id, e.getMessage());
        }
    }

    @Override
    public Products getAllProduct() {
        try {
            ScanResponse scanResponse = dynamoDbClient.scan(ScanRequest.builder()
                            .tableName(PRODUCT_TABLE_NAME)
                            .limit(20)
                            .build())
                    .get();

            logger.info("Scan returned: {} item(s)", scanResponse.count());

            List<Product> productList = new ArrayList<>();

            for (Map<String, AttributeValue> item : scanResponse.items()) {
                productList.add(ProductMapper.productFromDynamoDB(item));
            }

            return new Products(productList);
        } catch (InterruptedException | ExecutionException e) {
            logger.error("scan failed with message {}", e.getMessage());
            return new Products(Collections.emptyList());
        }
    }
}

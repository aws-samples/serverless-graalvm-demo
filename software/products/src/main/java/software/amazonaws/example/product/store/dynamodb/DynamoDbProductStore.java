// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package software.amazonaws.example.product.store.dynamodb;

import com.amazonaws.xray.interceptors.TracingInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.core.SdkSystemSetting;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
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
import software.amazonaws.example.product.model.Products;
import software.amazonaws.example.product.store.ProductStore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DynamoDbProductStore implements ProductStore {

    private static final Logger logger = LoggerFactory.getLogger(DynamoDbProductStore.class);

    private final String tableName;
    private final DynamoDbClient dynamoDbClient;

    public DynamoDbProductStore() {
        this(DynamoDbClient.builder()
                .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
                .region(Region.of(System.getenv(SdkSystemSetting.AWS_REGION.environmentVariable())))
                .overrideConfiguration(ClientOverrideConfiguration.builder()
                        .addExecutionInterceptor(new TracingInterceptor())
                        .build())
                .build(), System.getenv("PRODUCT_TABLE_NAME")
        );
    }

    public DynamoDbProductStore(DynamoDbClient dynamoDbClient, String tableName) {
        this.dynamoDbClient = dynamoDbClient;
        this.tableName = tableName;
    }

    @Override
    public Optional<Product> getProduct(String id) {
        GetItemResponse getItemResponse = dynamoDbClient.getItem(GetItemRequest.builder()
                .key(Map.of("PK", AttributeValue.builder().s(id).build()))
                .tableName(tableName)
                .build());

        if (getItemResponse.hasItem()) {
            return Optional.of(ProductMapper.productFromDynamoDB(getItemResponse.item()));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public void putProduct(Product product) {
        dynamoDbClient.putItem(PutItemRequest.builder()
                .tableName(tableName)
                .item(ProductMapper.productToDynamoDb(product))
                .build());
    }

    @Override
    public void deleteProduct(String id) {
        dynamoDbClient.deleteItem(DeleteItemRequest.builder()
                .tableName(tableName)
                .key(Map.of("PK", AttributeValue.builder().s(id).build()))
                .build());
    }

    @Override
    public Products getAllProduct() {
        ScanResponse scanResponse = dynamoDbClient.scan(ScanRequest.builder()
                .tableName(tableName)
                .limit(20)
                .build());

        logger.info("Scan returned: {} item(s)", scanResponse.count());

        List<Product> productList = new ArrayList<>();

        for (Map<String, AttributeValue> item : scanResponse.items()) {
            productList.add(ProductMapper.productFromDynamoDB(item));
        }

        return new Products(productList);
    }
}

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package software.amazonaws.example.product.entrypoints;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.BillingMode;
import software.amazon.awssdk.services.dynamodb.model.CreateTableRequest;
import software.amazon.awssdk.services.dynamodb.model.CreateTableResponse;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType;
import software.amazonaws.example.product.store.ProductStore;
import software.amazonaws.example.product.store.dynamodb.DynamoDbProductStore;

import java.net.URI;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

@Testcontainers
@TestInstance(PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ApiGatewayPutProductRequestHandlerTest {

    @Container
    static final GenericContainer dynamoDb = new GenericContainer("amazon/dynamodb-local")
            .withCommand("-jar DynamoDBLocal.jar -inMemory -sharedDb")
            .withExposedPorts(8000);
    public static final String TABLE_NAME = "Products";
    private ApiGatewayPutProductRequestHandler handler;
    private static ProductStore dynamoDbProductStore;
    private static DynamoDbClient client;

    @BeforeAll
    public static void init() {
        var endpointUrl = String.format("http://localhost:%d", dynamoDb.getFirstMappedPort());
        client = DynamoDbClient.builder()
                .endpointOverride(URI.create(endpointUrl))
                .build();
        dynamoDbProductStore = new DynamoDbProductStore(client, TABLE_NAME);
    }

    @Test
    @Order(1)
    void createTable() {
        CreateTableResponse response = client.createTable(CreateTableRequest.builder()
                .tableName(TABLE_NAME)
                .attributeDefinitions(AttributeDefinition.builder()
                        .attributeName("PK")
                        .attributeType(ScalarAttributeType.S)
                        .build())
                .keySchema(KeySchemaElement.builder()
                        .attributeName("PK")
                        .keyType(KeyType.HASH)
                        .build())
                .billingMode(BillingMode.PAY_PER_REQUEST)
                .build());
        assertTrue(response.sdkHttpResponse().isSuccessful());
    }

    @Test
    @Order(2)
    public void testValidRequest() {
        handler = new ApiGatewayPutProductRequestHandler(dynamoDbProductStore);

        APIGatewayV2HTTPEvent event = APIGatewayV2HTTPEvent.builder()
                .withBody("""
                        {
                            "id": "333",
                            "name": "test",
                            "price": 44.55
                        }""")
                .withPathParameters(Map.of("id", "333"))
                .build();
        APIGatewayV2HTTPResponse response = handler.handleRequest(event, new TestContext());

        Assertions.assertEquals(201, response.getStatusCode());
    }

    @Test
    public void testRequestBodyNull() {
        handler = new ApiGatewayPutProductRequestHandler(dynamoDbProductStore);

        APIGatewayV2HTTPEvent event = APIGatewayV2HTTPEvent.builder()
                .withPathParameters(Map.of("id", "333"))
                .build();
        APIGatewayV2HTTPResponse response = handler.handleRequest(event, new TestContext());

        Assertions.assertEquals(400, response.getStatusCode());
    }

    @Test
    public void testRequestBodyEmpty() {
        handler = new ApiGatewayPutProductRequestHandler(dynamoDbProductStore);

        APIGatewayV2HTTPEvent event = APIGatewayV2HTTPEvent.builder()
                .withBody("")
                .withPathParameters(Map.of("id", "333"))
                .build();
        APIGatewayV2HTTPResponse response = handler.handleRequest(event, new TestContext());

        Assertions.assertEquals(400, response.getStatusCode());
    }
}
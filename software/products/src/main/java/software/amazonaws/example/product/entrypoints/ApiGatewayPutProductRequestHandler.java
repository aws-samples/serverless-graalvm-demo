// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package software.amazonaws.example.product.entrypoints;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazonaws.example.product.model.Product;
import software.amazonaws.example.product.store.ProductStore;
import software.amazonaws.example.product.store.dynamodb.DynamoDbProductStore;

import java.util.Map;

import static software.amazon.awssdk.http.Header.CONTENT_TYPE;

public class ApiGatewayPutProductRequestHandler implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

    private static final Logger logger = LoggerFactory.getLogger(ApiGatewayPutProductRequestHandler.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private ProductStore productStore;

    public ApiGatewayPutProductRequestHandler() {
        this(new DynamoDbProductStore());
    }

    public ApiGatewayPutProductRequestHandler(ProductStore productStore) {
        this.productStore = productStore;
    }

    @Override
    public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent event, Context context) {
        logger.info("Event body: " + event.getBody());

        Product product = null;
        try {
            product = objectMapper.readValue(event.getBody(), Product.class);
        } catch (JsonProcessingException e) {
            logger.error(e.getMessage());
            return APIGatewayV2HTTPResponse.builder()
                    .withStatusCode(500)
                    .build();
        }

        logger.info("Parsed: " + product);

        try {
            productStore.putProduct(product);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return APIGatewayV2HTTPResponse.builder()
                    .withStatusCode(500)
                    .build();
        }

        return APIGatewayV2HTTPResponse.builder()
                .withStatusCode(201)
                .withHeaders(Map.of(CONTENT_TYPE, "application/json"))
                .withBody("{\"message\": \"Product created\"}")
                .build();
    }
}

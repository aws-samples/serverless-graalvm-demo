// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package software.amazonaws.example.product.entrypoints;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazonaws.example.product.model.Products;
import software.amazonaws.example.product.store.ProductStore;
import software.amazonaws.example.product.store.dynamodb.DynamoDbProductStore;

import software.amazon.lambda.powertools.tracing.Tracing;
import software.amazon.lambda.powertools.logging.Logging;
import software.amazon.lambda.powertools.metrics.Metrics;

import java.util.Map;

import static software.amazon.awssdk.http.Header.CONTENT_TYPE;

public class ApiGatewayGetAllProductRequestHandler implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

    private final static Logger logger = LogManager.getLogger(ApiGatewayGetAllProductRequestHandler.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ProductStore productStore;

    public ApiGatewayGetAllProductRequestHandler() {
        this(new DynamoDbProductStore());
    }

    public ApiGatewayGetAllProductRequestHandler(ProductStore productStore) {
        this.productStore = productStore;
    }

    @Logging
    @Tracing
    @Metrics(captureColdStart = true)
    @Override
    public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent event, Context context) {
        Products products;
        try {
            products = productStore.getAllProduct();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return APIGatewayV2HTTPResponse.builder()
                    .withStatusCode(500)
                    .withHeaders(Map.of(CONTENT_TYPE, "application/json"))
                    .withBody("{\"message\": \"Failed to get products\"}")
                    .build();
        }

        try {
            return APIGatewayV2HTTPResponse.builder()
                    .withStatusCode(200)
                    .withHeaders(Map.of(CONTENT_TYPE, "application/json"))
                    .withBody(objectMapper.writeValueAsString(products))
                    .build();
        } catch (JsonProcessingException e) {
            logger.error(e.getMessage(), e);
            return APIGatewayV2HTTPResponse.builder()
                    .withStatusCode(500)
                    .build();
        }
    }
}

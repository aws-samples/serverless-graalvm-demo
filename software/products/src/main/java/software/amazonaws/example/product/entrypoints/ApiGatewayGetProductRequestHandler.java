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
import software.amazonaws.example.product.store.dynamodb.DynamoDbProductStore;
import software.amazonaws.example.product.model.Product;
import software.amazonaws.example.product.store.ProductStore;

import software.amazon.lambda.powertools.tracing.Tracing;
import software.amazon.lambda.powertools.logging.Logging;
import software.amazon.lambda.powertools.metrics.Metrics;

import java.util.Map;
import java.util.Optional;

import static software.amazon.awssdk.http.Header.CONTENT_TYPE;

public class ApiGatewayGetProductRequestHandler implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

    private final static Logger logger = LogManager.getLogger(ApiGatewayGetProductRequestHandler.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ProductStore productStore;

    public ApiGatewayGetProductRequestHandler() {
        this(new DynamoDbProductStore());
    }

    public ApiGatewayGetProductRequestHandler(ProductStore productStore) {
        this.productStore = productStore;
    }

    @Logging
    @Tracing
    @Metrics(captureColdStart = true)
    @Override
    public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent event, Context context) {
        String id = event.getPathParameters().get("id");
        if (id == null) {
            logger.warn("Missing 'id' parameter in path");
            return APIGatewayV2HTTPResponse.builder()
                    .withStatusCode(400)
                    .withHeaders(Map.of(CONTENT_TYPE, "application/json"))
                    .withBody("{ \"message\": \"Missing 'id' parameter in path\" }")
                    .build();
        }

        logger.info("Fetching product {}", id);

        Optional<Product> product = productStore.getProduct(id);
        if (product.isEmpty()) {
            logger.warn("No product with id: {}", id);
            return APIGatewayV2HTTPResponse.builder()
                    .withStatusCode(404)
                    .withBody("{\"message\": \"Product not found\"}")
                    .build();
        }

        logger.info(product.toString());

        try {
            return APIGatewayV2HTTPResponse.builder()
                    .withStatusCode(200)
                    .withHeaders(Map.of(CONTENT_TYPE, "application/json"))
                    .withBody(objectMapper.writeValueAsString(product.get()))
                    .build();
        } catch (JsonProcessingException e) {
            return APIGatewayV2HTTPResponse.builder()
                    .withStatusCode(500)
                    .build();
        }
    }
}

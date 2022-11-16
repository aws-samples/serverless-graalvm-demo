// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package software.amazonaws.example.product.entrypoints;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazonaws.example.product.model.Product;
import software.amazonaws.example.product.store.ProductStore;
import software.amazonaws.example.product.store.dynamodb.DynamoDbProductStore;

import software.amazon.lambda.powertools.tracing.Tracing;
import software.amazon.lambda.powertools.logging.Logging;
import software.amazon.lambda.powertools.metrics.Metrics;

import java.io.IOException;
import java.util.Map;

import static software.amazon.awssdk.http.Header.CONTENT_TYPE;

public class ApiGatewayPutProductRequestHandler implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

    private final static Logger logger = LogManager.getLogger(ApiGatewayPutProductRequestHandler.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ProductStore productStore;

    public ApiGatewayPutProductRequestHandler() {
        this(new DynamoDbProductStore());
    }

    public ApiGatewayPutProductRequestHandler(ProductStore productStore) {
        this.productStore = productStore;
    }

    @Logging(logEvent = true)
    @Tracing
    @Metrics(captureColdStart = true)
    @Override
    public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent event, Context context) {
        logger.info("Event body: " + event.getBody());

        String id = event.getPathParameters().get("id");
        if (id == null) {
            logger.warn("Missing 'id' parameter in path");
            return APIGatewayV2HTTPResponse.builder()
                    .withStatusCode(400)
                    .withHeaders(Map.of(CONTENT_TYPE, "application/json"))
                    .withBody("{ \"message\": \"Missing 'id' parameter in path\" }")
                    .build();
        }

        if (event.getBody() == null || event.getBody().isEmpty()) {
            return APIGatewayV2HTTPResponse.builder()
                    .withStatusCode(400)
                    .withBody("{\"message\": \"Empty request body\"}")
                    .build();
        }

        Product product;
        try {
            product = objectMapper.readValue(event.getBody(), Product.class);
        } catch (IOException e) {
            logger.error(e.getMessage());
            return APIGatewayV2HTTPResponse.builder()
                    .withBody("{\"message\": \"Failed to parse product from request body\"}")
                    .withStatusCode(400)
                    .build();
        }

        if (!id.equals(product.getId())) {
            logger.error("Product ID in path ({}) does not match product ID in body ({})", id, product.getId());
            return APIGatewayV2HTTPResponse.builder()
                    .withStatusCode(400)
                    .withBody("{\"message\": \"Product ID in path does not match product ID in body\"}")
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

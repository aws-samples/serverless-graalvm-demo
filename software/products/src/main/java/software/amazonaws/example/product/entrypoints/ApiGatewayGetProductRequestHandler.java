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
import software.amazonaws.example.product.store.dynamodb.DynamoDbProductStore;
import software.amazonaws.example.product.model.Product;
import software.amazonaws.example.product.store.ProductStore;

import java.util.Map;

import static software.amazon.awssdk.http.Header.CONTENT_TYPE;

public class ApiGatewayGetProductRequestHandler implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

    private static final Logger logger = LoggerFactory.getLogger(ApiGatewayDeleteProductRequestHandler.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ProductStore productStore = new DynamoDbProductStore();

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

        Product product = productStore.getProduct(id);

        logger.info(product.toString());

        try {
            return APIGatewayV2HTTPResponse.builder()
                    .withStatusCode(200)
                    .withHeaders(Map.of(CONTENT_TYPE, "application/json"))
                    .withBody(objectMapper.writeValueAsString(product))
                    .build();
        } catch (JsonProcessingException e) {
            return APIGatewayV2HTTPResponse.builder()
                    .withStatusCode(500)
                    .build();
        }
    }
}

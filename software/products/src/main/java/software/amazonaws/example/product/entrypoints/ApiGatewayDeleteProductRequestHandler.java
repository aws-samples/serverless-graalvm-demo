// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package software.amazonaws.example.product.entrypoints;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazonaws.example.product.store.ProductStore;
import software.amazonaws.example.product.store.dynamodb.DynamoDbProductStore;

import java.util.Map;

import static software.amazon.awssdk.http.Header.CONTENT_TYPE;

public class ApiGatewayDeleteProductRequestHandler implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

    private static final Logger logger = LoggerFactory.getLogger(ApiGatewayDeleteProductRequestHandler.class);
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

        try {
            productStore.deleteProduct(id);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return APIGatewayV2HTTPResponse.builder()
                    .withStatusCode(500)
                    .withHeaders(Map.of(CONTENT_TYPE, "application/json"))
                    .withBody("{\"message\": \"Failed to delete product\"}")
                    .build();
        }

        return APIGatewayV2HTTPResponse.builder()
                .withStatusCode(200)
                .withHeaders(Map.of(CONTENT_TYPE, "application/json"))
                .withBody("{\"message\": \"Product deleted\"}")
                .build();
    }
}

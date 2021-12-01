// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package software.amazonaws.example.product.entrypoints;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import software.amazonaws.example.product.model.Product;
import software.amazonaws.example.product.store.ProductStore;

import java.util.Map;

import static org.mockito.Mockito.*;

public class ApiGatewayPutProductRequestHandlerTest {

    private ApiGatewayPutProductRequestHandler handler;

    private ProductStore mockProductStore = mock(ProductStore.class);

    @Test
    public void testValidRequest() {
        doNothing().when(mockProductStore).putProduct(any(Product.class));

        handler = new ApiGatewayPutProductRequestHandler(mockProductStore);

        APIGatewayV2HTTPEvent event = APIGatewayV2HTTPEvent.builder()
                .withBody("""
                {
                    \"id\": \"333\",
                    \"name\": \"test\",
                    \"price\": 44.55
                }""")
                .withPathParameters(Map.of("id", "333"))
                .build();
        APIGatewayV2HTTPResponse response = handler.handleRequest(event, new TestContext());

        Assertions.assertEquals(201, response.getStatusCode());
        verify(mockProductStore, timeout(1)).putProduct(any(Product.class));
    }

    @Test
    public void testRequestBodyNull() {
        handler = new ApiGatewayPutProductRequestHandler(mockProductStore);

        APIGatewayV2HTTPEvent event = APIGatewayV2HTTPEvent.builder()
                .withPathParameters(Map.of("id", "333"))
                .build();
        APIGatewayV2HTTPResponse response = handler.handleRequest(event, new TestContext());

        Assertions.assertEquals(400, response.getStatusCode());
    }

    @Test
    public void testRequestBodyEmpty() {
        handler = new ApiGatewayPutProductRequestHandler(mockProductStore);

        APIGatewayV2HTTPEvent event = APIGatewayV2HTTPEvent.builder()
                .withBody("")
                .withPathParameters(Map.of("id", "333"))
                .build();
        APIGatewayV2HTTPResponse response = handler.handleRequest(event, new TestContext());

        Assertions.assertEquals(400, response.getStatusCode());
    }
}
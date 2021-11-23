// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package software.amazonaws.example.product.entrypoints;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import software.amazonaws.example.product.model.Product;
import software.amazonaws.example.product.store.ProductStore;

import static org.mockito.Mockito.*;

public class ApiGatewayPutProductRequestHandlerTest {

    private ApiGatewayPutProductRequestHandler handler;

    private ProductStore mockProductStore = mock(ProductStore.class);

    @Test
    public void test() {
        doNothing().when(mockProductStore).putProduct(any(Product.class));

        handler = new ApiGatewayPutProductRequestHandler(mockProductStore);

        APIGatewayV2HTTPEvent event = APIGatewayV2HTTPEvent.builder()
                .withBody("{\n" +
                "    \"id\": \"333\",\n" +
                "    \"name\": \"test\",\n" +
                "    \"price\": \"4455\"\n" +
                "}")
                .build();
        APIGatewayV2HTTPResponse response = handler.handleRequest(event, new TestContext());

        Assertions.assertEquals(201, response.getStatusCode());
        verify(mockProductStore, timeout(1)).putProduct(any(Product.class));
    }
}
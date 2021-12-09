package software.amazonaws.example.product.entrypoints;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import software.amazonaws.example.product.model.Product;
import software.amazonaws.example.product.store.ProductStore;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ApiGatewayGetProductRequestHandlerTest {

    private ApiGatewayGetProductRequestHandler handler;

    @Test
    public void testGetForExistingProduct() throws JSONException {
        ProductStore mockProductStore = mock(ProductStore.class);
        Product product = new Product("Indigo Hats", "3d22f23b-1e74-4291-a6e9-4ab53c15cd77", new BigDecimal("13.3434343"));

        when(mockProductStore.getProduct("3d22f23b-1e74-4291-a6e9-4ab53c15cd77")).thenReturn(Optional.of(product));

        handler = new ApiGatewayGetProductRequestHandler(mockProductStore);

        APIGatewayV2HTTPEvent event = APIGatewayV2HTTPEvent.builder()
                .withPathParameters(Map.of("id", "3d22f23b-1e74-4291-a6e9-4ab53c15cd77"))
                .build();
        APIGatewayV2HTTPResponse response = handler.handleRequest(event, new TestContext());

        assertEquals(200, response.getStatusCode());
        JSONAssert.assertEquals("""
                {
                "id":"Indigo Hats",
                "name":"3d22f23b-1e74-4291-a6e9-4ab53c15cd77",
                "price":13.34
                }
                """, response.getBody(), JSONCompareMode.STRICT);
        verify(mockProductStore, timeout(1)).getProduct("3d22f23b-1e74-4291-a6e9-4ab53c15cd77");
    }

    @Test
    public void testGetForNonExistingProduct() throws JSONException {
        ProductStore mockProductStore = mock(ProductStore.class);
        when(mockProductStore.getProduct("3d22f23b-1e74-4291-a6e9-4ab53c15cd77")).thenReturn(Optional.empty());

        handler = new ApiGatewayGetProductRequestHandler(mockProductStore);

        APIGatewayV2HTTPEvent event = APIGatewayV2HTTPEvent.builder()
                .withPathParameters(Map.of("id", "3d22f23b-1e74-4291-a6e9-4ab53c15cd77"))
                .build();
        APIGatewayV2HTTPResponse response = handler.handleRequest(event, new TestContext());

        assertEquals(404, response.getStatusCode());
        JSONAssert.assertEquals("""
                {
                "message": "Product not found"
                }
                """, response.getBody(), JSONCompareMode.STRICT);
        verify(mockProductStore, timeout(1)).getProduct("3d22f23b-1e74-4291-a6e9-4ab53c15cd77");
    }
}
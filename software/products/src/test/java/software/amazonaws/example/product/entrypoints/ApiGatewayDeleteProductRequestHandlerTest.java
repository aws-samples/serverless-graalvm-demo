package software.amazonaws.example.product.entrypoints;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import software.amazonaws.example.product.store.ProductStore;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

public class ApiGatewayDeleteProductRequestHandlerTest {

    private ApiGatewayDeleteProductRequestHandler handler;

    private ProductStore mockProductStore = mock(ProductStore.class);

    @Test
    public void testDelete() throws JSONException {
        handler = new ApiGatewayDeleteProductRequestHandler(mockProductStore);

        APIGatewayV2HTTPEvent event = APIGatewayV2HTTPEvent.builder()
                .withPathParameters(Map.of("id", "12345"))
                .build();
        APIGatewayV2HTTPResponse response = handler.handleRequest(event, new TestContext());

        assertEquals(200, response.getStatusCode());
        JSONAssert.assertEquals("""
                {"message": "Product deleted"}
                """, response.getBody(), JSONCompareMode.STRICT);
        verify(mockProductStore, timeout(1)).deleteProduct("12345");
    }

    @Test
    public void testDeleteWhenWrongPathParam() throws JSONException {
        handler = new ApiGatewayDeleteProductRequestHandler(mockProductStore);

        APIGatewayV2HTTPEvent event = APIGatewayV2HTTPEvent.builder()
                .withPathParameters(Map.of("wrong", "12345"))
                .build();
        APIGatewayV2HTTPResponse response = handler.handleRequest(event, new TestContext());

        assertEquals(400, response.getStatusCode());
        JSONAssert.assertEquals("""
                { "message": "Missing 'id' parameter in path" }
                """, response.getBody(), JSONCompareMode.STRICT);
    }
}
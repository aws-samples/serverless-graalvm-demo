package software.amazonaws.example.product.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProductTest {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("2 decimal places")
    public void testPriceFormat() {
        Product product = new Product();
        product.setPrice(new BigDecimal("12.3456"));

        assertEquals(new BigDecimal("12.35"), product.getPrice());
    }

    @Test
    @DisplayName("Test serialisation")
    public void testSerialisation1() throws JSONException, JsonProcessingException {
        Product product = new Product("qwertyuiop", "pink shorts", new BigDecimal("12.3456"));

        JSONAssert.assertEquals("""
                {"id":"qwertyuiop","name":"pink shorts","price":12.35}""", objectMapper.writeValueAsString(product), JSONCompareMode.STRICT);
    }

    @Test
    @DisplayName("Test serialisation")
    public void testSerialisation2() throws JsonProcessingException, JSONException {
        Product product = new Product();
        product.setId("qwertyuiop");
        product.setName("pink shorts");
        product.setPrice(new BigDecimal("12.3456"));

        JSONAssert.assertEquals("""
                {"id":"qwertyuiop","name":"pink shorts","price":12.35}""", objectMapper.writeValueAsString(product), JSONCompareMode.STRICT);
    }
}
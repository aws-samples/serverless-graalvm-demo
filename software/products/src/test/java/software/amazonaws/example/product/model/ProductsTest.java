package software.amazonaws.example.product.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

class ProductsTest {

    @Test
    public void test() {
        Products products = new Products();
        products.setProducts(List.of(new Product("23123", "Pink Shorts", new BigDecimal("12.3456"))));

        Assertions.assertEquals(1, products.getProducts().size());
    }
}
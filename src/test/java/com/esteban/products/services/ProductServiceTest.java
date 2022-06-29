package com.esteban.products.services;

import com.esteban.products.entities.Product;
import com.esteban.products.model.ProductDto;
import com.esteban.products.repositories.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ProductServiceTest {

    private static String name = "Milk";
    private static int stock = 10;
    private static double price = 1.5;

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    public void setUp() {
        productRepository.deleteAll();
    }

    @Test
    public void testCreate() {
        ProductDto productDto = createProduct();

        Long productId = productService.create(productDto).getId();

        Optional<Product> optProduct = productRepository.findById(productId);
        assertTrue(optProduct.isPresent(), "Product created must exist");
        assertEquals(optProduct.get().getId(), productId, "Id returned must be the same as passed");
        assertEquals(optProduct.get().getName(), name, "Name must be the same as passed");
        assertEquals(optProduct.get().getStock(), stock, "Stock must be the same as passed");
        assertEquals(optProduct.get().getPrice(), price, "Price must be the same as passed");
    }

    @Test
    public void testDelete() {
        ProductDto productDto = createProduct();
        Long productId = productService.create(productDto).getId();

        productService.delete(productId);

        Optional<Product> optProduct = productRepository.findById(productId);
        assertFalse(optProduct.isPresent(), "Product deleted must not exist");
    }

    @Test
    public void testUpdate() {
        ProductDto productDto = createAnotherProduct();
        Long productId = productService.create(productDto).getId();

        productService.update(productId, createProduct());

        assertEquals(1, productRepository.count(), "Only one product must exist");
        Optional<Product> optProduct = productRepository.findById(productId);
        assertTrue(optProduct.isPresent(), "Product created must exist");
        assertEquals(productId, optProduct.get().getId(), "Id returned must be the same as passed in update");
        assertEquals(name, optProduct.get().getName(), "Name must be the same as passed in update");
        assertEquals(stock, optProduct.get().getStock(), "Stock must be the same as passed in update");
        assertEquals(price, optProduct.get().getPrice(), "Price must be the same as passed in update");
    }


    private ProductDto createProduct() {
        return ProductDto.builder()
                .name(name)
                .stock(stock)
                .price(price)
                .build();
    }

    private ProductDto createAnotherProduct() {
        return ProductDto.builder()
                .name("Bread")
                .stock(30)
                .price(0.75)
                .build();
    }

}

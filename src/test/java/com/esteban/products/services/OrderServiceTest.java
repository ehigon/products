package com.esteban.products.services;

import com.esteban.products.entities.Order;
import com.esteban.products.entities.Product;
import com.esteban.products.error.MissingStockException;
import com.esteban.products.model.ItemDto;
import com.esteban.products.model.OrderDto;
import com.esteban.products.repositories.ItemRepository;
import com.esteban.products.repositories.OrderRepository;
import com.esteban.products.repositories.ProductRepository;
import com.esteban.products.status.OrderStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class OrderServiceTest {

    private Long prodId1;

    private Long prodId2;

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ItemRepository itemRepository;

    @BeforeEach
    public void setUp() {
        createProducts();
    }

    @Test
    public void testCreate() {
        OrderDto orderDto = OrderDto.builder()
                .items(Arrays.asList(
                                ItemDto.builder()
                                        .quantity(7)
                                        .productId(prodId1)
                                        .build(),
                                ItemDto.builder()
                                        .quantity(5)
                                        .productId(prodId2)
                                        .build()))
                .build();

        Long orderId = orderService.create(orderDto).getId();

        Optional<Order> order = orderRepository.findById(orderId);
        assertTrue(order.isPresent(), "Order created must exist");
        assertEquals(2, order.get().getItems().size(), "2 items should be added to the order");
        assertEquals(OrderStatus.CREATED, order.get().getStatus(), "order created should have CREATED status");
        Optional<Product> product = productRepository.findById(prodId1);
        assertEquals(3, product.get().getStock(), "3 quantity should remain");

    }


    @Test()
    public void testCreateUnadequateItemQuantity() {
        OrderDto orderDto = OrderDto.builder()
                .items(Arrays.asList(
                        ItemDto.builder()
                                .quantity(12)
                                .productId(prodId1)
                                .build(),
                        ItemDto.builder()
                                .quantity(5)
                                .productId(prodId2)
                                .build()))
                .build();

        MissingStockException exception = Assertions.assertThrows(MissingStockException.class, () -> {
            orderService.create(orderDto).getId();
        });
        assertEquals(1, exception.getItems().size(), "1 Items must be returned as missing");
        assertEquals(2, exception.getItems().get(0).getQuantity(), "2 quantity must be returned as missing");
    }

    @Test
    public void testCancel() {
        OrderDto orderDto = OrderDto.builder()
                .items(Arrays.asList(
                        ItemDto.builder()
                                .quantity(7)
                                .productId(prodId1)
                                .build(),
                        ItemDto.builder()
                                .quantity(5)
                                .productId(prodId2)
                                .build()))
                .build();
        Long orderId = orderService.create(orderDto).getId();

        orderService.updateStatus(orderId, OrderStatus.CANCELLED);

        Optional<Order> order = orderRepository.findById(orderId);
        assertTrue(order.isPresent(), "Order created must exist");
        assertEquals(OrderStatus.CANCELLED, order.get().getStatus(), "order cancelled should have CANCELLED status");
        Optional<Product> product = productRepository.findById(prodId1);
        assertEquals(10, product.get().getStock(), "10 quantity should remain as order is cancelled");
    }

    private void createProducts() {
        orderRepository.deleteAll();
        itemRepository.deleteAll();
        productRepository.deleteAll();
        prodId1 = productRepository.save(Product.builder()
                        .stock(10)
                        .price(1.5)
                        .name("Milk")
                .build()).getId();
        prodId2 = productRepository.save(Product.builder()
                .stock(5)
                .price(0.75)
                .name("Bread")
                .build()).getId();
    }

}

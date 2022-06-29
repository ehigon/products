package com.esteban.products.services;

import com.esteban.products.entities.Item;
import com.esteban.products.entities.Order;
import com.esteban.products.entities.Product;
import com.esteban.products.error.MissingStockException;
import com.esteban.products.model.ItemDto;
import com.esteban.products.model.OrderDto;
import com.esteban.products.repositories.ItemRepository;
import com.esteban.products.repositories.OrderRepository;
import com.esteban.products.repositories.ProductRepository;
import com.esteban.products.status.OrderStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    private final ProductRepository productRepository;

    private final ItemRepository itemRepository;

    @Autowired
    public OrderService(OrderRepository orderRepository, ProductRepository productRepository, ItemRepository itemRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.itemRepository = itemRepository;
    }

    @Transactional
    public OrderDto create(OrderDto orderDto) {
        Map<Long, Product> products = updateProducts(orderDto);
        Iterable<Item> items = storeItems(orderDto, products);
        Order order = createOrder(items);
        return toDto(order);
    }

    @Transactional
    public OrderDto updateStatus(Long id, OrderStatus status) {
        Order order = storeStatus(id, status);
        if(status == OrderStatus.CANCELLED) {
            updateCancelledProducts(order);
        }
        return toDto(order);
    }


    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void expireOrders() {
        List<Order> orders = orderRepository.findByStatusAndCreatedBefore(OrderStatus.CREATED, new Date(getExpirationTime()));
        for(Order order : orders) {
            expireOrder(order);
        }
    }

    private void expireOrder(Order order) {
        changeStatus(order, OrderStatus.CANCELLED);
        updateCancelledProducts(order);
    }

    private Map<Long, Product> updateProducts(OrderDto orderDto) {
        Map<Long, Product> products = getMapOfProducts(orderDto);
        updateStocks(products, orderDto.getItems());
        checkAvailability(products);
        productRepository.saveAll(products.values());
        return products;
    }

    private Iterable<Item> storeItems(OrderDto orderDto, Map<Long, Product> products) {
        List<Item> items = createItems(orderDto, products);
        return itemRepository.saveAll(items);
    }

    private Map<Long, Product> getMapOfProducts(OrderDto orderDto) {
        return StreamSupport.stream(productRepository.findAllById(getProductIds(orderDto)).spliterator(), false)
                .collect(Collectors.toMap(Product::getId, Function.identity()));
    }


    private Iterable<Long> getProductIds(OrderDto orderDto) {
        return orderDto.getItems().stream()
                .map(ItemDto::getProductId)
                .collect(Collectors.toList());
    }

    private void updateStocks(Map<Long, Product> products, List<ItemDto> items) {
        items.forEach(i -> updateStock(products.get(i.getProductId()), i));
    }

    private void updateStock(Product product, ItemDto i) {
        if(product == null) {
            throw new ResponseStatusException(NOT_FOUND, "Product not found with id " + i.getProductId());
        }
        product.setStock(product.getStock() - i.getQuantity());
    }

    private void checkAvailability(Map<Long, Product> products) {
        List<ItemDto> missingItems = products.values().stream()
                .filter(p -> p.getStock()<0)
                .map(p -> ItemDto.builder()
                        .productId(p.getId())
                        .quantity(-p.getStock())
                        .build())
                .collect(Collectors.toList());
        if(!missingItems.isEmpty()) {
            throw new MissingStockException("Unable to create order as long as there are missing stock", missingItems);
        }
    }


    private List<Item> createItems(OrderDto orderDto, Map<Long, Product> products) {
        return orderDto.getItems().stream()
                .map(i -> Item.builder()
                        .quantity(i.getQuantity())
                        .product(products.get(i.getProductId()))
                        .build())
                .collect(Collectors.toList());
    }

    private Order createOrder(Iterable<Item> items) {
        Order order = new Order();
        items.forEach(i -> order.getItems().add(i));
        order.setStatus(OrderStatus.CREATED);
        order.setCreated(new Date());
        return orderRepository.save(order);
    }

    private OrderDto toDto(Order order) {
        return OrderDto.builder()
                .id(order.getId())
                .items(toDtos(order.getItems()))
                .status(order.getStatus())
                .created(order.getCreated())
                .build();
    }

    private List<ItemDto> toDtos(List<Item> items) {
        return items.stream()
                .map(i -> ItemDto.builder()
                        .quantity(i.getQuantity())
                        .productId(i.getProduct().getId())
                        .build()).collect(Collectors.toList());
    }

    private boolean isOrderExpired(Order existingOrder) {
        return existingOrder.getCreated().getTime() < getExpirationTime();
    }

    private long getExpirationTime() {
        return new Date().getTime() - 30 * 60 * 1000;
    }

    private Order storeStatus(Long id, OrderStatus status) {
        Optional<Order> optOrder = orderRepository.findById(id);
        if(!optOrder.isPresent()) {
            throw new ResponseStatusException(NOT_FOUND, "Unable to find order with id " +id);
        }
        Order order = optOrder.get();
        if(order.getStatus()!=OrderStatus.CREATED) {
            throw new ResponseStatusException(CONFLICT, "Order with id " + id + " has status " + order.getStatus());
        }
        if(isOrderExpired(order)) {
            throw new ResponseStatusException(CONFLICT, "Order with id " + id + " is expired");
        }

        return changeStatus(order, status);
    }

    private Order changeStatus(Order order, OrderStatus status) {
        order.setStatus(status);
        return orderRepository.save(order);
    }

    private void updateCancelledProducts(Order order) {
        List<Product> products = order.getItems().stream()
                .map(this::updateCancelled)
                .collect(Collectors.toList());
        productRepository.saveAll(products);
    }

    private Product updateCancelled(Item item) {
        Product product = item.getProduct();
        product.setStock(product.getStock() + item.getQuantity());
        return product;
    }

}

package com.esteban.products.controllers;

import com.esteban.products.model.OrderDto;
import com.esteban.products.services.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }


    @PostMapping
    public OrderDto create(@RequestBody OrderDto orderDto) {
        return orderService.create(orderDto);
    }

    @PatchMapping("/{id}")
    public OrderDto updateStatus(@PathVariable Long id, @RequestBody OrderDto orderDto) {
        if(orderDto.getStatus() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only status change is allowed");
        }
        return orderService.updateStatus(id, orderDto.getStatus());
    }

}

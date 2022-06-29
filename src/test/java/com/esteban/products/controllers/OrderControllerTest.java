package com.esteban.products.controllers;

import com.esteban.products.model.ItemDto;
import com.esteban.products.model.OrderDto;
import com.esteban.products.services.OrderService;
import com.esteban.products.services.ProductService;
import com.esteban.products.status.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
public class OrderControllerTest {

    @MockBean
    private OrderService orderService;

    @MockBean
    private ProductService productService;

    @Autowired
    private MockMvc mvc;

    @BeforeEach
    public void setUp() {
        Mockito.when(orderService.create(Mockito.any())).thenReturn(getOrder(OrderStatus.CREATED));
        Mockito.when(orderService.updateStatus(Mockito.anyLong(), Mockito.any())).thenReturn(getOrder(OrderStatus.CANCELLED));
    }

    @Test
    public void testCreate() throws Exception {
        mvc.perform(post("/orders").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"items\": [{\"productId\" : 1, \"quantity\": 4}, {\"productId\" : 2, \"quantity\": 4}]}"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void testUpdateStatus() throws Exception {
        mvc.perform(patch("/orders/1").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"CANCELLED\"}"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    private OrderDto getOrder(OrderStatus status) {
        return OrderDto.builder()
                .id(1L)
                .created(new Date())
                .status(status)
                .items(getItems())
                .build();
    }

    private List<ItemDto> getItems() {
        return Collections.singletonList(ItemDto.builder().productId(1L).quantity(10).build());
    }

}

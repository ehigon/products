package com.esteban.products.controllers;

import com.esteban.products.model.ProductDto;
import com.esteban.products.services.OrderService;
import com.esteban.products.services.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
public class ProductControllerTest {

    @MockBean
    private OrderService orderService;

    @MockBean
    private ProductService productService;

    @Autowired
    private MockMvc mvc;

    @BeforeEach
    public void setUp() {
        Mockito.when(productService.create(Mockito.any())).thenReturn(getProduct());
        Mockito.when(productService.update(Mockito.anyLong(), Mockito.any())).thenReturn(getProduct());
    }

    @Test
    public void testCreate() throws Exception {
        mvc.perform(post("/products").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Milk\", \"stock\": 10, \"price\": 1.5}"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void testDelete() throws Exception {
        mvc.perform(delete("/products/1"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void testUpdate() throws Exception {
        mvc.perform(put("/products/1").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Cow Milk\", \"stock\": 10, \"price\": 1.5}"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    private ProductDto getProduct() {
        return ProductDto.builder()
                .id(1L)
                .name("Milk")
                .price(1.5)
                .stock(10)
                .build();
    }


}

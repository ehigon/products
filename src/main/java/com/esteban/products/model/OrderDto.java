package com.esteban.products.model;

import com.esteban.products.status.OrderStatus;
import lombok.*;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDto {

    private Long id;

    private List<ItemDto> items;

    private OrderStatus status;

    private Date created;

}

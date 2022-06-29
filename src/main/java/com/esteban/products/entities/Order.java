package com.esteban.products.entities;

import com.esteban.products.status.OrderStatus;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity(name = "p_order")
@Getter
@Setter
public class Order {

    @Id
    @GeneratedValue
    private Long id;

    private OrderStatus status;

    @OneToMany(fetch = FetchType.EAGER)
    private List<Item> items = new ArrayList<>();

    @Temporal(TemporalType.TIMESTAMP)
    private Date created;

}

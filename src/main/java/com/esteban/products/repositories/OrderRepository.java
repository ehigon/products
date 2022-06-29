package com.esteban.products.repositories;

import com.esteban.products.entities.Order;
import com.esteban.products.status.OrderStatus;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface OrderRepository extends CrudRepository<Order, Long> {

    List<Order> findByStatusAndCreatedBefore(OrderStatus status, Date date);

}

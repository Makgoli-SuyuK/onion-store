package com.example.onionstore.domain.order.repository;

import com.example.onionstore.domain.order.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    @Query("SELECT oi FROM OrderItem oi WHERE oi.order.id IN :orderIds")
    List<OrderItem> findAllByOrder_IdIn(@Param("orderIds") List<Long> orderIds);

    @Query("SELECT oi FROM OrderItem oi WHERE oi.order.id = :orderId")
    List<OrderItem> findAllByOrderId(@Param("orderId") Long orderId);
}

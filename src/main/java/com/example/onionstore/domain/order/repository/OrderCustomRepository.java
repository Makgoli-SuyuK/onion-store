package com.example.onionstore.domain.order.repository;

import com.example.onionstore.domain.order.dto.OrderSearchRequest;
import com.example.onionstore.domain.order.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface OrderCustomRepository {

    Page<Order> findAllByUser_IdWithKeyword(Long userId, Pageable pageable, OrderSearchRequest request);
}

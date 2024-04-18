package com.senior.assessment.domain.repository;

import com.senior.assessment.domain.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID>, QuerydslPredicateExecutor<Order> {
    boolean existsOrderById(UUID orderId);
}

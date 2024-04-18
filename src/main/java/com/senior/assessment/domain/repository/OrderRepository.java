package com.senior.assessment.domain.repository;

import com.senior.assessment.domain.entity.Order;
import com.senior.assessment.domain.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID>, QuerydslPredicateExecutor<Order> {
    boolean existsOrderById(UUID orderId);

    @Modifying
    @Transactional
    @Query("UPDATE Order o SET o.status = :newStatus WHERE o.id = :id")
    void updateStatus(@Param("id") UUID id, @Param("newStatus") OrderStatus newStatus);
}

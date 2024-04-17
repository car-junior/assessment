package com.senior.assessment.domain.repository;

import com.senior.assessment.domain.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OrderItemRepository extends JpaRepository<OrderItem, UUID>{
    boolean existsOrderItemByItemId(UUID itemId);
}

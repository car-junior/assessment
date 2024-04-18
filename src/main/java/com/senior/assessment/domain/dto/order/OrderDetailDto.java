package com.senior.assessment.domain.dto.order;

import com.senior.assessment.domain.entity.OrderItem;
import com.senior.assessment.domain.enums.OrderStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class OrderDetailDto {
    private UUID id;
    private double discount;
    private OrderStatus status;
    private List<OrderItemDetailDto> orderItems;
}

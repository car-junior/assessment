package com.senior.assessment.domain.dto.order;

import com.senior.assessment.domain.enums.OrderStatus;
import com.senior.assessment.domain.validators.order.OrderStatusChange;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusChangeDto {
    @NotNull
    @OrderStatusChange(anyOf = {OrderStatus.CLOSED})
    private OrderStatus status;
}

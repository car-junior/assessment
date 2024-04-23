package com.senior.assessment.domain.dto.order.detailslist;

import com.senior.assessment.domain.enums.ItemType;
import com.senior.assessment.domain.enums.OrderStatus;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class OrderDetailDto {
    private UUID id;
    private double discount;
    private OrderStatus status;
    private BigDecimal total;
    private BigDecimal totalProduct;
    private BigDecimal totalService;
    private List<OrderItemDetailDto> orderItems;

    public BigDecimal getTotalService() {
        return calculateTotal(ItemType.SERVICE)
                .setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getTotalProduct() {
        return calculateTotal(ItemType.PRODUCT)
                .multiply(BigDecimal.valueOf(1 - discount))
                .setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getTotal() {
        return getTotalService().add(getTotalProduct()).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateTotal(ItemType itemType) {
        return orderItems.stream()
                .filter(orderItem -> orderItem.getItem().getType() == itemType)
                .map(orderItem -> orderItem.getItemPrice().multiply(BigDecimal.valueOf(orderItem.getAmount())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}

package com.senior.assessment.domain.dto.order.createupdate;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDto {
    private UUID id;

    @NotNull
    private ItemDto item;

    @NotNull
    @Positive
    private Integer amount;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderItemDto that = (OrderItemDto) o;
        return Objects.equals(id, that.id) && item.getId().equals(that.item.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, item.getId());
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemDto {
        @NotNull
        private UUID id;
    }
}

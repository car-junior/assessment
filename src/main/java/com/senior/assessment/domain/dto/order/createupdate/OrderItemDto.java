package com.senior.assessment.domain.dto.order.createupdate;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
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
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class ItemDto {
        @NotNull
        private UUID id;
    }
}

package com.senior.assessment.domain.querydsl.search;

import com.senior.assessment.domain.enums.ItemType;
import com.senior.assessment.domain.enums.OrderStatus;
import lombok.Builder;

import java.util.UUID;

@Builder
public record OrderSearch(UUID id, String query, ItemType itemType, OrderStatus status) {
}

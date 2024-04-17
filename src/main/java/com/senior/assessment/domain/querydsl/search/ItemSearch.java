package com.senior.assessment.domain.querydsl.search;

import com.senior.assessment.domain.enums.ItemStatus;
import com.senior.assessment.domain.enums.ItemType;
import lombok.Builder;

import java.util.UUID;

@Builder
public record ItemSearch(UUID id, String query, ItemType type, ItemStatus status) {
}

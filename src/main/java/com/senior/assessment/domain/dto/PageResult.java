package com.senior.assessment.domain.dto;

import lombok.*;

import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {
    private int totalPages;
    private long totalResults;
    private List<T> result = new ArrayList<>();
}
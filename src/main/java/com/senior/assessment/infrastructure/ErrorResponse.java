package com.senior.assessment.infrastructure;

import lombok.*;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    private Integer status;
    private String message;
    private Map<String, List<String>> errors;
}

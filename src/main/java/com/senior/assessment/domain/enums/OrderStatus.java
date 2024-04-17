package com.senior.assessment.domain.enums;

import java.util.Arrays;

import static com.senior.assessment.utilities.Utils.existsValue;

public enum OrderStatus {

    OPENED("OPENED"),
    CLOSED("CLOSED");

    private final String code;

    OrderStatus(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static OrderStatus of(String value) {
        return Arrays.stream(OrderStatus.values())
                .filter(v -> existsValue(value) && value.equals(v.getCode()))
                .findFirst()
                .orElse(null);
    }
}
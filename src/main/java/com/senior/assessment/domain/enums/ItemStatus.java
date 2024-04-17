package com.senior.assessment.domain.enums;

import java.util.Arrays;

import static com.senior.assessment.utilities.Utils.existsValue;

public enum ItemStatus {

    ACTIVE("ACTIVE"),
    DISABLED("DISABLED");

    private final String code;

    ItemStatus(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static ItemStatus of(String value) {
        return Arrays.stream(ItemStatus.values())
                .filter(v -> existsValue(value) && value.equals(v.getCode()))
                .findFirst()
                .orElse(null);
    }
}
package com.senior.assessment.domain.enums;

import java.util.Arrays;

import static com.senior.assessment.utilities.Utils.existsValue;

public enum ItemType {

    PRODUCT("PRODUCT"),
    SERVICE("SERVICE");

    private final String code;

    ItemType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static ItemType of(String value) {
        return Arrays.stream(ItemType.values())
                .filter(v -> existsValue(value) && value.equals(v.getCode()))
                .findFirst()
                .orElse(null);
    }
}
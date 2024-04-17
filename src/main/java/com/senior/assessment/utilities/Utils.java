package com.senior.assessment.utilities;

import org.apache.logging.log4j.util.Strings;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Optional;

public class Utils {
    private Utils() {
    }

    public static boolean existsValue(String value) {
        return Strings.isNotEmpty(value) && Strings.isNotBlank(value);
    }

    public static boolean isPresent(Object value) {
        if (value instanceof String)
            return existsValue((String) value);
        return Optional.ofNullable(value).isPresent();
    }

    public static Pageable createPagination(int page, int itemsPerPage, String sort, String sortName) {
        return PageRequest.of(page, itemsPerPage, Sort.by(Sort.Direction.fromString(sort), sortName));
    }
}

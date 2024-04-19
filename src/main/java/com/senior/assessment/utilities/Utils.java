package com.senior.assessment.utilities;

import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.core.types.dsl.StringTemplate;
import org.apache.logging.log4j.util.Strings;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.text.Normalizer;
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

    public static StringTemplate unaccentedExpression(StringPath stringPath) {
        return Expressions.stringTemplate("FUNCTION('unaccent', {0})", stringPath);
    }
    public static String unaccented(String src) {
        return Normalizer
                .normalize(src, Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", "");
    }
}

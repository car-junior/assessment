package com.senior.assessment.utilities;

import org.apache.logging.log4j.util.Strings;

public class Utils {
    private Utils() {
    }

    public static boolean existsValue(String value) {
        return Strings.isNotEmpty(value) && Strings.isNotBlank(value);
    }
}

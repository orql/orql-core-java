package com.github.orql.core.util;

public class OrqlUtil {

    public static String getKeyword(String orql) {
        String[] arr = orql.split(" ", 2);
        return arr[0];
    }

    public static String getSchema(String orql) {
        String[] arr = orql.split(" |:", 2);
        return arr[0];
    }

}

package com.ivan.inbound.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ClassUtil {
    public static <T> String target(Class<T> clazz) {
        return clazz.getName();
    }
}

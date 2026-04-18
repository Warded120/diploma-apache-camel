package com.ivan.inbound.enumeration;

import java.util.Arrays;

public enum OrderType {

    ELECTRONICS("electronics", 1),
    CLOTHING("clothing", 2),
    FOOD("food", 3),
    FURNITURE("furniture", 4),
    SPORTS("sports", 5),
    OTHER("other", 0);

    public final String code;
    public final int byteCode;

    OrderType(String code, int byteCode) {
        this.code = code;
        this.byteCode = byteCode;
    }

    public static int fromCode(String code) {
        return Arrays.stream(OrderType.values())
                .filter(orderType -> orderType.code.equals(code))
                .findFirst()
                .map(orderType -> orderType.byteCode)
                .orElseThrow(() -> new IllegalArgumentException("Unknown OrderType code: " + code));
    }
}
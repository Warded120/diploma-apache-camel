package com.ivan.outbound.enumeration;

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

    public static OrderType fromCode(int code) {
        return Arrays.stream(OrderType.values())
                .filter(orderType -> orderType.byteCode == code)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown OrderType code: " + code));
    }
}

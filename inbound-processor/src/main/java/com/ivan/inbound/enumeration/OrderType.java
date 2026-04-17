package com.ivan.inbound.enumeration;

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
        for (OrderType orderType : OrderType.values()) {
            if (orderType.code.equals(code)) {
                return orderType.byteCode;
            }
        }
        throw new IllegalArgumentException("Unknown OrderType code: " + code);
    }
}
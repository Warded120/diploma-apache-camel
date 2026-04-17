package com.ivan.inbound.enumeration;

public enum OrderType {

    ELECTRONICS("electronics", (byte) 1),
    CLOTHING("clothing", (byte) 2),
    FOOD("food", (byte) 3),
    FURNITURE("furniture", (byte) 4),
    SPORTS("sports", (byte) 5),
    OTHER("other", (byte) 0);

    public final String code;
    public final byte byteCode;

    OrderType(String code, byte byteCode) {
        this.code = code;
        this.byteCode = byteCode;
    }

    public static byte fromCode(String code) {
        for (OrderType orderType : OrderType.values()) {
            if (orderType.code.equals(code)) {
                return orderType.byteCode;
            }
        }
        throw new IllegalArgumentException("Unknown OrderType code: " + code);
    }
}
package com.ivan.outbound.enumeration;

public enum OrderType {
    ELECTRONICS("EL", (byte) 1),
    CLOTHING("CL",    (byte) 2),
    FOOD("FD",        (byte) 3),
    FURNITURE("FN",   (byte) 4),
    SPORTS("SP",      (byte) 5),
    OTHER("OT",       (byte) 0);

    public final String code;
    public final byte byteCode;

    OrderType(String code, byte byteCode) {
        this.code = code;
        this.byteCode = byteCode;
    }
}
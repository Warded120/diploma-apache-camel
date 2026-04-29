package com.ivan.outbound.constants;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ExchangeConstants {
    public static final String HEADER_ACTION = "action";

    public static final String PROP_CUSTOMER = "customer";
    public static final String PROP_PRODUCT  = "product";
    public static final String PROP_ORDER_MESSAGE = "orderMessage";

    public static final String HEADER_BASE_CURRENCY = "baseCurrency";
    public static final String HEADER_PRICE_USD     = "priceUsd";

    public static final double DISCOUNT = 0.1;
}


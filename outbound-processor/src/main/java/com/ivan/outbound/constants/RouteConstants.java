package com.ivan.outbound.constants;

import lombok.experimental.UtilityClass;

@UtilityClass
public class RouteConstants {
    public static final String ORDER_CONSUMER_ROUTE_ID = "order-consumer-route";

    public static final String CREATE_ORDER_ROUTE = "create-order-route";
    public static final String CREATE_ORDER_ROUTE_ID = routeId(CREATE_ORDER_ROUTE);

    public static final String UPDATE_ORDER_ROUTE = "update-order-route";
    public static final String UPDATE_ORDER_ROUTE_ID = routeId(UPDATE_ORDER_ROUTE);

    public static final String DELETE_ORDER_ROUTE = "delete-order-route";
    public static final String DELETE_ORDER_ROUTE_ID = routeId(DELETE_ORDER_ROUTE);

    public static final String CURRENCY_ENRICHER_ROUTE = "currency-enricher-route";
    public static final String CURRENCY_ENRICHER_ROUTE_ID = routeId(CURRENCY_ENRICHER_ROUTE);

    public static final String CURRENCY_API_ROUTE = "currency-api-route";
    public static final String CURRENCY_API_ROUTE_ID = routeId(CURRENCY_API_ROUTE);

    private static String routeId(String routeName) {
        return routeName + "-id";
    }
}

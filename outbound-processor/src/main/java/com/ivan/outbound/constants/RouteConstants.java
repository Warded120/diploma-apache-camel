package com.ivan.outbound.constants;

import lombok.experimental.UtilityClass;

@UtilityClass
public class RouteConstants {
    public static final String CREATE_ORDER_ROUTE = "create-order-route";
    public static final String CREATE_ORDER_ROUTE_ID = routeId(CREATE_ORDER_ROUTE);

    public static final String UPDATE_ORDER_ROUTE = "update-order-route";
    public static final String UPDATE_ORDER_ROUTE_ID = routeId(UPDATE_ORDER_ROUTE);

    public static final String DELETE_ORDER_ROUTE = "delete-order-route";
    public static final String DELETE_ORDER_ROUTE_ID = routeId(DELETE_ORDER_ROUTE);

    private static String routeId(String routeName) {
        return routeName + "-id";
    }
}

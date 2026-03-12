package com.ivan.route;

import org.apache.camel.builder.endpoint.EndpointRouteBuilder;

import static com.ivan.constants.RouteConstants.CREATE_ORDER_ROUTE;
import static com.ivan.constants.RouteConstants.DELETE_ORDER_ROUTE;
import static com.ivan.constants.RouteConstants.GET_ALL_ORDERS_ROUTE;
import static com.ivan.constants.RouteConstants.GET_ORDER_BY_ID_ROUTE;
import static com.ivan.constants.RouteConstants.UPDATE_ORDER_ROUTE;

public class OrdersRoute extends EndpointRouteBuilder {
    @Override
    public void configure() {
        restConfiguration()
            .component("netty-http")
            .host("localhost")
            .port(8080)
            .enableCORS(true)
            .apiContextPath("/api-doc")
            .apiProperty("api.title", "Orders API")
            .apiProperty("api.version", "1.0.0")
            .apiProperty("cors", "true");

        rest("/orders")
            .get().to(direct(GET_ALL_ORDERS_ROUTE))
            .get("/{id}").to(direct(GET_ORDER_BY_ID_ROUTE))
            .post().to(direct(CREATE_ORDER_ROUTE))
            .put("/{id}").to(direct(UPDATE_ORDER_ROUTE))
            .delete("/{id}").to(direct(DELETE_ORDER_ROUTE));
    }
}

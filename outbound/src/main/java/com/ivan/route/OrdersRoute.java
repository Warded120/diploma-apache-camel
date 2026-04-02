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
        //TODO: add exception handling
        //TODO: modify route to build a message and push it to kafka
        // headers: action type (create, update, delete)
        // payload: order itself, extend the order with different values
        rest("/orders")
            .consumes("application/json")
            .get().to(direct(GET_ALL_ORDERS_ROUTE))
            .get("/{id}").to(direct(GET_ORDER_BY_ID_ROUTE))
            .post().to(direct(CREATE_ORDER_ROUTE))
            .put("/{id}").to(direct(UPDATE_ORDER_ROUTE))
            .delete("/{id}").to(direct(DELETE_ORDER_ROUTE));
    }
}

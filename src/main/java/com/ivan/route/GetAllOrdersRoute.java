package com.ivan.route;

import org.apache.camel.builder.endpoint.EndpointRouteBuilder;

import static com.ivan.constants.RouteConstants.GET_ALL_ORDERS_ROUTE;
import static com.ivan.constants.RouteConstants.GET_ALL_ORDERS_ROUTE_ID;

public class GetAllOrdersRoute extends EndpointRouteBuilder {
    @Override
    public void configure() throws Exception {
        from(direct(GET_ALL_ORDERS_ROUTE))
            .routeId(GET_ALL_ORDERS_ROUTE_ID)
            .setBody(simple("Getting all orders"))
            .log("called route: " + GET_ALL_ORDERS_ROUTE_ID);
    }
}

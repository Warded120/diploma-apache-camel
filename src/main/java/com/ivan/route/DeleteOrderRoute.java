package com.ivan.route;

import org.apache.camel.builder.endpoint.EndpointRouteBuilder;

import static com.ivan.constants.RouteConstants.DELETE_ORDER_ROUTE;
import static com.ivan.constants.RouteConstants.DELETE_ORDER_ROUTE_ID;

public class DeleteOrderRoute extends EndpointRouteBuilder {
    @Override
    public void configure() throws Exception {
        from(direct(DELETE_ORDER_ROUTE))
            .routeId(DELETE_ORDER_ROUTE_ID)
            .setBody(simple("Deleting order by ID: ${header.id}"))
            .log("called route: " + DELETE_ORDER_ROUTE);
    }
}

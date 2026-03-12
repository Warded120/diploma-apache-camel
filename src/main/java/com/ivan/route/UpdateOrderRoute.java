package com.ivan.route;

import org.apache.camel.builder.endpoint.EndpointRouteBuilder;

import static com.ivan.constants.RouteConstants.UPDATE_ORDER_ROUTE;
import static com.ivan.constants.RouteConstants.UPDATE_ORDER_ROUTE_ID;

public class UpdateOrderRoute extends EndpointRouteBuilder {
    @Override
    public void configure() throws Exception {
        from(direct(UPDATE_ORDER_ROUTE))
            .routeId(UPDATE_ORDER_ROUTE_ID)
            .setBody(simple("Updating order by ID: ${header.id}"))
            .log("called route: " + UPDATE_ORDER_ROUTE);
    }
}

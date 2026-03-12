package com.ivan.route;

import org.apache.camel.builder.endpoint.EndpointRouteBuilder;

import static com.ivan.constants.RouteConstants.GET_ORDER_BY_ID_ROUTE;
import static com.ivan.constants.RouteConstants.GET_ORDER_BY_ID_ROUTE_ID;

public class GetOrderByIdRoute extends EndpointRouteBuilder {
    @Override
    public void configure() throws Exception {
        from(direct(GET_ORDER_BY_ID_ROUTE))
            .routeId(GET_ORDER_BY_ID_ROUTE_ID)
            .setBody(simple("Getting order by ID: ${header.id}"))
            .log("called route: " + GET_ORDER_BY_ID_ROUTE);
    }
}

package com.ivan.inbound.route;

import org.apache.camel.Exchange;
import org.apache.camel.builder.endpoint.EndpointRouteBuilder;

import static com.ivan.inbound.constants.RouteConstants.CREATE_ORDER_ROUTE;
import static com.ivan.inbound.constants.RouteConstants.DELETE_ORDER_ROUTE;
import static com.ivan.inbound.constants.RouteConstants.UPDATE_ORDER_ROUTE;
import static org.apache.camel.LoggingLevel.ERROR;

public class OrdersRoute extends EndpointRouteBuilder {

    @Override
    public void configure() {

        onException(Exception.class)
                .handled(true)
                .log(ERROR, "Unexpected error processing order request: ${exception.message}")
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(500))
                .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
                .setBody(constant("{\"error\":\"Internal server error\"}"));

        rest("/orders")
            .consumes("application/json")
            .produces("application/json")
            .post().to(direct(CREATE_ORDER_ROUTE))
            .put("/{id}").to(direct(UPDATE_ORDER_ROUTE))
            .delete("/{id}").to(direct(DELETE_ORDER_ROUTE));
    }
}

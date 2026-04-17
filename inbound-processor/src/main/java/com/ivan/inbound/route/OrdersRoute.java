package com.ivan.inbound.route;

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
                .log(ERROR, "Error processing order request: ${exception.message}")
                .log("Sending message to Kafka dead letter topic")
                .setHeader("error", simple("${exception.message}"))
                .to(kafka("{{kafka.dead-letter-topic}}"));

        rest("/orders")
            .consumes("application/json")
            .produces("application/json")
            .post().to(direct(CREATE_ORDER_ROUTE))
            .put("/{id}").to(direct(UPDATE_ORDER_ROUTE))
            .delete("/{id}").to(direct(DELETE_ORDER_ROUTE));
    }
}

package com.ivan.inbound.route;

import org.apache.camel.builder.endpoint.EndpointRouteBuilder;

import static com.ivan.inbound.constants.ExchangeConstants.HEADER_ACTION;
import static com.ivan.inbound.constants.ExchangeConstants.HEADER_ID;
import static com.ivan.inbound.constants.RouteConstants.DELETE_ORDER_ROUTE;
import static com.ivan.inbound.constants.RouteConstants.DELETE_ORDER_ROUTE_ID;
import static com.ivan.inbound.enumeration.OrderAction.DELETE;

public class DeleteOrderRoute extends EndpointRouteBuilder {
    @Override
    public void configure() {
        onException(Exception.class)
                .handled(true)
                .log("Exception occurred for offset ${header[kafka.OFFSET]}: ${exception.message}");

        from(direct(DELETE_ORDER_ROUTE))
            .routeId(DELETE_ORDER_ROUTE_ID)
                .removeHeaders("*", HEADER_ID)
                .setHeader(HEADER_ACTION, constant(DELETE.getAction()))
                .setBody(constant(null))
                .to(kafka("{{kafka.topic}}"))
                .setBody(simple("Order delete request is created"))
                .log("Order delete request sent to Kafka");
    }
}

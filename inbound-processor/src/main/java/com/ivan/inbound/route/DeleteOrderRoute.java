package com.ivan.inbound.route;

import com.ivan.inbound.exception.InvalidOrderIdException;
import com.ivan.inbound.processor.ValidateOrderIdProcessor;
import org.apache.camel.Exchange;
import org.apache.camel.builder.endpoint.EndpointRouteBuilder;

import static com.ivan.inbound.constants.ExchangeConstants.HEADER_ACTION;
import static com.ivan.inbound.constants.ExchangeConstants.HEADER_ID;
import static com.ivan.inbound.constants.RouteConstants.DELETE_ORDER_ROUTE;
import static com.ivan.inbound.constants.RouteConstants.DELETE_ORDER_ROUTE_ID;
import static com.ivan.inbound.enumeration.OrderAction.DELETE;

public class DeleteOrderRoute extends EndpointRouteBuilder {

    @Override
    public void configure() {

        onException(InvalidOrderIdException.class)
                .handled(true)
                .log("Delete order request has invalid id: ${exception.message}")
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(400))
                .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
                .setBody(simple("{\"error\":\"${exception.message}\"}"));

        from(direct(DELETE_ORDER_ROUTE))
            .routeId(DELETE_ORDER_ROUTE_ID)
            .process(new ValidateOrderIdProcessor())
            .removeHeaders("*", HEADER_ID)
            .setHeader(HEADER_ACTION, constant(DELETE.getAction()))
            .setBody(constant(null))
            .to(kafka("{{kafka.topic}}"))
            .setBody(simple("Order delete request is created"))
            .log("Order delete request sent to Kafka");
    }
}

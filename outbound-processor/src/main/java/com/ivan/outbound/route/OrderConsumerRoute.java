package com.ivan.outbound.route;

import org.apache.camel.builder.endpoint.EndpointRouteBuilder;
import org.apache.kafka.common.errors.SerializationException;

import static com.ivan.outbound.constants.ExchangeConstants.HEADER_ACTION;
import static com.ivan.outbound.constants.RouteConstants.CREATE_ORDER_ROUTE;
import static com.ivan.outbound.constants.RouteConstants.DELETE_ORDER_ROUTE;
import static com.ivan.outbound.constants.RouteConstants.ORDER_CONSUMER_ROUTE_ID;
import static com.ivan.outbound.constants.RouteConstants.UPDATE_ORDER_ROUTE;
import static com.ivan.outbound.enumeration.OrderAction.CREATE;
import static com.ivan.outbound.enumeration.OrderAction.DELETE;
import static com.ivan.outbound.enumeration.OrderAction.UPDATE;
import static org.apache.camel.LoggingLevel.ERROR;

public class OrderConsumerRoute extends EndpointRouteBuilder {

    @Override
    public void configure() {

        onException(SerializationException.class)
                .handled(true)
                .log(ERROR, "Failed to deserialize Kafka message, sending to dead-letter queue: ${exception.message}")
                .setHeader("error", simple("${exception.message}"))
                .setBody(simple("${exception.message}"))
                .to(kafka("{{kafka.dead-letter-topic}}"));

        onException(Exception.class)
                .handled(true)
                .log(ERROR, "Unexpected error processing Kafka message, sending to dead-letter queue: ${exception.message}")
                .setHeader("error", simple("${exception.message}"))
                .setBody(simple("${exception.message}"))
                .to(kafka("{{kafka.dead-letter-topic}}"));

        from(kafka("{{kafka.topic}}").groupId("{{kafka.group-id}}"))
                .routeId(ORDER_CONSUMER_ROUTE_ID)
                .log("Consuming message with offset ${header[kafka.OFFSET]}")
        .choice()
          .when(header(HEADER_ACTION).isEqualTo(CREATE.getAction()))
            .to(direct(CREATE_ORDER_ROUTE))
          .when(header(HEADER_ACTION).isEqualTo(UPDATE.getAction()))
            .to(direct(UPDATE_ORDER_ROUTE))
          .when(header(HEADER_ACTION).isEqualTo(DELETE.getAction()))
            .to(direct(DELETE_ORDER_ROUTE))
          .otherwise()
            .log("Unknown action type: ${header.action}");
    }
}

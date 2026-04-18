package com.ivan.outbound.route;

import org.apache.camel.builder.endpoint.EndpointRouteBuilder;

import static com.ivan.outbound.constants.ExchangeConstants.HEADER_ACTION;
import static com.ivan.outbound.constants.RouteConstants.CREATE_ORDER_ROUTE;
import static com.ivan.outbound.constants.RouteConstants.DELETE_ORDER_ROUTE;
import static com.ivan.outbound.constants.RouteConstants.UPDATE_ORDER_ROUTE;
import static com.ivan.outbound.enumeration.OrderAction.CREATE;
import static com.ivan.outbound.enumeration.OrderAction.DELETE;
import static com.ivan.outbound.enumeration.OrderAction.UPDATE;

public class OrderConsumerRoute extends EndpointRouteBuilder {

    @Override
    public void configure() {
       from(kafka("{{kafka.topic}}").groupId("{{kafka.group-id}}"))
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

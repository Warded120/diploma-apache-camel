package com.ivan.outbound.route;

import com.ivan.outbound.enumeration.OrderAction;
import org.apache.camel.builder.endpoint.EndpointRouteBuilder;

import static com.ivan.outbound.constants.ExchangeConstants.HEADER_ACTION;
import static com.ivan.outbound.constants.RouteConstants.CREATE_ORDER_ROUTE;
import static com.ivan.outbound.constants.RouteConstants.DELETE_ORDER_ROUTE;
import static com.ivan.outbound.constants.RouteConstants.UPDATE_ORDER_ROUTE;
import static com.ivan.outbound.enumeration.OrderAction.CREATE;
import static com.ivan.outbound.enumeration.OrderAction.DELETE;
import static com.ivan.outbound.enumeration.OrderAction.UPDATE;

public class OrdersRoute extends EndpointRouteBuilder {

    @Override
    public void configure() {
        //TODO: modify route to consume from Kafka
        // get the action type from headers and handle message
        // think of creative ways to handle message
       from(kafka("{{kafka.topic}}").groupId("{{kafka.group-id}}"))
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

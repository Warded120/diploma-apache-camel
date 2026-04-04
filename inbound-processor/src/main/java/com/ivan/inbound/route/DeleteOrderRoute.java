package com.ivan.inbound.route;

import com.ivan.inbound.processor.OrderActionResolverProcessor;
import org.apache.camel.builder.endpoint.EndpointRouteBuilder;

import static com.ivan.inbound.constants.RouteConstants.DELETE_ORDER_ROUTE;
import static com.ivan.inbound.constants.RouteConstants.DELETE_ORDER_ROUTE_ID;
import static com.ivan.inbound.enumeration.OrderAction.DELETE;

public class DeleteOrderRoute extends EndpointRouteBuilder {
    @Override
    public void configure() {
        //TODO: improve exception handling
        onException(Exception.class)
                .handled(true)
                .log("request does not contain id...");

        from(direct(DELETE_ORDER_ROUTE))
            .routeId(DELETE_ORDER_ROUTE_ID)
                .process(new OrderActionResolverProcessor(DELETE))
                .to(kafka("{{kafka.topic}}"))
                .log("Order delete request sent to Kafka");

    }
}

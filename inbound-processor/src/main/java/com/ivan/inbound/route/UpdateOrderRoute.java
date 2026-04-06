package com.ivan.inbound.route;

import com.ivan.inbound.dto.OrderDto;
import com.ivan.inbound.entity.Order;
import com.ivan.inbound.processor.OrderActionResolverProcessor;
import org.apache.camel.builder.endpoint.EndpointRouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;

import static com.ivan.inbound.constants.RouteConstants.UPDATE_ORDER_ROUTE;
import static com.ivan.inbound.constants.RouteConstants.UPDATE_ORDER_ROUTE_ID;
import static com.ivan.inbound.enumeration.OrderAction.*;
import static com.ivan.inbound.util.ClassUtil.target;

public class UpdateOrderRoute extends EndpointRouteBuilder {
    @Override
    public void configure() {
        //TODO: improve exception handling
        onException(Exception.class)
                .handled(true)
                .log("request does not contain id...")
                .log("OR request does not have valid body...");


        from(direct(UPDATE_ORDER_ROUTE))
            .routeId(UPDATE_ORDER_ROUTE_ID)
                .unmarshal().json(JsonLibrary.Jackson, OrderDto.class)
                .to(mapstruct(target(Order.class)))
                .process(new OrderActionResolverProcessor(UPDATE))
                //TODO: how to ignore/remove all headers except custom ones? (action, id)
                .marshal().json(JsonLibrary.Jackson)
                .to(kafka("{{kafka.topic}}"))
                .log("Order update request sent to Kafka");
    }
}

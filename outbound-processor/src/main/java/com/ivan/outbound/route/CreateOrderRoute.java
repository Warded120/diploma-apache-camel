package com.ivan.outbound.route;

import com.ivan.outbound.entity.Order;
import org.apache.camel.builder.endpoint.EndpointRouteBuilder;

import static com.ivan.outbound.constants.RouteConstants.CREATE_ORDER_ROUTE;
import static com.ivan.outbound.constants.RouteConstants.CREATE_ORDER_ROUTE_ID;
import static com.ivan.outbound.util.ClassUtil.target;

public class CreateOrderRoute extends EndpointRouteBuilder {
    @Override
    public void configure() {
        from(direct(CREATE_ORDER_ROUTE))
            .routeId(CREATE_ORDER_ROUTE_ID)
            .transacted()
            .log("Creating order...")
            .to(mapstruct(target(Order.class)))
            .to(jpa(target(Order.class)));
    }
}

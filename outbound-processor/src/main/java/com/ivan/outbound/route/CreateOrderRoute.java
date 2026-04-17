package com.ivan.outbound.route;

import com.ivan.outbound.entity.Order;
import com.ivan.outbound.message.OrderMessage;
import org.apache.camel.builder.endpoint.EndpointRouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;

import static com.ivan.outbound.constants.RouteConstants.CREATE_ORDER_ROUTE;
import static com.ivan.outbound.constants.RouteConstants.CREATE_ORDER_ROUTE_ID;
import static com.ivan.outbound.util.ClassUtil.target;

public class CreateOrderRoute extends EndpointRouteBuilder {
    @Override
    public void configure() throws Exception {
        from(direct(CREATE_ORDER_ROUTE))
            .routeId(CREATE_ORDER_ROUTE_ID)
            .transacted()
            .log("Creating order...")
            .unmarshal().json(JsonLibrary.Jackson, OrderMessage.class)
            .to(mapstruct(target(Order.class)))
            .to(jpa(target(Order.class)));
    }
}

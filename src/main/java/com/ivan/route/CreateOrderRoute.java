package com.ivan.route;

import com.ivan.dto.OrderDto;
import com.ivan.entity.Order;
import org.apache.camel.builder.endpoint.EndpointRouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;

import static com.ivan.constants.RouteConstants.CREATE_ORDER_ROUTE;
import static com.ivan.constants.RouteConstants.CREATE_ORDER_ROUTE_ID;
import static com.ivan.util.ClassUtil.target;

public class CreateOrderRoute extends EndpointRouteBuilder {
    @Override
    public void configure() throws Exception {
        from(direct(CREATE_ORDER_ROUTE))
            .routeId(CREATE_ORDER_ROUTE_ID)
            .log("Creating order...")
            .unmarshal().json(JsonLibrary.Jackson, OrderDto.class)
            .to(mapstruct(target(Order.class)))
            .to(jpa(target(Order.class)))
            .marshal().json(JsonLibrary.Jackson, Order.class);
    }
}

package com.ivan.outbound.route;

import com.ivan.outbound.dto.OrderDto;
import com.ivan.outbound.entity.Order;
import com.ivan.outbound.processor.SetOrderIdProcessor;
import org.apache.camel.builder.endpoint.EndpointRouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;

import static com.ivan.outbound.constants.RouteConstants.UPDATE_ORDER_ROUTE;
import static com.ivan.outbound.constants.RouteConstants.UPDATE_ORDER_ROUTE_ID;
import static com.ivan.outbound.util.ClassUtil.target;

public class UpdateOrderRoute extends EndpointRouteBuilder {
    @Override
    public void configure() throws Exception {
        from(direct(UPDATE_ORDER_ROUTE))
            .routeId(UPDATE_ORDER_ROUTE_ID)
            .transacted()
            .log("Updating order...")
            .unmarshal().json(JsonLibrary.Jackson, OrderDto.class)
            .to(mapstruct(target(Order.class)))
            .bean(SetOrderIdProcessor.class)
            .to(jpa(target(Order.class)));
    }
}

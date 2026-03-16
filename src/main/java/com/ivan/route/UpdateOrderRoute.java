package com.ivan.route;

import com.ivan.dto.OrderDto;
import com.ivan.entity.Order;
import com.ivan.processor.SetOrderIdProcessor;
import org.apache.camel.builder.endpoint.EndpointRouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;

import static com.ivan.constants.RouteConstants.UPDATE_ORDER_ROUTE;
import static com.ivan.constants.RouteConstants.UPDATE_ORDER_ROUTE_ID;
import static com.ivan.util.ClassUtil.target;

public class UpdateOrderRoute extends EndpointRouteBuilder {
    @Override
    public void configure() throws Exception {
        from(direct(UPDATE_ORDER_ROUTE))
            .routeId(UPDATE_ORDER_ROUTE_ID)
                .log("Updating order...")
                .unmarshal().json(JsonLibrary.Jackson, OrderDto.class)
                .to(mapstruct(target(Order.class)))
                .bean(SetOrderIdProcessor.class)
                .to(jpa(target(Order.class)))
                .marshal().json(JsonLibrary.Jackson, Order.class);
    }
}

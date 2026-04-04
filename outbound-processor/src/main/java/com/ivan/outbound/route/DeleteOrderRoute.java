package com.ivan.outbound.route;

import com.ivan.outbound.entity.Order;
import com.ivan.outbound.processor.JpaParametersProcessor;
import org.apache.camel.builder.endpoint.EndpointRouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;

import static com.ivan.outbound.constants.JpaConstants.DELETE_BY_ID;
import static com.ivan.outbound.constants.RouteConstants.DELETE_ORDER_ROUTE;
import static com.ivan.outbound.constants.RouteConstants.DELETE_ORDER_ROUTE_ID;
import static com.ivan.outbound.util.ClassUtil.target;

public class DeleteOrderRoute extends EndpointRouteBuilder {
    @Override
    public void configure() {
        from(direct(DELETE_ORDER_ROUTE))
            .routeId(DELETE_ORDER_ROUTE_ID)
            .transacted()
            .log("Deleting order by id...")
            .bean(JpaParametersProcessor.class)
            .to(jpa(target(Order.class)).namedQuery(DELETE_BY_ID).useExecuteUpdate(true));
    }
}

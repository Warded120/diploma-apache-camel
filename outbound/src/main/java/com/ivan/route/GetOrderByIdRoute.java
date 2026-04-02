package com.ivan.route;

import com.ivan.entity.Order;
import com.ivan.processor.JpaParametersProcessor;
import org.apache.camel.builder.endpoint.EndpointRouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;

import static com.ivan.constants.JpaConstants.FIND_BY_ID;
import static com.ivan.constants.RouteConstants.GET_ORDER_BY_ID_ROUTE;
import static com.ivan.constants.RouteConstants.GET_ORDER_BY_ID_ROUTE_ID;
import static com.ivan.util.ClassUtil.target;

public class GetOrderByIdRoute extends EndpointRouteBuilder {
    @Override
    public void configure() throws Exception {
        from(direct(GET_ORDER_BY_ID_ROUTE))
            .routeId(GET_ORDER_BY_ID_ROUTE_ID)
            .log("Getting order by id...")
            .bean(JpaParametersProcessor.class)
            .to(jpa(target(Order.class)).namedQuery(FIND_BY_ID))
            .marshal().json(JsonLibrary.Jackson, Order.class);
    }
}

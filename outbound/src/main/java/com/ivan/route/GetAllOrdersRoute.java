package com.ivan.route;

import com.ivan.entity.Order;
import org.apache.camel.builder.endpoint.EndpointRouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;

import static com.ivan.constants.JpaConstants.FIND_ALL;
import static com.ivan.constants.RouteConstants.GET_ALL_ORDERS_ROUTE;
import static com.ivan.constants.RouteConstants.GET_ALL_ORDERS_ROUTE_ID;
import static com.ivan.util.ClassUtil.target;

public class GetAllOrdersRoute extends EndpointRouteBuilder {
    @Override
    public void configure() {
        from(direct(GET_ALL_ORDERS_ROUTE))
            .routeId(GET_ALL_ORDERS_ROUTE_ID)
            .log("Getting all orders...")
            .to(jpa(target(Order.class)).namedQuery(FIND_ALL))
            .marshal().json(JsonLibrary.Jackson, Order.class);

    }
}

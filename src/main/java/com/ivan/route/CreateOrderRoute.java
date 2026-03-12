package com.ivan.route;

import com.ivan.entity.Order;
import org.apache.camel.builder.endpoint.EndpointRouteBuilder;

import static com.ivan.constants.RouteConstants.CREATE_ORDER_ROUTE;
import static com.ivan.constants.RouteConstants.CREATE_ORDER_ROUTE_ID;

public class CreateOrderRoute extends EndpointRouteBuilder {
    @Override
    public void configure() throws Exception {
        from(direct(CREATE_ORDER_ROUTE))
            .routeId(CREATE_ORDER_ROUTE_ID)
            .setBody(simple("Creating order"))
            .log("called route: " + CREATE_ORDER_ROUTE)
            .to(jpa(target(Order.class))); //TODO: create persistence.xml, configure datasource and register EntityManagerFactory
    }

    private<T> String target(Class<T> clazz) {
        return clazz.getName();
    }
}

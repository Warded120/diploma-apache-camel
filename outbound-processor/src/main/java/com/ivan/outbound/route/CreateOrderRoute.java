package com.ivan.outbound.route;

import com.ivan.outbound.entity.Order;
import com.ivan.outbound.processor.EnricherProcessor;
import com.ivan.outbound.processor.ResolveCustomerProcessor;
import com.ivan.outbound.processor.ResolveProductProcessor;
import jakarta.persistence.EntityNotFoundException;
import org.apache.camel.BeanInject;
import org.apache.camel.builder.endpoint.EndpointRouteBuilder;

import static com.ivan.outbound.constants.RouteConstants.CREATE_ORDER_ROUTE;
import static com.ivan.outbound.constants.RouteConstants.CREATE_ORDER_ROUTE_ID;
import static com.ivan.outbound.constants.RouteConstants.CURRENCY_ENRICHER_ROUTE;
import static com.ivan.outbound.util.ClassUtil.target;

public class CreateOrderRoute extends EndpointRouteBuilder {

    @BeanInject
    private ResolveCustomerProcessor resolveCustomerProcessor;

    @BeanInject
    private ResolveProductProcessor resolveProductProcessor;

    @Override
    public void configure() {
        onException(EntityNotFoundException.class)
            .handled(true)
            .log("Order rejected with reason: ${exception.message}");

        from(direct(CREATE_ORDER_ROUTE))
            .routeId(CREATE_ORDER_ROUTE_ID)
            .transacted()
            .log("Creating order...")
            .process(resolveCustomerProcessor)
            .process(resolveProductProcessor)
            .to(direct(CURRENCY_ENRICHER_ROUTE))
            .to(mapstruct(target(Order.class)))
            .process(new EnricherProcessor())
            .to(jpa(target(Order.class)));
    }
}

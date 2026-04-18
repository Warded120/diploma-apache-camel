package com.ivan.outbound.route;

import com.ivan.outbound.entity.Order;
import com.ivan.outbound.processor.EnricherProcessor;
import com.ivan.outbound.processor.ResolveCustomerProcessor;
import com.ivan.outbound.processor.ResolveProductProcessor;
import com.ivan.outbound.processor.SetOrderIdProcessor;
import org.apache.camel.BeanInject;
import org.apache.camel.builder.endpoint.EndpointRouteBuilder;

import static com.ivan.outbound.constants.RouteConstants.UPDATE_ORDER_ROUTE;
import static com.ivan.outbound.constants.RouteConstants.UPDATE_ORDER_ROUTE_ID;
import static com.ivan.outbound.util.ClassUtil.target;

public class UpdateOrderRoute extends EndpointRouteBuilder {

    @BeanInject
    private ResolveCustomerProcessor resolveCustomerProcessor;

    @BeanInject
    private ResolveProductProcessor resolveProductProcessor;

    @Override
    public void configure() {
        from(direct(UPDATE_ORDER_ROUTE))
            .routeId(UPDATE_ORDER_ROUTE_ID)
            .transacted()
            .log("Updating order...")
            .process(resolveCustomerProcessor)
            .process(resolveProductProcessor)
            .to(mapstruct(target(Order.class)))
            .process(new EnricherProcessor())
            .bean(SetOrderIdProcessor.class)
            .to(jpa(target(Order.class)));
    }
}

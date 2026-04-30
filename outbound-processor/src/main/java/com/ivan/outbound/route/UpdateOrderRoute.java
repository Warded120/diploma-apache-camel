package com.ivan.outbound.route;

import com.ivan.outbound.entity.Order;
import com.ivan.outbound.exception.OrderMappingException;
import com.ivan.outbound.processor.AfterMappingProcessor;
import com.ivan.outbound.processor.ResolveCustomerProcessor;
import com.ivan.outbound.processor.ResolveProductProcessor;
import com.ivan.outbound.processor.SetOrderIdProcessor;
import com.ivan.outbound.processor.ValidateOrderExistsProcessor;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceException;
import org.apache.camel.BeanInject;
import org.apache.camel.Exchange;
import org.apache.camel.builder.endpoint.EndpointRouteBuilder;
import org.springframework.transaction.TransactionException;

import static com.ivan.outbound.constants.RouteConstants.CURRENCY_ENRICHER_ROUTE;
import static com.ivan.outbound.constants.RouteConstants.UPDATE_ORDER_ROUTE;
import static com.ivan.outbound.constants.RouteConstants.UPDATE_ORDER_ROUTE_ID;
import static com.ivan.outbound.util.ClassUtil.target;
import static org.apache.camel.LoggingLevel.ERROR;

public class UpdateOrderRoute extends EndpointRouteBuilder {

    @BeanInject
    private ValidateOrderExistsProcessor validateOrderExistsProcessor;

    @BeanInject
    private ResolveCustomerProcessor resolveCustomerProcessor;

    @BeanInject
    private ResolveProductProcessor resolveProductProcessor;

    @Override
    public void configure() {

        onException(EntityNotFoundException.class)
                .handled(true)
                .log("Update order rejected — referenced entity not found: ${exception.message}");

        onException(OrderMappingException.class)
                .handled(true)
                .log(ERROR, "Update order mapping failed, sending to dead-letter queue: ${exception.message}")
                .setHeader("error", simple("${exception.message}"))
                .setBody(simple("${exception.message}"))
                .to(kafka("{{kafka.dead-letter-topic}}"));

        onException(PersistenceException.class)
                .handled(true)
                .log(ERROR, "Update order database persistence failed, sending to dead-letter queue: ${exception.message}")
                .setHeader("error", simple("${exception.message}"))
                .setBody(simple("${exception.message}"))
                .to(kafka("{{kafka.dead-letter-topic}}"));

        onException(TransactionException.class)
                .handled(true)
                .log(ERROR, "Update order transaction failed, sending to dead-letter queue: ${exception.message}")
                .setHeader("error", simple("${exception.message}"))
                .setBody(simple("${exception.message}"))
                .to(kafka("{{kafka.dead-letter-topic}}"));

        from(direct(UPDATE_ORDER_ROUTE))
            .routeId(UPDATE_ORDER_ROUTE_ID)
            .transacted()
            .log("Updating order...")
            .process(validateOrderExistsProcessor)
            .process(resolveCustomerProcessor)
            .process(resolveProductProcessor)
            .to(direct(CURRENCY_ENRICHER_ROUTE))
            .doTry()
                .to(mapstruct(target(Order.class)))
            .doCatch(Exception.class)
                .process(exchange -> {
                    Exception cause = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
                    throw new OrderMappingException("Failed to map OrderMessage to Order entity", cause);
                })
            .end()
            .process(new AfterMappingProcessor())
            .bean(SetOrderIdProcessor.class)
            .to(jpa(target(Order.class)));
    }
}

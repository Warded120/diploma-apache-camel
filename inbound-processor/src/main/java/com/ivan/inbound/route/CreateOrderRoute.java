package com.ivan.inbound.route;

import com.ivan.avro.OrderMessage;
import com.ivan.inbound.dto.OrderDto;
import com.ivan.inbound.processor.ValidationErrorHandlerProcessor;
import org.apache.camel.builder.endpoint.EndpointRouteBuilder;
import org.apache.camel.component.bean.validator.BeanValidationException;
import org.apache.camel.model.dataformat.JsonLibrary;

import static com.ivan.inbound.constants.ExchangeConstants.HEADER_ACTION;
import static com.ivan.inbound.constants.RouteConstants.CREATE_ORDER_ROUTE;
import static com.ivan.inbound.constants.RouteConstants.CREATE_ORDER_ROUTE_ID;
import static com.ivan.inbound.constants.RouteConstants.ORDER_DTO_VALIDATOR;
import static com.ivan.inbound.enumeration.OrderAction.CREATE;
import static com.ivan.inbound.util.ClassUtil.target;

public class CreateOrderRoute extends EndpointRouteBuilder {

    @Override
    public void configure() {
        onException(BeanValidationException.class)
                .handled(true)
                .bean(ValidationErrorHandlerProcessor.class)
                .log("Create order request does not have valid body: ${header.ValidationError}")
                .setBody(simple("Create order request does not have valid body: ${header.ValidationError}"));

        onException(Exception.class)
                .handled(true)
                //TODO: handle it properly
                //[Camel (camel-1) thread #6 - NettyConsumerExecutorGroup] INFO  create-order-route-id - Exception occurred during order creation: Unrecognized field "name" (class com.ivan.inbound.dto.OrderDto), not marked as ignorable (7 known properties: "currency", "shippingAddress", "customerId", "productId", "quantity", "firstPurchase", "price"])
                // at [Source: REDACTED (`StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION` disabled); line: 11, column: 2] (through reference chain: com.ivan.inbound.dto.OrderDto["name"])
                .log("Exception occurred during order creation: ${exception.message}");

        from(direct(CREATE_ORDER_ROUTE))
            .routeId(CREATE_ORDER_ROUTE_ID)
            .unmarshal().json(JsonLibrary.Jackson, OrderDto.class)
            .to(beanValidator(ORDER_DTO_VALIDATOR))
            .to(mapstruct(target(OrderMessage.class)))
            .removeHeaders("*")
            .setHeader(HEADER_ACTION, constant(CREATE.getAction()))
            .to(kafka("{{kafka.topic}}"))
            .setBody(simple("Order request is created"))
            .log("Order creation request sent to Kafka");
    }
}

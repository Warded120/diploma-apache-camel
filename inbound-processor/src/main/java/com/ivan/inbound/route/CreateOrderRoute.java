package com.ivan.inbound.route;

import com.ivan.inbound.dto.OrderDto;
import com.ivan.inbound.message.OrderMessage;
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
                .log("Exception occurred during order creation: ${exception.message}");

        from(direct(CREATE_ORDER_ROUTE))
            .routeId(CREATE_ORDER_ROUTE_ID)
            .unmarshal().json(JsonLibrary.Jackson, OrderDto.class)
            .to(beanValidator(ORDER_DTO_VALIDATOR))
            .to(mapstruct(target(OrderMessage.class)))
            .marshal().json(JsonLibrary.Jackson)
            .removeHeaders("*")
            .setHeader(HEADER_ACTION, constant(CREATE.getAction()))
            .to(kafka("{{kafka.topic}}"))
            .setBody(simple("Order request is created"))
            .log("Order creation request sent to Kafka");
    }
}

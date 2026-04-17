package com.ivan.inbound.route;

import com.ivan.avro.OrderMessage;
import com.ivan.inbound.dto.OrderDto;
import com.ivan.inbound.processor.ValidationErrorHandlerProcessor;
import org.apache.camel.builder.endpoint.EndpointRouteBuilder;
import org.apache.camel.component.bean.validator.BeanValidationException;
import org.apache.camel.model.dataformat.JsonLibrary;

import static com.ivan.inbound.constants.ExchangeConstants.HEADER_ACTION;
import static com.ivan.inbound.constants.ExchangeConstants.HEADER_ID;
import static com.ivan.inbound.constants.RouteConstants.ORDER_DTO_VALIDATOR;
import static com.ivan.inbound.constants.RouteConstants.UPDATE_ORDER_ROUTE;
import static com.ivan.inbound.constants.RouteConstants.UPDATE_ORDER_ROUTE_ID;
import static com.ivan.inbound.enumeration.OrderAction.UPDATE;
import static com.ivan.inbound.util.ClassUtil.target;

public class UpdateOrderRoute extends EndpointRouteBuilder {
    @Override
    public void configure() {
        onException(BeanValidationException.class)
                .handled(true)
                .bean(ValidationErrorHandlerProcessor.class)
                .log("Update order request does not have valid body: ${header.ValidationError}")
                .setBody(simple("Update order request does not have valid body: ${header.ValidationError}"));

        onException(Exception.class)
                .handled(true)
                .log("Exception occurred during order update: ${exception.message}");

        from(direct(UPDATE_ORDER_ROUTE))
            .routeId(UPDATE_ORDER_ROUTE_ID)
                .unmarshal().json(JsonLibrary.Jackson, OrderDto.class)
                .to(beanValidator(ORDER_DTO_VALIDATOR))
                .to(mapstruct(target(OrderMessage.class)))
                .removeHeaders("*", HEADER_ID)
                .setHeader(HEADER_ACTION, constant(UPDATE.getAction()))
                .to(kafka("{{kafka.topic}}"))
                .setBody(simple("Order update request is created"))
                .log("Order update request sent to Kafka");
    }
}

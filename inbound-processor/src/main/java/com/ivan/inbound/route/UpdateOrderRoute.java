package com.ivan.inbound.route;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.ivan.avro.OrderMessage;
import com.ivan.inbound.dto.OrderDto;
import com.ivan.inbound.exception.InvalidOrderIdException;
import com.ivan.inbound.exception.OrderMappingException;
import com.ivan.inbound.processor.ValidateOrderIdProcessor;
import com.ivan.inbound.processor.ValidationErrorHandlerProcessor;
import org.apache.camel.Exchange;
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
import static org.apache.camel.LoggingLevel.ERROR;

public class UpdateOrderRoute extends EndpointRouteBuilder {

    @Override
    public void configure() {

        onException(InvalidOrderIdException.class)
                .handled(true)
                .log("Update order request has invalid id: ${exception.message}")
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(400))
                .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
                .setBody(simple("{\"error\":\"${exception.message}\"}"));

        onException(BeanValidationException.class)
                .handled(true)
                .bean(ValidationErrorHandlerProcessor.class)
                .log("Update order request failed validation: ${header.ValidationError}")
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(400))
                .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
                .setBody(simple("{\"error\":\"Validation failed\",\"details\":\"${header.ValidationError}\"}"));

        onException(JsonMappingException.class)
                .handled(true)
                .log("Update order request has invalid body structure: ${exception.message}")
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(400))
                .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
                .setBody(constant("{\"error\":\"Invalid request body structure\"}"));

        onException(JsonParseException.class)
                .handled(true)
                .log("Update order request contains malformed JSON: ${exception.message}")
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(400))
                .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
                .setBody(constant("{\"error\":\"Malformed JSON\"}"));

        onException(OrderMappingException.class)
                .handled(true)
                .log(ERROR, "Failed to map update order DTO to Avro message: ${exception.message}")
                .setHeader("error", simple("${exception.message}"))
                .setBody(simple("${exception.message}"))
                .to(kafka("{{kafka.dead-letter-topic}}"))
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(500))
                .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
                .setBody(constant("{\"error\":\"Failed to process order, it has been sent to dead-letter queue\"}"));

        from(direct(UPDATE_ORDER_ROUTE))
            .routeId(UPDATE_ORDER_ROUTE_ID)
            .process(new ValidateOrderIdProcessor())
            .unmarshal().json(JsonLibrary.Jackson, OrderDto.class)
            .to(beanValidator(ORDER_DTO_VALIDATOR))
            .doTry()
                .to(mapstruct(target(OrderMessage.class)))
            .doCatch(Exception.class)
                .process(exchange -> {
                    Exception cause = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
                    throw new OrderMappingException("Failed to map order DTO to Avro message", cause);
                })
            .end()
            .removeHeaders("*", HEADER_ID)
            .setHeader(HEADER_ACTION, constant(UPDATE.getAction()))
            .to(kafka("{{kafka.topic}}"))
            .setBody(simple("Order update request is created"))
            .log("Order update request sent to Kafka");
    }
}

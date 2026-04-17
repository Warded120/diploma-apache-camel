package com.ivan.inbound.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.bean.validator.BeanValidationException;
import java.util.stream.Collectors;

import static com.ivan.inbound.constants.ExchangeConstants.HEADER_VALIDATION_ERROR;

public class ValidationErrorHandlerProcessor implements Processor {

    @Override
    public void process(Exchange exchange) {
        BeanValidationException cause = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, BeanValidationException.class);

        String errors = cause.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + " " + v.getMessage())
                .collect(Collectors.joining(", "));

        exchange.getIn().setHeader(HEADER_VALIDATION_ERROR, errors);
    }
}

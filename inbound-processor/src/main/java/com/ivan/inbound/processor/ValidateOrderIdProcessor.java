package com.ivan.inbound.processor;

import com.ivan.inbound.exception.InvalidOrderIdException;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import static com.ivan.inbound.constants.ExchangeConstants.HEADER_ID;

public class ValidateOrderIdProcessor implements Processor {

    @Override
    public void process(Exchange exchange) {
        String id = exchange.getIn().getHeader(HEADER_ID, String.class);
        if (id == null || id.isBlank()) {
            throw new InvalidOrderIdException("Order id is required");
        }
        try {
            long parsed = Long.parseLong(id);
            if (parsed <= 0) {
                throw new InvalidOrderIdException("Order id must be a positive number, got: " + id);
            }
        } catch (NumberFormatException e) {
            throw new InvalidOrderIdException("Order id must be a valid number, got: " + id);
        }
    }
}


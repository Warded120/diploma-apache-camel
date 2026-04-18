package com.ivan.outbound.processor;

import com.ivan.avro.OrderMessage;
import com.ivan.outbound.repository.CustomerRepo;
import lombok.RequiredArgsConstructor;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import static com.ivan.outbound.constants.ExchangeConstants.PROP_CUSTOMER;

@RequiredArgsConstructor
public class ResolveCustomerProcessor implements Processor {

    private final CustomerRepo customerRepo;

    @Override
    public void process(Exchange exchange) {
        var orderMessage = exchange.getIn().getBody(OrderMessage.class);
        exchange.setProperty(PROP_CUSTOMER, customerRepo.findById(orderMessage.getCid()));
    }
}


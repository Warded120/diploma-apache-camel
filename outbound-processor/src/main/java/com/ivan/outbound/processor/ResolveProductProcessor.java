package com.ivan.outbound.processor;

import com.ivan.avro.OrderMessage;
import com.ivan.outbound.repository.ProductRepo;
import lombok.RequiredArgsConstructor;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import static com.ivan.outbound.constants.ExchangeConstants.PROP_PRODUCT;

@RequiredArgsConstructor
public class ResolveProductProcessor implements Processor {

    private final ProductRepo productRepo;

    @Override
    public void process(Exchange exchange) {
        var orderMessage = exchange.getIn().getBody(OrderMessage.class);
        exchange.setProperty(PROP_PRODUCT, productRepo.findById(orderMessage.getPid()));
    }
}


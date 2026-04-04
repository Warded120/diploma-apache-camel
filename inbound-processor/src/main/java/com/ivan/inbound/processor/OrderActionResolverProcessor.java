package com.ivan.inbound.processor;

import com.ivan.inbound.enumeration.OrderAction;
import lombok.RequiredArgsConstructor;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import java.util.Optional;

import static com.ivan.inbound.constants.ExchangeConstants.HEADER_ACTION;

@RequiredArgsConstructor
public class OrderActionResolverProcessor implements Processor {

    private final OrderAction action;
    @Override
    public void process(Exchange exchange) {
        Optional.of(exchange)
                .map(Exchange::getIn)
                .ifPresent(message ->
                        message.setHeader(HEADER_ACTION, action.getAction())
                );
    }
}

package com.ivan.outbound.processor;

import com.ivan.outbound.repository.OrderRepo;
import lombok.RequiredArgsConstructor;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import static com.ivan.outbound.constants.JpaConstants.JPA_PARAMETER_ID;

@RequiredArgsConstructor
public class ValidateOrderExistsProcessor implements Processor {

    private final OrderRepo orderRepo;

    @Override
    public void process(Exchange exchange) {
        var id = exchange.getIn().getHeader(JPA_PARAMETER_ID, Long.class);
        orderRepo.findById(id);
    }
}


package com.ivan.outbound.processor;

import com.ivan.outbound.entity.Order;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import java.util.Optional;

import static com.ivan.outbound.constants.JpaConstants.JPA_PARAMETER_ID;

public class SetOrderIdProcessor implements Processor {
    @Override
    public void process(Exchange exchange) throws Exception {
        Optional.of(exchange)
                .map(Exchange::getIn)
                .ifPresent(SetOrderIdProcessor::setOrderId);
    }

    private static void setOrderId(Message in) {
        var id = in.getHeader(JPA_PARAMETER_ID, Long.class);
        var order = in.getBody(Order.class);
        order.setId(id);
    }
}

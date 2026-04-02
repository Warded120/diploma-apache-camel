package com.ivan.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import java.util.Map;
import java.util.Optional;

import static com.ivan.constants.JpaConstants.JPA_PARAMETERS;
import static com.ivan.constants.JpaConstants.JPA_PARAMETER_ID;

public class JpaParametersProcessor implements Processor {
    @Override
    public void process(Exchange exchange) throws Exception {
        Optional.of(exchange)
                .map(Exchange::getIn)
                .ifPresent(JpaParametersProcessor::setJpaParameters);
    }

    private static void setJpaParameters(Message in) {
        if (in.getHeader(JPA_PARAMETER_ID) != null) {
            in.setHeader(JPA_PARAMETERS, Map.of(JPA_PARAMETER_ID, in.getHeader(JPA_PARAMETER_ID)));
        }
    }
}

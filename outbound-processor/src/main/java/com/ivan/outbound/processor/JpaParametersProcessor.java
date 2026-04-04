package com.ivan.outbound.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.ivan.outbound.constants.JpaConstants.JPA_PARAMETERS;
import static com.ivan.outbound.constants.JpaConstants.JPA_PARAMETER_ID;

public class JpaParametersProcessor implements Processor {
    @Override
    public void process(Exchange exchange) throws Exception {
        Optional.of(exchange)
                .map(Exchange::getIn)
                .ifPresent(JpaParametersProcessor::setJpaParameters);
    }

    private static void setJpaParameters(Message in) {
        var jpaParams = new HashMap<String, Object>();
        var idParameter = in.getHeader(JPA_PARAMETER_ID, Long.class);
        if (idParameter != null) {
            jpaParams.put(JPA_PARAMETER_ID, idParameter);
        }
        in.setHeader(JPA_PARAMETERS, jpaParams);
    }
}

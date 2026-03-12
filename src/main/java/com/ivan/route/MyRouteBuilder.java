package com.ivan.route;

import com.ivan.processor.MyProcessor;
import org.apache.camel.PropertyInject;
import org.apache.camel.builder.endpoint.EndpointRouteBuilder;

public class MyRouteBuilder extends EndpointRouteBuilder {

    @PropertyInject("myPeriod")
    private String myPeriod;

    @Override
    public void configure() {
        from(timer("myTimer").period(myPeriod))
                .bean(MyProcessor.class)
                .log("Processed message: ${body}");
    }
}

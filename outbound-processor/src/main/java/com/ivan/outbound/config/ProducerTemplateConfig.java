package com.ivan.outbound.config;

import org.apache.camel.BindToRegistry;
import org.apache.camel.CamelContext;
import org.apache.camel.Configuration;
import org.apache.camel.ProducerTemplate;

@Configuration
public class ProducerTemplateConfig {

    @BindToRegistry("producerTemplate")
    public ProducerTemplate producerTemplate(CamelContext context) {
        return context.createProducerTemplate();
    }
}

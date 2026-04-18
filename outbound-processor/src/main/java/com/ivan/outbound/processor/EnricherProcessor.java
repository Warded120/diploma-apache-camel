package com.ivan.outbound.processor;

import com.ivan.outbound.entity.Customer;
import com.ivan.outbound.entity.Order;
import com.ivan.outbound.entity.Product;
import org.apache.camel.Exchange;

import java.util.Optional;

import static com.ivan.outbound.constants.ExchangeConstants.PROP_CUSTOMER;
import static com.ivan.outbound.constants.ExchangeConstants.PROP_PRODUCT;

public class EnricherProcessor implements org.apache.camel.Processor {
    @Override
    public void process(Exchange exchange) throws Exception {
        var order = Optional.of(exchange)
                .map(Exchange::getIn)
                .map(message -> message.getBody(Order.class))
                .orElseThrow(() -> new Exception("Order not found"));

        order.setCustomer(getCustomer(exchange));
        order.setProduct(getProduct(exchange));
    }

    public Customer getCustomer(Exchange exchange) {
        return exchange.getProperty(PROP_CUSTOMER, Customer.class);
    }

    public Product getProduct(Exchange exchange) {
        return exchange.getProperty(PROP_PRODUCT, Product.class);
    }
}

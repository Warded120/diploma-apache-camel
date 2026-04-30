package com.ivan.outbound.processor;

import com.ivan.outbound.entity.Customer;
import com.ivan.outbound.entity.Order;
import com.ivan.outbound.entity.Product;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import java.util.Optional;

import static com.ivan.outbound.constants.ExchangeConstants.HEADER_PRICE_USD;
import static com.ivan.outbound.constants.ExchangeConstants.PROP_CUSTOMER;
import static com.ivan.outbound.constants.ExchangeConstants.PROP_PRODUCT;

public class AfterMappingProcessor implements Processor {
    @Override
    public void process(Exchange exchange) throws Exception {
        var order = Optional.of(exchange)
                .map(Exchange::getIn)
                .map(message -> message.getBody(Order.class))
                .orElseThrow(() -> new Exception("Order not found"));

        var customer = getCustomer(exchange);
        order.setCustomer(customer);
        order.setProduct(getProduct(exchange));
        order.setPriceUsd(exchange.getIn().getHeader(HEADER_PRICE_USD, Double.class));
        if (order.getShippingAddress() == null && customer != null) {
            order.setShippingAddress(customer.getShippingAddress());
        }
    }

    public Customer getCustomer(Exchange exchange) {
        return exchange.getProperty(PROP_CUSTOMER, Customer.class);
    }

    public Product getProduct(Exchange exchange) {
        return exchange.getProperty(PROP_PRODUCT, Product.class);
    }
}

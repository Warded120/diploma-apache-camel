package com.ivan.outbound.aggregation;

import com.ivan.outbound.entity.Order;
import org.apache.camel.AggregationStrategy;
import org.apache.camel.Exchange;

public class CurrencyAggregationStrategy implements AggregationStrategy {

    @Override
    public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
        var priceUsd = oldExchange.getIn().getBody(Double.class);
        var order = newExchange.getIn().getBody(Order.class);

        order.setPriceUsd(priceUsd);

        return oldExchange;
    }
}

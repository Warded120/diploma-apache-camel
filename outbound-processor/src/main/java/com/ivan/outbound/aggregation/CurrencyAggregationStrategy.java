package com.ivan.outbound.aggregation;

import com.ivan.avro.OrderMessage;
import com.ivan.outbound.dto.ExchangeRateResponse;
import org.apache.camel.AggregationStrategy;
import org.apache.camel.Exchange;

import static com.ivan.outbound.constants.ExchangeConstants.DISCOUNT;
import static com.ivan.outbound.constants.ExchangeConstants.HEADER_PRICE_USD;

/**
 * Merges the original exchange (body = {@link OrderMessage}) with the
 * enrichment exchange (body = {@link ExchangeRateResponse}).
 * <p>
 * Computes the USD price (applying the first-purchase discount when applicable),
 * sets {@code HEADER_PRICE_USD} on the original exchange, and returns it
 * with the {@link OrderMessage} still as the body.
 */
public class CurrencyAggregationStrategy implements AggregationStrategy {

    @Override
    public Exchange aggregate(Exchange originalExchange, Exchange enrichmentExchange) {
        OrderMessage orderMessage = originalExchange.getIn().getBody(OrderMessage.class);
        ExchangeRateResponse rateResponse = enrichmentExchange.getIn().getBody(ExchangeRateResponse.class);

        double usdRate = rateResponse.getRates().getOrDefault("USD", 1.0);
        double basePrice = orderMessage.getPr();
        boolean firstPurchase = orderMessage.getFp();

        double discountedPrice = firstPurchase ? basePrice * (1 - DISCOUNT) : basePrice;
        double priceUsd = discountedPrice * usdRate;

        originalExchange.getIn().setHeader(HEADER_PRICE_USD, priceUsd);
        return originalExchange;
    }
}

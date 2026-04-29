package com.ivan.outbound.processor;

import com.ivan.avro.OrderMessage;
import com.ivan.outbound.dto.ExchangeRateResponse;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import static com.ivan.outbound.constants.ExchangeConstants.DISCOUNT;
import static com.ivan.outbound.constants.ExchangeConstants.HEADER_PRICE_USD;
import static com.ivan.outbound.constants.ExchangeConstants.PROP_ORDER_MESSAGE;

/**
 * Executed after the JCachePolicy block in {@code CurrencyEnricherRoute}.
 * <p>
 * At this point the exchange body holds an {@link ExchangeRateResponse}
 * (either freshly fetched or restored from cache). The original
 * {@link OrderMessage} was saved in exchange property {@code PROP_ORDER_MESSAGE}
 * before the HTTP call so it is always available here.
 * <p>
 * The processor:
 * <ol>
 *   <li>Reads the USD rate from the response.</li>
 *   <li>Applies the first-purchase discount to the base price.</li>
 *   <li>Multiplies by the USD rate and sets {@code HEADER_PRICE_USD}.</li>
 *   <li>Restores the {@link OrderMessage} as the exchange body.</li>
 * </ol>
 */
public class CurrencyPriceCalculatorProcessor implements Processor {

    @Override
    public void process(Exchange exchange) {
        ExchangeRateResponse rateResponse = exchange.getIn().getBody(ExchangeRateResponse.class);
        OrderMessage orderMessage = exchange.getProperty(PROP_ORDER_MESSAGE, OrderMessage.class);

        double usdRate = rateResponse.getRates().getOrDefault("USD", 1.0);
        double basePrice = orderMessage.getPr();
        boolean firstPurchase = orderMessage.getFp();

        double discountedPrice = firstPurchase ? basePrice * (1 - DISCOUNT) : basePrice;
        double priceUsd = discountedPrice * usdRate;

        exchange.getIn().setHeader(HEADER_PRICE_USD, priceUsd);
        exchange.getIn().setBody(orderMessage);
    }
}


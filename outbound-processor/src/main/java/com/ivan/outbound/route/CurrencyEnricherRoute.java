package com.ivan.outbound.route;

import com.ivan.outbound.dto.ExchangeRateResponse;
import com.ivan.outbound.processor.CurrencyPriceCalculatorProcessor;
import org.apache.camel.builder.endpoint.EndpointRouteBuilder;
import org.apache.camel.component.jcache.policy.JCachePolicy;

import javax.cache.Cache;
import javax.cache.CacheException;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.CreatedExpiryPolicy;
import javax.cache.expiry.Duration;
import javax.cache.spi.CachingProvider;
import java.util.concurrent.TimeUnit;

import static com.ivan.outbound.constants.ExchangeConstants.HEADER_BASE_CURRENCY;
import static com.ivan.outbound.constants.ExchangeConstants.PROP_ORDER_MESSAGE;
import static com.ivan.outbound.constants.RouteConstants.CURRENCY_ENRICHER_ROUTE;
import static com.ivan.outbound.constants.RouteConstants.CURRENCY_ENRICHER_ROUTE_ID;

public class CurrencyEnricherRoute extends EndpointRouteBuilder {

    private static final String CACHE_NAME = "currencyRatesCache";

    @Override
    public void configure() {
        JCachePolicy cachePolicy = buildCachePolicy();

        from(direct(CURRENCY_ENRICHER_ROUTE))
            .routeId(CURRENCY_ENRICHER_ROUTE_ID)
            .setProperty(PROP_ORDER_MESSAGE, body())
            .setHeader(HEADER_BASE_CURRENCY, simple("${body.cur}"))
            .log("Fetching exchange rates for currency: ${header." + HEADER_BASE_CURRENCY + "}")
            .policy(cachePolicy)
                .setBody(constant(null))
                .toD("{{api.currency.base-url}}/v4/latest/${header." + HEADER_BASE_CURRENCY + "}")
                .process(exchange ->
                        System.out.println("debug"))
                .unmarshal().json(ExchangeRateResponse.class)
            .end()
            .process(new CurrencyPriceCalculatorProcessor())
            .log("Computed priceUsd=${header." + "priceUsd" + "} for currency ${header." + HEADER_BASE_CURRENCY + "}");
    }

    private JCachePolicy buildCachePolicy() {
        CachingProvider provider = Caching.getCachingProvider();
        CacheManager cacheManager = provider.getCacheManager();

        MutableConfiguration<Object, Object> config = new MutableConfiguration<Object, Object>()
            .setExpiryPolicyFactory(
                CreatedExpiryPolicy.factoryOf(new Duration(TimeUnit.HOURS, 1)))
            .setStoreByValue(false);

        Cache<Object, Object> cache;
        try {
            cache = cacheManager.createCache(CACHE_NAME, config);
        } catch (CacheException e) {
            cache = cacheManager.getCache(CACHE_NAME);
        }

        JCachePolicy policy = new JCachePolicy();
        policy.setCache(cache);
        policy.setKeyExpression(simple("${header." + HEADER_BASE_CURRENCY + "}"));
        return policy;
    }
}

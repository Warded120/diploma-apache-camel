package com.ivan.outbound.config;

import org.apache.camel.BindToRegistry;
import org.apache.camel.Configuration;
import org.apache.camel.component.jcache.policy.JCachePolicy;
import org.apache.camel.language.simple.SimpleLanguage;

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
import static org.apache.camel.builder.Builder.simple;

@Configuration
public class CacheConfig {

    private static final String CACHE_NAME = "currencyRatesCache";

    @BindToRegistry("currencyRatesCachePolicy")
    public JCachePolicy currencyRatesCachePolicy() {
        CachingProvider provider = Caching.getCachingProvider();
        CacheManager cacheManager = provider.getCacheManager();

        MutableConfiguration<Object, Object> config = new MutableConfiguration<>()
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




package com.ivan.outbound.route;

import com.ivan.outbound.dto.ExchangeRateResponse;
import org.apache.camel.BeanInject;
import org.apache.camel.builder.endpoint.EndpointRouteBuilder;
import org.apache.camel.component.jcache.policy.JCachePolicy;

import static com.ivan.outbound.constants.ExchangeConstants.HEADER_BASE_CURRENCY;
import static com.ivan.outbound.constants.RouteConstants.CURRENCY_API_ROUTE;
import static com.ivan.outbound.constants.RouteConstants.CURRENCY_API_ROUTE_ID;

public class CurrencyApiRoute extends EndpointRouteBuilder {

    @BeanInject
    private JCachePolicy cachePolicy;

    @Override
    public void configure() throws Exception {
        from(direct(CURRENCY_API_ROUTE))
                .routeId(CURRENCY_API_ROUTE_ID)
                .policy(cachePolicy)
                    .setBody(constant(null))
                    .log("Calling currency api and caching the result")
                    .toD("{{api.currency.base-url}}/v4/latest/${header." + HEADER_BASE_CURRENCY + "}")
                    .unmarshal().json(ExchangeRateResponse.class)
                .end();
    }
}

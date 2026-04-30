package com.ivan.outbound.route;

import com.ivan.outbound.aggregation.CurrencyAggregationStrategy;
import org.apache.camel.builder.endpoint.EndpointRouteBuilder;

import static com.ivan.outbound.constants.ExchangeConstants.HEADER_BASE_CURRENCY;
import static com.ivan.outbound.constants.RouteConstants.CURRENCY_ENRICHER_ROUTE;
import static com.ivan.outbound.constants.RouteConstants.CURRENCY_ENRICHER_ROUTE_ID;
import static com.ivan.outbound.constants.RouteConstants.CURRENCY_API_ROUTE;

public class CurrencyEnricherRoute extends EndpointRouteBuilder {

    @Override
    public void configure() {
        from(direct(CURRENCY_ENRICHER_ROUTE))
            .routeId(CURRENCY_ENRICHER_ROUTE_ID)
            .setHeader(HEADER_BASE_CURRENCY, simple("${body.cur}"))
            .log("Fetching exchange rates for currency: ${header." + HEADER_BASE_CURRENCY + "}")
            .enrich(direct(CURRENCY_API_ROUTE), new CurrencyAggregationStrategy());
    }
}

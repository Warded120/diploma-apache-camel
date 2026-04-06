package com.ivan.inbound.processor;

import com.ivan.inbound.enumeration.OrderAction;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.support.DefaultExchange;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static com.ivan.inbound.constants.ExchangeConstants.HEADER_ACTION;
import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OrderActionResolverProcessorTest {
    private OrderActionResolverProcessor processor;
    private CamelContext context;
    private Exchange exchange;

    @BeforeAll
    void beforeAll() {
        context = new DefaultCamelContext();
    }

    @BeforeEach
    void beforeEach() {
        exchange = new DefaultExchange(context);
    }

    @ParameterizedTest
    @MethodSource("actionsProvider")
    void processShouldSetHeader(OrderAction action) {
        processor = new OrderActionResolverProcessor(action);

        processor.process(exchange);

        assertEquals(action.getAction(), exchange.getIn().getHeader(HEADER_ACTION, String.class));
    }

    public Stream<Arguments> actionsProvider() {
        return Stream.of(
                Arguments.of(OrderAction.CREATE),
                Arguments.of(OrderAction.UPDATE),
                Arguments.of(OrderAction.DELETE)
        );
    }
}
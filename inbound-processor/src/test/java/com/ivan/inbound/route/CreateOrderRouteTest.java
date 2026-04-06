package com.ivan.inbound.route;

import org.apache.camel.CamelContext;
import org.apache.camel.EndpointInject;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.model.OnExceptionDefinition;
import org.apache.camel.spi.PropertiesComponent;
import org.apache.camel.test.junit5.CamelTestSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.ivan.inbound.constants.RouteConstants.CREATE_ORDER_ROUTE_ID;
import static com.ivan.inbound.testdata.OrderDtoTestData.getOrderDtoJson;
import static com.ivan.inbound.testdata.OrderTestData.getOrder;
import static com.ivan.inbound.testdata.OrderTestData.getOrderJson;
import static org.apache.camel.builder.AdviceWith.adviceWith;

class CreateOrderRouteTest extends CamelTestSupport {

    public static final String DIRECT_START = "direct:start";
    public static final String KAFKA_PATTERN = "kafka:*";
    private static final String MAPSTRUCT_PATTERN = "mapstruct:*";

    @EndpointInject("mock:kafka")
    private MockEndpoint mockKafka;

    @EndpointInject("mock:exception")
    private MockEndpoint mockException;

    @EndpointInject("mock:mapstruct")
    private MockEndpoint mockMapstruct;

    public CreateOrderRouteTest() {
        super();
        this.testConfigurationBuilder.withUseAdviceWith(true);
    }

    @Override
    protected CamelContext createCamelContext() throws Exception {
        var camelContext = super.createCamelContext();
        var propertiesComponent = camelContext.getPropertiesComponent();
        propertiesComponent.setLocation("classpath:application.properties");

        return camelContext;
    }

    @BeforeEach
    void preConditions() throws Exception {
        var route = new CreateOrderRoute();
        context.addRoutes(route);

        adviceWith(
                context,
                CREATE_ORDER_ROUTE_ID,
                routeBuilder -> {
                    routeBuilder.replaceFromWith(DIRECT_START);
                    routeBuilder.weaveByToUri(MAPSTRUCT_PATTERN).replace().to(mockMapstruct);
                    routeBuilder.weaveByToUri(KAFKA_PATTERN).replace().to(mockKafka);
                }
        );

        context.start();
    }

    @AfterEach
    void tearDown() {
        context.stop();
    }

    @Test
    void configureShouldSendRequestToKafka() throws InterruptedException {
        mockKafka.expectedMessageCount(1);
        mockKafka.expectedBodiesReceived(getOrderJson());
        mockMapstruct.whenAnyExchangeReceived(exchange -> exchange.getMessage().setBody(getOrder()));
        mockException.expectedMessageCount(0);

        template.sendBody(DIRECT_START, getOrderDtoJson());

        mockKafka.assertIsSatisfied();
        mockException.assertIsSatisfied();
    }
}
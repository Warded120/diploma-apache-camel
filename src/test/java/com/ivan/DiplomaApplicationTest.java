package com.ivan;

import java.util.concurrent.TimeUnit;

import org.apache.camel.builder.NotifyBuilder;
import org.apache.camel.test.main.junit5.CamelMainTestSupport;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A simple unit test showing how to test the application {@link DiplomaApplication}.
 */
class DiplomaApplicationTest extends CamelMainTestSupport {

    @Override
    protected Class<?> getMainClass() {
        // The main class of the application to test
        return DiplomaApplication.class;
    }

    @Test
    void should_complete_the_auto_detected_route() {
        NotifyBuilder notify = new NotifyBuilder(context)
                .whenCompleted(1).whenBodiesDone("Goodbye World").create();
        assertTrue(
                notify.matches(20, TimeUnit.SECONDS), "1 message should be completed"
        );
    }
}

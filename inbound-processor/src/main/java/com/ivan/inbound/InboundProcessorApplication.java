package com.ivan.inbound;

import lombok.NoArgsConstructor;
import org.apache.camel.main.Main;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class InboundProcessorApplication {

    public static void main(String[] args) throws Exception {
        Main main = new Main(InboundProcessorApplication.class);
        main.run(args);
    }
}

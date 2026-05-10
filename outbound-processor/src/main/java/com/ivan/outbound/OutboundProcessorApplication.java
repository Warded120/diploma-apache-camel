package com.ivan.outbound;

import lombok.NoArgsConstructor;
import org.apache.camel.main.Main;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class OutboundProcessorApplication {

    public static void main(String[] args) throws Exception {
        System.setProperty("vertx.cacheDirBase",
                System.getProperty("user.home") + "/.vertx");

        Main main = new Main(OutboundProcessorApplication.class);
        main.run(args);
    }
}

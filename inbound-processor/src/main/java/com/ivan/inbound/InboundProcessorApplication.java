package com.ivan.inbound;

import lombok.NoArgsConstructor;
import org.apache.camel.main.Main;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class InboundProcessorApplication {

    public static void main(String[] args) throws Exception {
        // Vert.x (used by camel-platform-http-vertx) derives its cache dir from
        // java.io.tmpdir, which on some Windows environments resolves to C:\Windows
        // and is not writable without admin rights. Override explicitly.
        System.setProperty("vertx.cacheDirBase",
                System.getProperty("user.home") + "/.vertx");

        Main main = new Main(InboundProcessorApplication.class);
        main.run(args);
    }
}

package com.ivan;

import org.apache.camel.main.Main;

/**
 * Main class that boot the Camel application
 */
public final class DiplomaApplication {

    private DiplomaApplication() {
    }

    public static void main(String[] args) throws Exception {
        Main main = new Main(DiplomaApplication.class);
        main.run(args);
    }

}

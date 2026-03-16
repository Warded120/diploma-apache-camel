package com.ivan;

import lombok.NoArgsConstructor;
import org.apache.camel.main.Main;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class DiplomaApplication {

    //TODO: add tests
    public static void main(String[] args) throws Exception {
        Main main = new Main(DiplomaApplication.class);
        main.run(args);
    }

}

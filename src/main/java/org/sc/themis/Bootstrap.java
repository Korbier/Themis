package org.sc.themis;

import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;

@QuarkusMain
public class Bootstrap implements QuarkusApplication {

    @Override
    public int run(String... args) {

        System.out.println( "Plop sdfsdf plop !");

        return 0;

    }

}

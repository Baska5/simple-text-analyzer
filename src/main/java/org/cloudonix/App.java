package org.cloudonix;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;

public class App extends AbstractVerticle {

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        ServiceVert v = new ServiceVert();
        vertx.deployVerticle(v);
    }

}
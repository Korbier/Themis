package org.sc.themis.engine;

import org.sc.themis.gamestate.Gamestate;
import org.sc.themis.input.Input;
import org.sc.themis.scene.Scene;

public class EngineTestGamestate implements Gamestate {

    private final Engine engine;
    private int counter;

    public EngineTestGamestate( Engine engine, int counter ) {
        this.engine = engine;
        this.counter = counter;
    }

    @Override
    public void setup(Scene scene) {

    }

    @Override
    public void cleanup( Scene scene ) {

    }

    @Override
    public void input(Scene scene, Input input, long tpf) {

    }

    @Override
    public void update(Scene scene, long tpf) {

        this.counter--;

        if ( this.counter <= 0 ) {
            this.engine.stop();
        }

    }

}

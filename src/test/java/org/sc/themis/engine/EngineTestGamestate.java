package org.sc.themis.engine;

import org.sc.themis.gamestate.Gamestate;
import org.sc.themis.input.Input;
import org.sc.themis.renderer.Renderer;
import org.sc.themis.scene.Scene;
import org.sc.themis.shared.exception.ThemisException;

public class EngineTestGamestate implements Gamestate {

    private final Engine engine;
    private final Gamestate inner;
    private int counter;

    public EngineTestGamestate( Engine engine, int counter ) {
        this( engine, null, counter );
    }

    public EngineTestGamestate( Engine engine, Gamestate inner, int counter ) {
        this.engine = engine;
        this.counter = counter;
        this.inner = inner;
    }

    @Override
    public void setup(Renderer renderer, Scene scene) throws ThemisException {
        if ( this.inner != null ) {
            this.inner.setup( renderer, scene );
        }
    }

    @Override
    public void cleanup( Renderer renderer, Scene scene ) throws ThemisException {
        if ( this.inner != null ) {
            this.inner.cleanup( renderer, scene );
        }
    }

    @Override
    public void input(Scene scene, Input input, long tpf) {
        if ( this.inner != null ) {
            this.inner.input( scene, input, tpf );
        }
    }

    @Override
    public void update(Scene scene, long tpf) {

        if ( this.inner != null ) {
            this.inner.update( scene, tpf );
        }

        this.counter--;

        if ( this.counter <= 0 ) {
            this.engine.stop();
        }

    }

}

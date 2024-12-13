package org.sc.playground.noop;

import org.sc.themis.gamestate.Gamestate;
import org.sc.themis.input.Input;
import org.sc.themis.renderer.Renderer;
import org.sc.themis.scene.Scene;
import org.sc.themis.shared.exception.ThemisException;

public class NoopGamestate implements Gamestate {

    @Override
    public void setup(Renderer renderer, Scene scene) {

    }

    @Override
    public void cleanup(Renderer renderer, Scene scene) throws ThemisException {

    }

    @Override
    public void input(Scene scene, Input input, long tpf) {

    }

    @Override
    public void update(Scene scene, long tpf) {

    }

}

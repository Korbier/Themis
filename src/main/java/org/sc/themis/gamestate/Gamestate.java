package org.sc.themis.gamestate;

import org.sc.themis.input.Input;
import org.sc.themis.scene.Scene;

public interface Gamestate {

    void input(Scene scene, Input input, long tpf);
    void update( Scene scene, long tpf );

}

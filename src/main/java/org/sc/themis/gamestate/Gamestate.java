package org.sc.themis.gamestate;

import org.sc.themis.input.Input;
import org.sc.themis.renderer.Renderer;
import org.sc.themis.scene.Scene;
import org.sc.themis.shared.exception.ThemisException;

public interface Gamestate {

    void setup(Renderer renderer, Scene scene ) throws ThemisException; //Initialisation du gamestate => ajout des elements a la scene
    void cleanup( Renderer renderer, Scene scene ) throws ThemisException ; //Nettoyage du gamestate => Suppression des elements de la scene

    void input( Scene scene, Input input, long tpf );
    void update( Scene scene, long tpf );

}

package org.sc.themis.gamestate;

import org.sc.themis.input.Input;
import org.sc.themis.scene.Scene;

public interface Gamestate {

    void setup( Scene scene ); //Initialisation du gamestate => ajout des elements a la scene
    void cleanup( Scene scene ); //Nettoyage du gamestate => Suppression des elements de la scene

    void input( Scene scene, Input input, long tpf );
    void update( Scene scene, long tpf );

}

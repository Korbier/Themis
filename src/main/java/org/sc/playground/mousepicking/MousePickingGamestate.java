package org.sc.playground.mousepicking;

import org.sc.themis.gamestate.Gamestate;
import org.sc.themis.input.Input;
import org.sc.themis.renderer.Renderer;
import org.sc.themis.scene.MeshFactory;
import org.sc.themis.scene.Model;
import org.sc.themis.scene.Scene;
import org.sc.themis.shared.exception.ThemisException;

public class MousePickingGamestate implements Gamestate {

    private final MeshFactory meshFactory = new MeshFactory();

    private Model model;

    @Override
    public void setup(Renderer renderer, Scene scene) throws ThemisException {

        scene.getCamera().setPosition( 0.0f, 0.0f, 7.0f );

        this.model = createCubeModel(renderer);
        scene.add( this.model.create() );
        scene.add( this.model.create().position(  4.0f, 0.0f, 0.0f ).scale( 0.5f) );
        scene.add( this.model.create().position( -4.0f, 0.0f, 0.0f ).rotate( 45.0f, 1.0f, 0.0f, 0.0f ) );

    }

    @Override
    public void cleanup(Renderer renderer, Scene scene) throws ThemisException {
        this.model.cleanup();
    }

    @Override
    public void input(Scene scene, Input input, long tpf) {

    }

    @Override
    public void update(Scene scene, long tpf) {

    }

    private Model createCubeModel(Renderer renderer) throws ThemisException {
        return new Model( "my-cube-model", this.meshFactory.createCube( renderer.getResourceAllocator(), "my-cube ") );
    }

}
package org.sc.viewer.gamestate;

import org.sc.themis.gamestate.Gamestate;
import org.sc.themis.input.Input;
import org.sc.themis.renderer.Renderer;
import org.sc.themis.scene.*;
import org.sc.themis.shared.exception.ThemisException;

import java.nio.file.Path;

public class ViewerGamestate implements Gamestate {

    private final ModelFactory modelFactory = new ModelFactory();

    private Model model;

    @Override
    public void setup(Renderer renderer, Scene scene) throws ThemisException {

        scene.getCamera().setPosition( 0.0f, 1.0f, 5.0f );

        this.model = createSphere(renderer, "sphere-1");
        scene.add( this.model.create() );

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

    private Model createSphere(Renderer renderer, String id) throws ThemisException {
        return this.modelFactory.create( id, renderer.getResourceAllocator(), Path.of( "./src/main/resources/model/sphere/scene.gltf") );
    }

}

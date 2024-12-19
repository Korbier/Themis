package org.sc.playground.scene.triangle2;

import org.sc.themis.gamestate.Gamestate;
import org.sc.themis.input.Input;
import org.sc.themis.renderer.Renderer;
import org.sc.themis.scene.*;
import org.sc.themis.shared.exception.ThemisException;

public class SceneTriangle2Gamestate implements Gamestate {

    private final MeshFactory meshFactory = new MeshFactory();
    private final ModelFactory modelFactory = new ModelFactory();

    private Model model;

    @Override
    public void setup(Renderer renderer, Scene scene) throws ThemisException {

        this.model = createTriangleModel(renderer);
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

    private Model createTriangleModel(Renderer renderer) throws ThemisException {
        return this.modelFactory.create( "my-triangle-model", this.meshFactory.createTriangle( renderer.getResourceAllocator(), "my-triangle ") );
    }

}

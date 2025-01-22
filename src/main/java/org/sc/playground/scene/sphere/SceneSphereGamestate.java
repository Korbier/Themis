package org.sc.playground.scene.sphere;

import org.sc.themis.gamestate.Gamestate;
import org.sc.themis.renderer.Renderer;
import org.sc.themis.scene.Model;
import org.sc.themis.scene.ModelFactory;
import org.sc.themis.scene.Scene;
import org.sc.themis.shared.exception.ThemisException;

import java.nio.file.Path;

public class SceneSphereGamestate implements Gamestate {

    private final ModelFactory modelFactory = new ModelFactory();

    private Model model;

    @Override
    public void setup(Renderer renderer, Scene scene) throws ThemisException {

        scene.getCamera().setPosition( 0.0f, 0.0f, 7.0f );

        this.model = createSphereModel(renderer);
        scene.add( this.model.create() );
        scene.add( this.model.create().position(  4.0f, 0.0f, 0.0f ).scale( 0.5f) );
        scene.add( this.model.create().position( -4.0f, 0.0f, 0.0f ).rotate( 45.0f, 1.0f, 0.0f, 0.0f ) );

    }

    @Override
    public void cleanup(Renderer renderer, Scene scene) throws ThemisException {
        this.model.cleanup();
    }

    private Model createSphereModel(Renderer renderer) throws ThemisException {
        return this.modelFactory.create(
            "my-sphere-model",
            renderer.getResourceAllocator(),
            Path.of( "./src/main/resources/model/sphere/scene.gltf")
        );
    }

}

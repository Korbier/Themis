package org.sc.playground.scene.cube4;

import org.joml.Vector4f;
import org.sc.themis.gamestate.Gamestate;
import org.sc.themis.input.Input;
import org.sc.themis.renderer.Renderer;
import org.sc.themis.renderer.material.MaterialProperties;
import org.sc.themis.renderer.material.MaterialProperty;
import org.sc.themis.scene.*;
import org.sc.themis.shared.exception.ThemisException;

public class SceneCube4Gamestate implements Gamestate {

    private final MeshFactory meshFactory = new MeshFactory();
    private final ModelFactory modelFactory = new ModelFactory();

    private Model model;
    private Model model2;
    private Model model3;

    @Override
    public void setup(Renderer renderer, Scene scene) throws ThemisException {

        scene.getCamera().setPosition( 0.0f, 0.0f, 7.0f );

        this.model3 = createCubeModel("cube3", renderer, new Vector4f(1.0f, 0.0f, 0.0f, 1.0f ));
        scene.add( this.model3.create().position( -4.0f, 0.0f, 0.0f ).rotate( 45.0f, 1.0f, 0.0f, 0.0f ) );

        this.model = createCubeModel( "cube1", renderer, new Vector4f(0.0f, 1.0f, 0.0f, 1.0f ));
        scene.add( this.model.create() );

        this.model2 = createCubeModel("cube2", renderer, new Vector4f(0.0f, 0.0f, 1.0f, 1.0f ));
        scene.add( this.model2.create().position(  4.0f, 0.0f, 0.0f ).scale( 0.5f) );

    }

    @Override
    public void cleanup(Renderer renderer, Scene scene) throws ThemisException {
        this.model.cleanup();
        this.model2.cleanup();
        this.model3.cleanup();
    }

    @Override
    public void input(Scene scene, Input input, long tpf) {

    }

    @Override
    public void update(Scene scene, long tpf) {

    }

    private Model createCubeModel(String prefix, Renderer renderer, Vector4f color) throws ThemisException {

        MaterialProperties material = new MaterialProperties();
        material.put( MaterialProperty.COLOR_BASE, color );


        Mesh cube = this.meshFactory.createCube( renderer.getResourceAllocator(), prefix + "my-cube ", ColorMaterial.IDENTIFIER );
        cube.setProperties( material );

        return this.modelFactory.create( prefix + "my-cube-model", cube );

    }

}

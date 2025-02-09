package org.sc.viewer.gamestate;

import org.joml.Vector3f;
import org.sc.themis.gamestate.Gamestate;
import org.sc.themis.renderer.Renderer;
import org.sc.themis.scene.*;
import org.sc.themis.scene.controller.FpsCameraController;
import org.sc.themis.scene.factory.MaterialFactory;
import org.sc.themis.scene.factory.ModelFactory;
import org.sc.themis.scene.light.DirectionalLight;
import org.sc.themis.shared.exception.ThemisException;
import org.sc.viewer.gamestate.controller.PostProcessorController;
import org.sc.viewer.renderactivity.postprocess.postprocessor.ShowTBNPostprocessor;

import java.nio.file.Path;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_F1;

public class ViewerGamestate implements Gamestate {

    private final PostProcessorContext ppContext = new PostProcessorContext();
    private final ModelFactory modelFactory = new ModelFactory();
    private final MaterialFactory materialFactory = new MaterialFactory();

    private Model model;

    @Override
    public void setup(Renderer renderer, Scene scene) throws ThemisException {
        setupPostProcessors(scene);
        setupCamera(scene);
        setupScene(renderer, scene);
    }

    @Override
    public void cleanup(Renderer renderer, Scene scene) throws ThemisException {
        this.model.cleanup();
    }

    public PostProcessorContext getPostProcessorContext() {
        return this.ppContext;
    }

    private void setupPostProcessors(Scene scene) {

        this.ppContext.add(ShowTBNPostprocessor.IDENTIFIER);

        PostProcessorController ppController = new PostProcessorController(this.ppContext);
        ppController.map(GLFW_KEY_F1, ShowTBNPostprocessor.IDENTIFIER);

        scene.add(ppController);
        scene.add(new FpsCameraController(scene));

    }

    private void setupCamera(Scene scene) {
        scene.getCamera().setPosition(0.0f, 1.0f, 5.0f);
    }

    private void setupScene(Renderer renderer, Scene scene) throws ThemisException {

        this.model = createSphere(renderer, "sphere-1");
        this.model.setMaterialProperties(this.materialFactory.colored(1.0f, 0.0f, 0.0f, 128.0f));

        scene.add(this.model.create());

        scene.add(new DirectionalLight(
            new Vector3f(0.01f),
            new Vector3f(0.3f),
            new Vector3f(0.9f),
            new Vector3f(0.0f, 0.0f, -2.0f))
        );

    }

    private Model createSphere(Renderer renderer, String id) throws ThemisException {
        return this.modelFactory.create(id,
                renderer.getResourceAllocator(),
                Path.of("./src/main/resources/model/cube/cube.obj"));
    }

}

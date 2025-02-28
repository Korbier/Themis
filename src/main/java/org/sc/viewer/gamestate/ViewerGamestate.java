package org.sc.viewer.gamestate;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_F1;

import java.nio.file.Path;
import org.joml.Vector3f;
import org.sc.themis.gamestate.Gamestate;
import org.sc.themis.renderer.Renderer;
import org.sc.themis.scene.Model;
import org.sc.themis.scene.Scene;
import org.sc.themis.scene.controller.FpsCameraController;
import org.sc.themis.scene.factory.MaterialFactory;
import org.sc.themis.scene.factory.ModelFactory;
import org.sc.themis.scene.light.Attenuation;
import org.sc.themis.scene.light.DirectionalLight;
import org.sc.themis.scene.light.PointLight;
import org.sc.themis.scene.light.SpotLight;
import org.sc.themis.scene.pen.Pencil;
import org.sc.themis.shared.exception.ThemisException;
import org.sc.viewer.gamestate.controller.PostProcessorController;
import org.sc.viewer.renderactivity.postprocess.postprocessor.ShowTBNPostprocessor;

public class ViewerGamestate implements Gamestate {

    private final PostProcessorContext ppContext = new PostProcessorContext();
    private final ModelFactory modelFactory = new ModelFactory();
    private final MaterialFactory materialFactory = new MaterialFactory();

    private Model model;

    @Override
    public void setup(Renderer renderer, Scene scene) throws ThemisException {
        setupPostProcessors(scene);
        setupCamera(scene);
        setupUI(scene);
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
        scene.getCamera().setPosition(0.0f, 1.0f, 8.0f);
    }

    private void setupUI(Scene scene) {
        scene.getPencil()
                .drawRect(10.0f, 10.0f, 200.0f, 100.0f)
                .drawRect(210.0f, 110.0f, 200.0f, 100.0f)
                .drawTriangle(200.0f, 200.0f, 300.0f, 300.0f, 200.0f, 300.0f);
    }

    private void setupScene(Renderer renderer, Scene scene) throws ThemisException {

        this.model = createSphere(renderer, "sphere-1");
        this.model.setMaterialProperties(this.materialFactory.color(1.0f, 1.0f, 1.0f, 128.0f));

        scene.add(this.model.create().position(-3.0f,  3.0f, 0.0f));
        scene.add(this.model.create().position(-3.0f,  0.0f, 0.0f));
        scene.add(this.model.create().position(-3.0f, -3.0f, 0.0f));

        scene.add(this.model.create().position(0.0f,  3.0f, 0.0f));
        scene.add(this.model.create().position(0.0f, -3.0f, 0.0f));

        scene.add(this.model.create().position(3.0f,  3.0f, 0.0f));
        scene.add(this.model.create().position(3.0f,  0.0f, 0.0f));
        scene.add(this.model.create().position(3.0f, -3.0f, 0.0f));

        scene.add(new SpotLight(
                new Vector3f(0.0f, 0.0f, 0.1f),
                new Vector3f(0.0f, 0.0f, 0.7f),
                new Vector3f(0.0f, 0.0f, 0.9f),
                new Vector3f(0.0f, 0.5f, 10.0f),
                new Vector3f(0.0f, 0.5f, -10.0f),
                Attenuation.type1(128.0f, 64.0f),
                (float) Math.cos(Math.toRadians(18.0f)),
                (float) Math.cos(Math.toRadians(20.0f))
        ));

        scene.add(new DirectionalLight(
            new Vector3f(0.01f),
            new Vector3f(0.1f),
            new Vector3f(0.3f),
            new Vector3f(0.0f, 0.0f, -1.0f))
        );

        scene.add(new PointLight(
            new Vector3f(0.01f),
            new Vector3f(0.4f, 0.0f, 0.0f),
            new Vector3f(0.7f, 0.0f, 0.0f),
            new Vector3f(5.0f, 5.0f, 5.0f),
            Attenuation.type1(128.0f, 64.0f)
        ));

    }

    private Model createSphere(Renderer renderer, String id) throws ThemisException {
        return this.modelFactory.create(id,
            renderer.getResourceAllocator(),
            Path.of("./src/main/resources/model/sphere/scene.gltf"));
    }

}

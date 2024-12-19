package org.sc.playground;

import org.sc.playground.controller.fpscamera.ControllerFpsCameraGamestate;
import org.sc.playground.controller.fpscamera.ControllerFpsCameraRendererActivity;
import org.sc.playground.descriptorset.imagesampler.DescriptorsetImageSamplerRendererActivity;
import org.sc.playground.descriptorset.uniform.DescriptorsetUniformRendererActivity;
import org.sc.playground.mousepicking.MousePickingGamestate;
import org.sc.playground.mousepicking.MousePickingRendererActivity;
import org.sc.playground.scene.cube.SceneCubeGamestate;
import org.sc.playground.scene.cube.SceneCubeRendererActivity;
import org.sc.playground.scene.cube2.SceneCube2Gamestate;
import org.sc.playground.scene.cube2.SceneCube2RendererActivity;
import org.sc.playground.scene.sphere.SceneSphereGamestate;
import org.sc.playground.scene.sphere.SceneSphereRendererActivity;
import org.sc.playground.scene.triangle.SceneTriangleRendererActivity;
import org.sc.playground.noop.NoopGamestate;
import org.sc.playground.noop.NoopRendererActivity;
import org.sc.playground.pushconstant.PushConstantRendererActivity;
import org.sc.playground.resource.stagingimage.ResourceStagingImageRendererActivity;
import org.sc.playground.scene.triangle2.SceneTriangle2Gamestate;
import org.sc.playground.scene.triangle2.SceneTriangle2RendererActivity;
import org.sc.playground.triangle.TriangleRendererActivity;
import org.sc.themis.gamestate.Gamestate;
import org.sc.themis.renderer.activity.RendererActivity;
import org.sc.themis.shared.Configuration;

import java.util.function.Function;

public enum Playgrounds {

    NOOP( "noop", new NoopGamestate(), NoopRendererActivity::new),
    TRIANGLE( "triangle", new NoopGamestate(), TriangleRendererActivity::new ),
    PUSH_CONSTANT( "pushconstant", new NoopGamestate(), PushConstantRendererActivity::new ),
    DESC_UNIFORM( "desc-uniform", new NoopGamestate(), DescriptorsetUniformRendererActivity::new ),
    DESC_IMAGESAMPLER( "desc-imagesampler", new NoopGamestate(), DescriptorsetImageSamplerRendererActivity::new ),
    RES_STAGINGIMAGE( "res-stagingimage", new NoopGamestate(), ResourceStagingImageRendererActivity::new ),
    SCENE_TRIANGLE( "scene-triangle", new NoopGamestate(), SceneTriangleRendererActivity::new ),
    SCENE_TRIANGLE_2( "scene-triangle-2", new SceneTriangle2Gamestate(), SceneTriangle2RendererActivity::new ),
    SCENE_CUBE( "scene-cube", new SceneCubeGamestate(), SceneCubeRendererActivity::new ),
    SCENE_SPHERE( "scene-sphere", new SceneSphereGamestate(), SceneSphereRendererActivity::new ),
    MOUSEPICKING( "mousepicking", new MousePickingGamestate(), MousePickingRendererActivity::new ),
    CONTROLLER_FPS_CAMERA( "controller-fpscamera", new ControllerFpsCameraGamestate(), ControllerFpsCameraRendererActivity::new );

    final String name;
    final Gamestate gamestate;
    final Function<Configuration, RendererActivity> rendererActivityFactory;

    Playgrounds(String name, Gamestate gamestate, Function<Configuration, RendererActivity> rendererActivityFactory) {
        this.name = name;
        this.gamestate = gamestate;
        this.rendererActivityFactory = rendererActivityFactory;
    }

    public Function<Configuration, RendererActivity> getFactory() {
        return this.rendererActivityFactory;
    }

    public Gamestate getGamestate() {
        return this.gamestate;
    }

    public static Playgrounds fromName( String name ) {

        for (Playgrounds playground : values() ) {
            if ( playground.name.equals( name ) ) return playground;
        }

        return Playgrounds.NOOP;

    }

}

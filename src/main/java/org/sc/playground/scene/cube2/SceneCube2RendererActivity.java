package org.sc.playground.scene.cube2;

import org.sc.playground.shared.BaseRendererActivity;
import org.sc.themis.renderer.command.VkCommand;
import org.sc.themis.renderer.framebuffer.VkFrameBuffer;
import org.sc.themis.renderer.sync.VkFence;
import org.sc.themis.scene.*;
import org.sc.themis.scene.descriptorset.SceneDescriptorSet;
import org.sc.themis.shared.Configuration;
import org.sc.themis.shared.exception.ThemisException;

import static org.lwjgl.vulkan.VK10.*;

public class SceneCube2RendererActivity extends BaseRendererActivity {

    private SceneDescriptorSet sceneDescriptorSet;
    private ColorMaterial colorMaterial;

    public SceneCube2RendererActivity(Configuration configuration) {
        super(configuration);
    }

    @Override
    public void render(Scene scene, long tpf) throws ThemisException {

        int frame = this.renderer.acquire(scene);

        this.sceneDescriptorSet.update( frame, scene );

        VkCommand       command     = getCommand( frame );
        VkFence         fence       = getFence( frame );
        VkFrameBuffer   framebuffer = getFramebuffer( frame );

        command.begin();
        command.beginRenderPass( this.renderPass, framebuffer );
        command.viewportAndScissor( this.renderer.getExtent() );
        command.bindPipeline(this.colorMaterial.getPipeline());

        for ( Model model : scene.getModels() ) {
            if ( model.isRenderable() ) {
                for (Mesh mesh : model.getMeshes() ) {

                    command.bindDescriptorSets(
                        new int[0],
                        this.colorMaterial.getDescriptorSets( frame, mesh.getProperties())
                    );

                    command.bindBuffers(mesh.getVerticesBuffer(), mesh.getIndicesBuffer());

                    for (Instance instance : model.getInstances() ) {
                        command.pushConstant( VK_SHADER_STAGE_VERTEX_BIT, 0, instance.matrix() );
                        command.drawIndexed(mesh.getIndiceCount());
                    }

                }
            }
        }

        command.endRenderPass();
        command.end();
        command.submit( fence, this.renderer.getAcquireSemaphore( frame ), this.renderer.getPresentSemaphore( frame ) );

        fence.waitForAndReset();

    }

    @Override
    public void setup( Scene scene ) throws ThemisException {
        for ( Model model : scene.getModels() ) {
            for ( Mesh mesh : model.getMeshes() ) {
                this.colorMaterial.add( mesh.getProperties() );
            }
        }
    }

    @Override
    public void setupPipeline() throws ThemisException {
        this.setupSceneDescriptorSet();
    }

    @Override
    public void cleanupPipeline() throws ThemisException {
        this.colorMaterial.cleanup();
        this.sceneDescriptorSet.cleanup();
    }

    private void setupSceneDescriptorSet() throws ThemisException {

        this.sceneDescriptorSet = new SceneDescriptorSet( getConfiguration(), this.renderer);
        this.sceneDescriptorSet.setup();

        this.colorMaterial = new ColorMaterial( getConfiguration(), this.renderer, this.renderPass, this.sceneDescriptorSet );
        this.colorMaterial.setup();

    }


}

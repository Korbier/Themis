package org.sc.playground.scene.cube3;

import org.sc.playground.shared.BaseRendererActivity;
import org.sc.themis.renderer.command.VkCommand;
import org.sc.themis.renderer.framebuffer.VkFrameBuffer;
import org.sc.themis.renderer.sync.VkFence;
import org.sc.themis.scene.Instance;
import org.sc.themis.scene.Mesh;
import org.sc.themis.scene.Model;
import org.sc.themis.scene.Scene;
import org.sc.themis.scene.descriptorset.SceneDescriptorSet;
import org.sc.themis.scene.material.BaseTextureMaterial;
import org.sc.themis.shared.Configuration;
import org.sc.themis.shared.exception.ThemisException;

import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_VERTEX_BIT;

public class SceneCube3RendererActivity extends BaseRendererActivity {

    private SceneDescriptorSet sceneDescriptorSet;
    private BaseTextureMaterial material;

    public SceneCube3RendererActivity(Configuration configuration) {
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
        command.bindPipeline(this.material.getPipeline());

        for ( Model model : scene.getModels() ) {
            if ( model.isRenderable() ) {
                for (Mesh mesh : model.getMeshes() ) {

                    command.bindDescriptorSets(
                        new int[0],
                        this.material.getDescriptorSet( mesh.getProperties(), frame )
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
                this.material.add( mesh.getProperties() );
            }
        }
    }

    @Override
    public void setupPipeline() throws ThemisException {
        this.setupSceneDescriptorSet();
    }

    @Override
    public void cleanupPipeline() throws ThemisException {
        this.material.cleanup();
        this.sceneDescriptorSet.cleanup();
    }

    private void setupSceneDescriptorSet() throws ThemisException {

        this.sceneDescriptorSet = new SceneDescriptorSet( getConfiguration(), this.renderer);
        this.sceneDescriptorSet.setup();

        this.material = new BaseTextureMaterial( getConfiguration(), this.renderer, this.renderPass, this.sceneDescriptorSet );
        this.material.setup();

    }


}

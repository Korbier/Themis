package org.sc.viewer.renderactivity;

import org.sc.themis.renderer.Renderer;
import org.sc.themis.renderer.activity.RendererActivity;
import org.sc.themis.renderer.base.frame.FrameKey;
import org.sc.themis.renderer.base.frame.Frames;
import org.sc.themis.renderer.device.VkDevice;
import org.sc.themis.renderer.sync.VkSemaphore;
import org.sc.themis.scene.Scene;
import org.sc.themis.scene.descriptorset.MousePickingDescriptorSet;
import org.sc.themis.scene.descriptorset.SceneDescriptorSet;
import org.sc.themis.shared.Configuration;
import org.sc.themis.shared.exception.ThemisException;
import org.sc.viewer.gamestate.ViewerGamestate;
import org.sc.viewer.renderactivity.geometry.GeometryRenderPass;
import org.sc.viewer.renderactivity.picking.MousePickingRenderPass;
import org.sc.viewer.renderactivity.postprocess.PostProcessRenderPass;
import org.sc.viewer.renderactivity.shadow.ShadowRenderPass;
import org.sc.viewer.renderactivity.ui.UiRenderPass;

public class ViewerRendererActivity extends RendererActivity {

    private final ViewerGamestate gamestate;
    private Renderer renderer;

    /** Renderpasses **/
    private final MousePickingRenderPass mousePickingRenderPass;
    private final ShadowRenderPass shadowRenderPass;
    private final GeometryRenderPass geometryRenderPass;
    private final PostProcessRenderPass postProcessRenderPass;
    private final UiRenderPass uiRenderPass;

    /** Additionnal Semaphores **/
    private FrameKey<VkSemaphore> semPickingPassCompleted;
    private FrameKey<VkSemaphore> semShadowPassCompleted;
    private FrameKey<VkSemaphore> semGeometryPassCompleted;
    private FrameKey<VkSemaphore> semPostProcessPassCompleted;

    /** Common descriptorsets **/
    private SceneDescriptorSet dsScene;
    private MousePickingDescriptorSet dsMousePicking;

    public ViewerRendererActivity(Configuration configuration, ViewerGamestate gamestate) {
        super(configuration);
        this.gamestate              = gamestate;
        this.mousePickingRenderPass = new MousePickingRenderPass( configuration );
        this.shadowRenderPass       = new ShadowRenderPass( configuration );
        this.geometryRenderPass     = new GeometryRenderPass( configuration );
        this.postProcessRenderPass  = new PostProcessRenderPass( configuration, gamestate.getPostProcessorContext() );
        this.uiRenderPass           = new UiRenderPass( configuration );
    }

    public Renderer getRenderer() {
        return this.renderer;
    }

    public Frames getFrames() {
        return this.renderer.getFrames();
    }

    public VkDevice getDevice() {
        return this.renderer.getDevice();
    }

    public SceneDescriptorSet getSceneDescriptorset() {
        return this.dsScene;
    }

    public MousePickingDescriptorSet getMousePickingDescriptorset() {
        return this.dsMousePicking;
    }

    public GeometryRenderPass getGeometryRenderPass() {
        return this.geometryRenderPass;
    }

    @Override
    public void setup(Renderer renderer) throws ThemisException {

        this.renderer = renderer;

        setupDescriptorsets();
        setupRenderPasses();
        setupSemaphores();

    }

    @Override
    public void setup(Scene scene) throws ThemisException {

        this.mousePickingRenderPass.setup( scene );
        this.shadowRenderPass.setup( scene );
        this.geometryRenderPass.setup( scene );
        this.postProcessRenderPass.setup( scene );
        this.uiRenderPass.setup( scene );

    }

    private void setupDescriptorsets() throws ThemisException {

        this.dsScene = new SceneDescriptorSet(getConfiguration(), renderer );
        this.dsScene.setup();

        this.dsMousePicking = new MousePickingDescriptorSet(getConfiguration(), renderer);
        this.dsMousePicking.setup();

    }

    private void setupRenderPasses() throws ThemisException {
        this.mousePickingRenderPass.setup( this );
        this.shadowRenderPass.setup( this );
        this.geometryRenderPass.setup( this );
        this.postProcessRenderPass.setup( this );
        this.uiRenderPass.setup( this );
    }

    private void setupSemaphores() throws ThemisException {

        this.semPickingPassCompleted = FrameKey.of( VkSemaphore.class );
        getFrames().create( this.semPickingPassCompleted, () -> new VkSemaphore(getConfiguration(), getDevice() ) );

        this.semShadowPassCompleted = FrameKey.of( VkSemaphore.class );
        getFrames().create( this.semShadowPassCompleted, () -> new VkSemaphore(getConfiguration(), getDevice() ) );

        this.semGeometryPassCompleted = FrameKey.of( VkSemaphore.class );
        getFrames().create( this.semGeometryPassCompleted, () -> new VkSemaphore(getConfiguration(), getDevice() ) );

        this.semPostProcessPassCompleted = FrameKey.of( VkSemaphore.class );
        getFrames().create( this.semPostProcessPassCompleted, () -> new VkSemaphore(getConfiguration(), getDevice() ) );

    }

    @Override
    public void cleanup() throws ThemisException {

        getFrames().remove( this.semPostProcessPassCompleted );
        getFrames().remove( this.semGeometryPassCompleted );
        getFrames().remove( this.semShadowPassCompleted );
        getFrames().remove( this.semPickingPassCompleted );

        this.dsMousePicking.cleanup();
        this.dsScene.cleanup();

        this.uiRenderPass.cleanup();
        this.postProcessRenderPass.cleanup();
        this.geometryRenderPass.cleanup();
        this.shadowRenderPass.cleanup();
        this.mousePickingRenderPass.cleanup();

    }

    @Override
    public void render( Scene scene, long tpf ) throws ThemisException {

        int frame = this.renderer.acquire( scene );

        this.update( frame, scene);
        this.render( frame, scene );

    }

    @Override
    public void resize() throws ThemisException {
        this.mousePickingRenderPass.resize();
        this.shadowRenderPass.resize();
        this.geometryRenderPass.resize();
        this.postProcessRenderPass.resize();
        this.uiRenderPass.resize();
    }

    private void update( int frame, Scene scene) {
        this.dsScene.update( frame, scene );
    }

    private void render(int frame, Scene scene) throws ThemisException {
        /** Cas nominal
        this.mousePickingRenderPass.render( frame, scene, this.renderer.getAcquireSemaphore( frame ),                 getFrames().get( frame, this.semPickingPassCompleted ) );
        this.shadowRenderPass.render(       frame, scene, getFrames().get( frame, this.semPickingPassCompleted ),     getFrames().get( frame, this.semShadowPassCompleted ) );
        this.geometryRenderPass.render(     frame, scene, getFrames().get( frame, this.semShadowPassCompleted ),      getFrames().get( frame, this.semGeometryPassCompleted ) );
        this.postProcessRenderPass.render(  frame, scene, getFrames().get( frame, this.semGeometryPassCompleted ),    getFrames().get( frame, this.semPostProcessPassCompleted ) );
        this.uiRenderPass.render(           frame, scene, getFrames().get( frame, this.semPostProcessPassCompleted ), this.renderer.getPresentSemaphore( frame ) );
        **/
        this.geometryRenderPass.render(     frame, scene, this.renderer.getAcquireSemaphore( frame ), getFrames().get( frame, this.semGeometryPassCompleted ) );
        this.postProcessRenderPass.render(  frame, scene, getFrames().get( frame, this.semGeometryPassCompleted ), this.renderer.getPresentSemaphore( frame ) );
    }

}
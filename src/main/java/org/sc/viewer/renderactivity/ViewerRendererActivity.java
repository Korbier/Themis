package org.sc.viewer.renderactivity;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.sc.themis.renderer.Renderer;
import org.sc.themis.renderer.RendererActivity;
import org.sc.themis.renderer.base.device.VkDevice;
import org.sc.themis.renderer.base.frame.FrameKey;
import org.sc.themis.renderer.base.frame.Frames;
import org.sc.themis.renderer.base.framebuffer.VkFrameBufferAttachments;
import org.sc.themis.renderer.base.sync.VkFence;
import org.sc.themis.renderer.base.sync.VkSemaphore;
import org.sc.themis.renderer.material.MaterialManager;
import org.sc.themis.scene.Scene;
import org.sc.themis.scene.descriptorset.InputDescriptorSet;
import org.sc.themis.scene.descriptorset.MousePickingDescriptorSet;
import org.sc.themis.scene.descriptorset.SceneDescriptorSet;
import org.sc.themis.scene.light.pipeline.LightDescriptorSet;
import org.sc.themis.shared.configuration.Configuration;
import org.sc.themis.shared.exception.ThemisException;
import org.sc.viewer.ViewerContext;
import org.sc.viewer.gamestate.ViewerGamestate;
import org.sc.viewer.renderactivity.geometry.GeometryRenderPass;
import org.sc.viewer.renderactivity.picking.MousePickingRenderPass;
import org.sc.viewer.renderactivity.postprocess.PostProcessRenderPass;
import org.sc.viewer.renderactivity.shadow.ShadowRenderPass;
import org.sc.viewer.renderactivity.ui.UiRenderPass;

import static org.lwjgl.vulkan.VK10.*;

/**
 * Viewer renderer activity.
 */
@ApplicationScoped
public class ViewerRendererActivity extends RendererActivity {

  public static final String GEOMETRY_FB_ATTACHMENT_COLOR = "geometry.framebuffer.attachment.color";
  public static final String GEOMETRY_FB_ATTACHMENT_DEPTH = "geometry.framebuffer.attachment.depth";

  @Inject
  ViewerGamestate gamestate;
  @Inject
  Configuration configuration;
  @Inject
  MaterialManager materialManager;
  @Inject
  ViewerContext context;
  private Renderer renderer;

  // Renderpasses
  private MousePickingRenderPass mousePickingRenderPass;
  private ShadowRenderPass shadowRenderPass;
  private GeometryRenderPass geometryRenderPass;
  private PostProcessRenderPass postProcessRenderPass;
  private UiRenderPass uiRenderPass;

  // Renderpasses common data
  private VkFrameBufferAttachments geometryFrameBufferAttachments;

  // Sync.
  private FrameKey<VkSemaphore> semPickingPassCompleted;
  private FrameKey<VkSemaphore> semShadowPassCompleted;
  private FrameKey<VkSemaphore> semGeometryPassCompleted;
  private FrameKey<VkSemaphore> semPostProcessPassCompleted;
  private FrameKey<VkFence> fenceGlobal;

  // Common descriptorsets
  private SceneDescriptorSet dsScene;
  private MousePickingDescriptorSet dsMousePicking;
  private LightDescriptorSet dsLight;
  private InputDescriptorSet dsGeometry;

  @PostConstruct
  public void start() {
    this.mousePickingRenderPass = new MousePickingRenderPass();
    this.shadowRenderPass = new ShadowRenderPass();
    this.geometryRenderPass = new GeometryRenderPass(materialManager);
    this.postProcessRenderPass = new PostProcessRenderPass(context);
    this.uiRenderPass = new UiRenderPass(gamestate.getPencil());
  }

  public Renderer getRenderer() {
    return this.renderer;
  }

  public Frames getFrames() {
    return this.renderer.getFramesInFlight();
  }

  public VkDevice getDevice() {
    return this.renderer.getDevice();
  }

  public VkFrameBufferAttachments getGeometryFrameBufferAttachments() {
    return this.geometryFrameBufferAttachments;
  }

  public SceneDescriptorSet getSceneDescriptorset() {
    return this.dsScene;
  }

  public MousePickingDescriptorSet getMousePickingDescriptorset() {
    return this.dsMousePicking;
  }

  public LightDescriptorSet getLighDescriptorset() {
    return this.dsLight;
  }

  public InputDescriptorSet getGeometryDescriptorset() {
    return this.dsGeometry;
  }

  public GeometryRenderPass getGeometryRenderPass() {
    return this.geometryRenderPass;
  }

  @Override
  public void setup(Renderer renderer) throws ThemisException {

    this.renderer = renderer;

    setupGeometryFrameBufferAttachments();
    setupDescriptorsets();
    setupRenderPasses();
    setupSemaphores();
  }

  @Override
  public void setup(Scene scene) throws ThemisException {

    this.mousePickingRenderPass.setup(scene);
    this.shadowRenderPass.setup(scene);
    this.geometryRenderPass.setup(scene);
    this.postProcessRenderPass.setup(scene);
    this.uiRenderPass.setup(scene);

    this.dsLight.setup(scene);
  }

  private void setupGeometryFrameBufferAttachments() throws ThemisException {
    this.geometryFrameBufferAttachments = new VkFrameBufferAttachments(getDevice(), getRenderer().getExtent());
    this.geometryFrameBufferAttachments.setup();
    this.geometryFrameBufferAttachments.depth(GEOMETRY_FB_ATTACHMENT_DEPTH, VK_FORMAT_D32_SFLOAT, VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT, 1);
    this.geometryFrameBufferAttachments.color(GEOMETRY_FB_ATTACHMENT_COLOR, VK_FORMAT_R16G16B16A16_UNORM, VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT, VK_SAMPLE_COUNT_1_BIT);
  }

  private void setupDescriptorsets() throws ThemisException {

    this.dsScene = new SceneDescriptorSet(renderer);
    this.dsScene.setup();

    this.dsMousePicking = new MousePickingDescriptorSet(renderer);
    this.dsMousePicking.setup();

    this.dsLight = new LightDescriptorSet(renderer);
    this.dsLight.setup();

    setupGeometryDescriptorset();
  }

  private void setupGeometryDescriptorset() throws ThemisException {
    this.dsGeometry = new InputDescriptorSet(getRenderer(), getGeometryFrameBufferAttachments().size());
    this.dsGeometry.setup();
  }

  private void setupRenderPasses() throws ThemisException {
    this.mousePickingRenderPass.setup(this);
    this.shadowRenderPass.setup(this);
    this.geometryRenderPass.setup(this);
    this.postProcessRenderPass.setup(this);
    this.uiRenderPass.setup(this);
  }

  private void setupSemaphores() throws ThemisException {

    this.semPickingPassCompleted = FrameKey.of(VkSemaphore.class);
    getFrames().create(this.semPickingPassCompleted, () -> new VkSemaphore(getDevice()));

    this.semShadowPassCompleted = FrameKey.of(VkSemaphore.class);
    getFrames().create(this.semShadowPassCompleted, () -> new VkSemaphore(getDevice()));

    this.semGeometryPassCompleted = FrameKey.of(VkSemaphore.class);
    getFrames().create(this.semGeometryPassCompleted, () -> new VkSemaphore(getDevice()));

    this.semPostProcessPassCompleted = FrameKey.of(VkSemaphore.class);
    getFrames().create(this.semPostProcessPassCompleted, () -> new VkSemaphore(getDevice()));

    this.fenceGlobal = FrameKey.of(VkFence.class);
    getFrames().create(this.fenceGlobal, () -> new VkFence(getDevice(), true));
  }

  @Override
  public void cleanup() throws ThemisException {

    getRenderer().waitIdle();

    getFrames().remove(this.semPostProcessPassCompleted);
    getFrames().remove(this.semGeometryPassCompleted);
    getFrames().remove(this.semShadowPassCompleted);
    getFrames().remove(this.semPickingPassCompleted);

    this.dsGeometry.cleanup();
    this.dsLight.cleanup();
    this.dsMousePicking.cleanup();
    this.dsScene.cleanup();

    this.geometryFrameBufferAttachments.cleanup();

    this.uiRenderPass.cleanup();
    this.postProcessRenderPass.cleanup();
    this.geometryRenderPass.cleanup();
    this.shadowRenderPass.cleanup();
    this.mousePickingRenderPass.cleanup();
  }

  @Override
  public void render(Scene scene, long tpf) throws ThemisException {

    int frame = this.renderer.acquire(scene);

    this.update(frame, scene);
    this.render(frame, scene);
  }

  private void render(int frame, Scene scene) throws ThemisException {

    // this.mousePickingRenderPass.render(
    //      frame, scene,
    //      this.renderer.getAcquireSemaphore(frame),
    //      getFrames().get(frame, this.semPickingPassCompleted));
    // this.shadowRenderPass.render(
    //      frame, scene,
    //      getFrames().get(frame, this.semPickingPassCompleted),
    //      getFrames().get(frame, this.semShadowPassCompleted));
    // this.geometryRenderPass.render(
    //      frame, scene,
    //      getFrames().get(frame, this.semShadowPassCompleted),
    //      getFrames().get(frame, this.semGeometryPassCompleted));
    // this.postProcessRenderPass.render(
    //      frame, scene,
    //      getFrames().get(frame, this.semGeometryPassCompleted),
    //      getFrames().get(frame, this.semPostProcessPassCompleted));
    // this.uiRenderPass.render(
    //      frame, scene,
    //      getFrames().get(frame, this.semPostProcessPassCompleted),
    //      this.renderer.getPresentSemaphore(frame));

    VkFence fence = getFrames().get(frame, this.fenceGlobal);
    fence.waitForAndReset();

    this.geometryRenderPass.render(frame, scene, this.renderer.getAcquireSemaphore(frame), getFrames().get(frame, this.semGeometryPassCompleted));

    this.postProcessRenderPass.render(frame, scene, getFrames().get(frame, this.semGeometryPassCompleted), getFrames().get(frame, this.semPostProcessPassCompleted));

    this.uiRenderPass.render(frame, scene, getFrames().get(frame, this.semPostProcessPassCompleted), this.renderer.getPresentSemaphore(frame), fence);
  }

  @Override
  public void resize(Scene scene) throws ThemisException {

    this.dsGeometry.cleanup();
    this.geometryFrameBufferAttachments.cleanup();

    setupGeometryFrameBufferAttachments();
    setupGeometryDescriptorset();

    this.mousePickingRenderPass.resize(scene);
    this.shadowRenderPass.resize(scene);
    this.geometryRenderPass.resize(scene);
    this.postProcessRenderPass.resize(scene);
    this.uiRenderPass.resize(scene);
  }

  private void update(int frame, Scene scene) throws ThemisException {
    this.dsLight.update(frame, scene);
    this.dsScene.update(frame, scene);
    this.dsGeometry.update(frame, getGeometryFrameBufferAttachments());
  }
}

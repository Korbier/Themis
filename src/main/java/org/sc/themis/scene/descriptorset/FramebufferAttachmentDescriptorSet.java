package org.sc.themis.scene.descriptorset;

import org.sc.themis.core.LifeCycle;
import org.sc.themis.renderer.Renderer;
import org.sc.themis.renderer.base.frame.FrameKey;
import org.sc.themis.renderer.base.framebuffer.VkFrameBufferAttachment;
import org.sc.themis.renderer.base.pipeline.descriptorset.VkDescriptorPool;
import org.sc.themis.renderer.base.pipeline.descriptorset.VkDescriptorSet;
import org.sc.themis.renderer.base.pipeline.descriptorset.VkDescriptorSetBinding;
import org.sc.themis.renderer.base.pipeline.descriptorset.VkDescriptorSetLayout;
import org.sc.themis.shared.exception.ThemisException;

import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_FRAGMENT_BIT;

/**
 *
 */
public class FramebufferAttachmentDescriptorSet implements LifeCycle {

  private static final FrameKey<VkDescriptorSet> FK_DESCRIPTORSET = FrameKey.of(VkDescriptorSet.class);

  private final Renderer renderer;
  private final VkFrameBufferAttachment[] attachments;

  private VkDescriptorSetLayout descriptorSetLayout;
  private VkDescriptorPool descriptorPool;

  public FramebufferAttachmentDescriptorSet(Renderer renderer, VkFrameBufferAttachment... attachments) {
    this.renderer = renderer;
    this.attachments = attachments;
  }

  public VkDescriptorSetLayout getDescriptorSetLayout() {
    return this.descriptorSetLayout;
  }

  public VkDescriptorSet getDescriptorSet(int frame) {
    return this.renderer.getFramesInFlight().get(frame, FK_DESCRIPTORSET);
  }

  @Override
  public void setup() throws ThemisException {
    setupDescriptorLayout();
    setupDescriptorPool();
    setupDescriptorSets();
  }

  private void setupDescriptorSets() throws ThemisException {
    this.renderer.getFramesInFlight().create(FK_DESCRIPTORSET, () -> new VkDescriptorSet(this.renderer.getDevice(), this.descriptorPool, this.descriptorSetLayout));
    this.renderer.getFramesInFlight().update(FK_DESCRIPTORSET, desc -> {
      for (int i = 0; i < this.attachments.length; i++) {
        desc.bind(i, this.attachments[i]);
      }
    });
  }

  private void setupDescriptorLayout() throws ThemisException {

    VkDescriptorSetBinding[] bindings = new VkDescriptorSetBinding[this.attachments.length];
    for (int i = 0; i < this.attachments.length; i++) {
      bindings[i] = VkDescriptorSetBinding.input(i, VK_SHADER_STAGE_FRAGMENT_BIT);
    }

    this.descriptorSetLayout = new VkDescriptorSetLayout(this.renderer.getDevice(), bindings);
    this.descriptorSetLayout.setup();
  }

  private void setupDescriptorPool() throws ThemisException {
    this.descriptorPool = new VkDescriptorPool(this.renderer.getDevice(), this.renderer.getFramesInFlight().getSize(), this.descriptorSetLayout);
    this.descriptorPool.setup();
  }

  @Override
  public void cleanup() throws ThemisException {
    this.renderer.getFramesInFlight().remove(FK_DESCRIPTORSET);
    this.descriptorPool.cleanup();
    this.descriptorSetLayout.cleanup();
  }
}

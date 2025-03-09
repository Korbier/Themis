package org.sc.themis.scene.descriptorset;

import static org.lwjgl.vulkan.VK10.VK_FILTER_LINEAR;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_FRAGMENT_BIT;

import org.sc.themis.renderer.Renderer;
import org.sc.themis.renderer.base.frame.FrameKey;
import org.sc.themis.renderer.base.framebuffer.VkFrameBufferAttachment;
import org.sc.themis.renderer.base.framebuffer.VkFrameBufferAttachments;
import org.sc.themis.renderer.base.pipeline.descriptorset.VkDescriptorPool;
import org.sc.themis.renderer.base.pipeline.descriptorset.VkDescriptorSet;
import org.sc.themis.renderer.base.pipeline.descriptorset.VkDescriptorSetBinding;
import org.sc.themis.renderer.base.pipeline.descriptorset.VkDescriptorSetLayout;
import org.sc.themis.renderer.base.pipeline.descriptorset.VkDescriptorSetProvider;
import org.sc.themis.renderer.base.resource.image.VkSampler;
import org.sc.themis.renderer.base.resource.image.VkSamplerDescriptor;
import org.sc.themis.shared.Configuration;
import org.sc.themis.shared.exception.ThemisException;
import org.sc.themis.shared.tobject.TObject;

public class InputDescriptorSet extends TObject implements VkDescriptorSetProvider {

  private final Renderer renderer;
  private final FrameKey<VkDescriptorSet> descriptorSets = FrameKey.of(VkDescriptorSet.class);

  private VkDescriptorSetLayout descriptorSetLayout;
  private VkDescriptorPool descriptorPool;
  private final int size;

  private VkSampler sampler;

  public InputDescriptorSet(Configuration configuration, Renderer renderer, int size) {
    super(configuration);
    this.renderer = renderer;
    this.size = size;
  }

  @Override
  public void setup() throws ThemisException {
    this.sampler = createDefaultSampler();
    this.descriptorSetLayout = createDescriptorSetLayout();
    this.descriptorPool = createDescriptorPool(this.descriptorSetLayout);
    createDescriptorSets(this.descriptorPool, this.descriptorSetLayout);
  }

  @Override
  public void cleanup() throws ThemisException {
    this.descriptorPool.cleanup();
    this.descriptorSetLayout.cleanup();
    this.sampler.cleanup();
  }

  public VkDescriptorSetLayout getLayout() {
    return this.descriptorSetLayout;
  }

  @Override
  public VkDescriptorSetLayout getDescriptorSetLayout() {
    return this.descriptorSetLayout;
  }

  public VkDescriptorSet getDescriptorSet(int frame) {
    return this.renderer.getFramesInFlight().get(frame, this.descriptorSets);
  }

  public void update(int frame, VkFrameBufferAttachments... inputAttachments)
      throws ThemisException {

    VkDescriptorSet descriptorSet =
        this.renderer.getFramesInFlight().get(frame, this.descriptorSets);

    int i = 0;
    for (VkFrameBufferAttachments inputs : inputAttachments) {
      for (VkFrameBufferAttachment attachment : inputs.get()) {
        if (attachment.getType() != VkFrameBufferAttachment.VkFrameBufferAttachmentType.RAW) {
          descriptorSet.bind(i++, attachment, this.sampler);
        }
      }
    }
  }

  private VkDescriptorSetLayout createDescriptorSetLayout() throws ThemisException {

    VkDescriptorSetBinding[] bindings = new VkDescriptorSetBinding[this.size];

    for (int i = 0; i < this.size; i++) {
      bindings[i] = VkDescriptorSetBinding.attachment(VK_SHADER_STAGE_FRAGMENT_BIT);
    }

    VkDescriptorSetLayout descriptorSetLayout =
        new VkDescriptorSetLayout(getConfiguration(), this.renderer.getDevice(), bindings);
    descriptorSetLayout.setup();

    return descriptorSetLayout;
  }

  private VkDescriptorPool createDescriptorPool(VkDescriptorSetLayout layout)
      throws ThemisException {
    VkDescriptorPool pool =
        new VkDescriptorPool(
            getConfiguration(), this.renderer.getDevice(), this.renderer.getFrameCount(), layout);
    pool.setup();
    return pool;
  }

  private void createDescriptorSets(VkDescriptorPool pool, VkDescriptorSetLayout layout)
      throws ThemisException {
    this.renderer.getFramesInFlight().create(this.descriptorSets, pool::create);
  }

  private VkSampler createDefaultSampler() throws ThemisException {
    VkSampler sampler =
        new VkSampler(
            getConfiguration(),
            this.renderer.getDevice(),
            new VkSamplerDescriptor(VK_FILTER_LINEAR, 1, false));
    sampler.setup();
    return sampler;
  }
}

package org.sc.themis.renderer.pencil2d;

import org.sc.themis.renderer.Renderer;
import org.sc.themis.renderer.base.command.VkCommand;
import org.sc.themis.renderer.base.pipeline.descriptorset.VkDescriptorSet;
import org.sc.themis.renderer.base.renderpass.VkRenderPass;
import org.sc.themis.renderer.base.resource.buffer.VkBuffer;
import org.sc.themis.renderer.base.resource.buffer.VkBufferDescriptor;
import org.sc.themis.renderer.pencil2d.pipeline.Pencil2DChannelPipeline;
import org.sc.themis.renderer.pencil2d.pipeline.TextChannelPipeline;
import org.sc.themis.renderer.pencil2d.pipeline.TriangleChannelPipeline;
import org.sc.themis.scene.Scene;
import org.sc.themis.shared.exception.ThemisException;
import org.sc.themis.core.LifeCycle;
import org.sc.themis.shared.utils.MemorySizeUtils;

import java.util.HashMap;
import java.util.Map;

public class Pencil2DPipeline implements LifeCycle {


  private final Renderer renderer;
  private final VkRenderPass pass;
  private final Pencil2D pencil;

  private Pencil2DChannelPipeline trianglePipeline;
  private Pencil2DChannelPipeline textPipeline;

  private Pencil2DDescriptorset pencilDescriptorSet;
  private Map<Pencil2DChannel, ChannelBuffers> buffers = new HashMap<>();
  //private VkBuffer drawCommandVertexBuffer;
  //private VkBuffer drawCommandIndiceBuffer;

  public Pencil2DPipeline(Renderer renderer, VkRenderPass pass, Pencil2D pencil) {
    this.renderer = renderer;
    this.pass = pass;
    this.pencil = pencil;
  }

  @Override
  public void setup() throws ThemisException {
    setupDescriptorset();
    setupPipeline();
  }

  private void setupDescriptorset() throws ThemisException {
    this.pencilDescriptorSet = new Pencil2DDescriptorset(this.renderer, this.pencil.fonts());
    this.pencilDescriptorSet.setup();
  }

  private void setupPipeline() throws ThemisException {
    this.trianglePipeline = new TriangleChannelPipeline(renderer, this.pass, pencilDescriptorSet.getDescriptorSetLayout());
    this.trianglePipeline.setup();
    this.textPipeline = new TextChannelPipeline(renderer, this.pass, pencilDescriptorSet.getDescriptorSetLayout());
    this.textPipeline.setup();
  }

  @Override
  public void cleanup() throws ThemisException {

    for (ChannelBuffers cBuffers : this.buffers.values()) {
      if (cBuffers.drawCommandVertexBuffer != null) cBuffers.drawCommandVertexBuffer.cleanup();
      if (cBuffers.drawCommandIndiceBuffer != null) cBuffers.drawCommandIndiceBuffer.cleanup();
    }

    this.pencilDescriptorSet.cleanup();

    this.textPipeline.cleanup();
    this.trianglePipeline.cleanup();

  }

  public void draw(VkCommand command, int frame) throws ThemisException {
    for (Pencil2DLayer layer : this.pencil.layers()) {
      drawChannel(command, frame, layer.getTriangleChannel(), this.trianglePipeline);
      //drawChannel(command, frame, layer.getTextChannel(), this.textPipeline);
    }
  }

  private void drawChannel(VkCommand command, int frame, Pencil2DChannel channel, Pencil2DChannelPipeline pipeline ) throws ThemisException {
    if ( channel.isRenderable() ) {
      ChannelBuffers buffers = getBuffers(channel);
      command.bindPipeline(pipeline.getPipeline());
      command.bindDescriptorSets(new int[0], this.getDescriptorset(frame));
      command.bindBuffers(buffers.drawCommandVertexBuffer, buffers.drawCommandIndiceBuffer);
      command.drawIndexed(channel.getIndiceCount());
    }
  }

  public void update(Scene scene) throws ThemisException {
    this.pencilDescriptorSet.updateAll(scene);
  }

  private ChannelBuffers getBuffers(Pencil2DChannel channel) throws ThemisException {

    ChannelBuffers cBuffers = null;

    long channelVertexSize = (long) channel.getVertexLength() * MemorySizeUtils.FLOAT;
    long channelIndiceSize = (long) channel.getIndiceCount() * MemorySizeUtils.INT;

    if (this.buffers.containsKey(channel)) {

      cBuffers = this.buffers.get(channel);

      if (cBuffers.drawCommandVertexBuffer.getRequestedSize() < channelVertexSize) {
        cBuffers.drawCommandVertexBuffer.cleanup();
        cBuffers.drawCommandVertexBuffer = null;
      }

      if (cBuffers.drawCommandIndiceBuffer.getRequestedSize() < channelIndiceSize) {
        cBuffers.drawCommandIndiceBuffer.cleanup();
        cBuffers.drawCommandIndiceBuffer = null;
      }

    }

    if (cBuffers == null) {
      cBuffers = new ChannelBuffers();
      this.buffers.put(channel, cBuffers);
    }

    if (cBuffers.drawCommandVertexBuffer == null) {
      VkBufferDescriptor decriptor = VkBufferDescriptor.vertexBuffer(channelVertexSize);
      cBuffers.drawCommandVertexBuffer = new VkBuffer(this.renderer.getDevice(), this.renderer.getMemoryAllocator(), decriptor);
      cBuffers.drawCommandVertexBuffer.setup();
    }

    if (cBuffers.drawCommandIndiceBuffer == null) {
      VkBufferDescriptor decriptorIndices = VkBufferDescriptor.indiceBuffer(channelIndiceSize);
      cBuffers.drawCommandIndiceBuffer = new VkBuffer(this.renderer.getDevice(), this.renderer.getMemoryAllocator(), decriptorIndices);
      cBuffers.drawCommandIndiceBuffer.setup();
    }

    cBuffers.drawCommandVertexBuffer.set(0, channel.getVertices());
    cBuffers.drawCommandIndiceBuffer.set(0, channel.getIndices());

    return cBuffers;


  }

  public VkDescriptorSet getDescriptorset(int frame) {
    return this.pencilDescriptorSet.getDescriptorSet(frame);
  }

  private class ChannelBuffers {
    public VkBuffer drawCommandVertexBuffer = null;
    public VkBuffer drawCommandIndiceBuffer = null;
  }
}

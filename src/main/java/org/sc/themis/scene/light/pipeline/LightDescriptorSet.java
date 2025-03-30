package org.sc.themis.scene.light.pipeline;

import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_STORAGE_BUFFER_BIT;
import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT;
import static org.lwjgl.vulkan.VK10.VK_MEMORY_PROPERTY_HOST_CACHED_BIT;
import static org.lwjgl.vulkan.VK10.VK_MEMORY_PROPERTY_HOST_COHERENT_BIT;
import static org.lwjgl.vulkan.VK10.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_FRAGMENT_BIT;

import java.util.List;
import org.sc.themis.renderer.Renderer;
import org.sc.themis.renderer.base.frame.FrameKey;
import org.sc.themis.renderer.base.pipeline.descriptorset.VkDescriptorPool;
import org.sc.themis.renderer.base.pipeline.descriptorset.VkDescriptorSet;
import org.sc.themis.renderer.base.pipeline.descriptorset.VkDescriptorSetBinding;
import org.sc.themis.renderer.base.pipeline.descriptorset.VkDescriptorSetLayout;
import org.sc.themis.renderer.base.pipeline.descriptorset.VkDescriptorSetProvider;
import org.sc.themis.renderer.base.resource.buffer.VkBuffer;
import org.sc.themis.renderer.base.resource.buffer.VkBufferDescriptor;
import org.sc.themis.renderer.base.resource.buffer.VkBufferFiller;
import org.sc.themis.scene.Scene;
import org.sc.themis.scene.light.DirectionalLight;
import org.sc.themis.scene.light.PointLight;
import org.sc.themis.scene.light.SpotLight;
import org.sc.themis.shared.configuration.Configuration;
import org.sc.themis.shared.exception.ThemisException;
import org.sc.themis.shared.tobject.TObject;
import org.sc.themis.shared.utils.MemorySizeUtils;

/**
 * Descriptorset layout.
 *
 * <pre>
 * MemorySizeUtils.MAT4x4F Projection
 * MemorySizeUtils.MAT4x4F View
 * MemorySizeUtils.MAT4x4F Inverse projection
 * MemorySizeUtils.MAT4x4F Inverse view
 * MemorySizeUtils.MAT4x4F Camera position
 * MemorySizeUtils.VEC2F   Resolution
 * MemorySizeUtils.INT     UTime</pre>
 *
 * <p>Shader source.
 *
 * <pre>
 *
 *  struct DirectionalLight {
 *      vec4 ambient;
 *      vec4 diffuse;
 *      vec4 specular;
 *      vec4 visible;
 *      vec4 direction;
 *  };
 *
 *  struct PointLight {
 *      vec4 ambient;
 *      vec4 diffuse;
 *      vec4 specular;
 *      vec4 visible;
 *      vec4 position;
 *      vec4 attenuation;
 *  };
 *
 *  struct SpotLight {
 *      vec4 ambient;
 *      vec4 diffuse;
 *      vec4 specular;
 *      vec4 visible;
 *      vec4 position;
 *      vec4 direction;
 *      vec4 attenuation;
 *      float innerCutOff; //cos(rad(angle))
 *      float outerCutOff; //cos(rad(angle))
 *  };
 *
 *  layout(std140, set = 3, binding = 0) uniform Lights {
 *      uint directionalLightCount;
 *      uint pointLightCount;
 *      uint spotLightCount;
 *  } lights;
 *
 *  layout(std430, set = 3, binding = 1) readonly buffer DirectionalLights {
 *      DirectionalLight lights[];
 *  } directionalLights;
 *
 *  layout(std430, set = 3, binding = 2) readonly buffer PointLights {
 *      PointLight lights[];
 *  } pointLights;
 *
 *  layout(std430, set = 3, binding = 3) readonly buffer SpotLights {
 *      SpotLight lights[];
 *  } spotLights;
 * </pre>
 */
public class LightDescriptorSet extends TObject implements VkDescriptorSetProvider {

  private static final FrameKey<VkBuffer> FK_BUFFER_DATA = FrameKey.of(VkBuffer.class);
  private static final FrameKey<VkBuffer> FK_BUFFER_DIRECTIONAL_LIGHTS =
      FrameKey.of(VkBuffer.class);
  private static final FrameKey<VkBuffer> FK_BUFFER_POINT_LIGHTS = FrameKey.of(VkBuffer.class);
  private static final FrameKey<VkBuffer> FK_BUFFER_SPOT_LIGHTS = FrameKey.of(VkBuffer.class);
  private static final FrameKey<VkDescriptorSet> FK_DESCRIPTORSET =
      FrameKey.of(VkDescriptorSet.class);

  private final Renderer renderer;

  private VkDescriptorSetLayout descriptorSetLayout;
  private VkDescriptorPool descriptorPool;

  /**
   * Constructor.
   *
   * @param configuration Globale configuration
   * @param renderer Renderer
   */
  public LightDescriptorSet(Configuration configuration, Renderer renderer) {
    super(configuration);
    this.renderer = renderer;
  }

  /**
   * Update all framed data with provided scene.
   *
   * @param scene scene
   */
  public void updateAll(Scene scene) {
    for (int frame = 0; frame < renderer.getFramesInFlight().getSize(); frame++) {
      update(frame, scene);
    }
  }

  /**
   * Update frame with provided scene.
   *
   * @param scene scene
   */
  public void update(int frame, Scene scene) {
    updateData(frame, scene);
    updateDirectionalLights(frame, scene.getDirectionalLights());
    updatePointLights(frame, scene.getPointLights());
    updateSpotLights(frame, scene.getSpotLights());
  }

  public VkDescriptorSetLayout getDescriptorSetLayout() {
    return this.descriptorSetLayout;
  }

  /**
   * Return descriptorset for provided frame.
   *
   * @param frame frame
   */
  public VkDescriptorSet getDescriptorSet(int frame) {
    return this.renderer.getFramesInFlight().get(frame, FK_DESCRIPTORSET);
  }

  /** Descriptorset setup. */
  @Override
  public void setup() throws ThemisException {
    setupDescriptorLayout();
    setupDescriptorPool();
    setupDescriptorSets();
  }

  /** Descriptorset setup. */
  public void setup(Scene scene) throws ThemisException {
    setupBuffersData(scene);
    setupBuffersDirectionalLights(scene);
    setupBuffersPointLights(scene);
    setupBuffersSpotLights(scene);
  }

  private void setupDescriptorSets() throws ThemisException {
    this.renderer
        .getFramesInFlight()
        .create(
            FK_DESCRIPTORSET,
            () ->
                new VkDescriptorSet(
                    getConfiguration(),
                    this.renderer.getDevice(),
                    this.descriptorPool,
                    this.descriptorSetLayout));
  }

  private void setupBuffersData(Scene scene) throws ThemisException {

    VkBufferDescriptor descriptor =
        new VkBufferDescriptor(
            MemorySizeUtils.VEC4F,
            VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT,
            VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT,
            0);

    this.renderer
        .getFramesInFlight()
        .create(
            FK_BUFFER_DATA,
            () ->
                new VkBuffer(
                    getConfiguration(),
                    this.renderer.getDevice(),
                    this.renderer.getMemoryAllocator(),
                    descriptor));

    this.renderer
        .getFramesInFlight()
        .update(
            FK_DESCRIPTORSET,
            (frame, descriptorset) ->
                descriptorset.bind(
                    0, this.renderer.getFramesInFlight().get(frame, FK_BUFFER_DATA)));
  }

  private void setupBuffersDirectionalLights(Scene scene) throws ThemisException {

    VkBufferDescriptor descriptor =
        new VkBufferDescriptor(
            getDirectionalLightsBufferSize(scene),
            VK_BUFFER_USAGE_STORAGE_BUFFER_BIT,
            VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_CACHED_BIT,
            VK_MEMORY_PROPERTY_HOST_COHERENT_BIT);

    this.renderer
        .getFramesInFlight()
        .create(
            FK_BUFFER_DIRECTIONAL_LIGHTS,
            () ->
                new VkBuffer(
                    getConfiguration(),
                    this.renderer.getDevice(),
                    this.renderer.getMemoryAllocator(),
                    descriptor));

    this.renderer
        .getFramesInFlight()
        .update(
            FK_DESCRIPTORSET,
            (frame, descriptorset) ->
                descriptorset.bind(
                    1, this.renderer.getFramesInFlight().get(frame, FK_BUFFER_DIRECTIONAL_LIGHTS)));
  }

  private void setupBuffersPointLights(Scene scene) throws ThemisException {

    VkBufferDescriptor descriptor =
        new VkBufferDescriptor(
            getPointLightsBufferSize(scene),
            VK_BUFFER_USAGE_STORAGE_BUFFER_BIT,
            VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_CACHED_BIT,
            VK_MEMORY_PROPERTY_HOST_COHERENT_BIT);

    this.renderer
        .getFramesInFlight()
        .create(
            FK_BUFFER_POINT_LIGHTS,
            () ->
                new VkBuffer(
                    getConfiguration(),
                    this.renderer.getDevice(),
                    this.renderer.getMemoryAllocator(),
                    descriptor));

    this.renderer
        .getFramesInFlight()
        .update(
            FK_DESCRIPTORSET,
            (frame, descriptorset) ->
                descriptorset.bind(
                    2, this.renderer.getFramesInFlight().get(frame, FK_BUFFER_POINT_LIGHTS)));
  }

  private void setupBuffersSpotLights(Scene scene) throws ThemisException {

    VkBufferDescriptor descriptor =
        new VkBufferDescriptor(
            getSpotLightsBufferSize(scene),
            VK_BUFFER_USAGE_STORAGE_BUFFER_BIT,
            VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_CACHED_BIT,
            VK_MEMORY_PROPERTY_HOST_COHERENT_BIT);

    this.renderer
        .getFramesInFlight()
        .create(
            FK_BUFFER_SPOT_LIGHTS,
            () ->
                new VkBuffer(
                    getConfiguration(),
                    this.renderer.getDevice(),
                    this.renderer.getMemoryAllocator(),
                    descriptor));

    this.renderer
        .getFramesInFlight()
        .update(
            FK_DESCRIPTORSET,
            (frame, descriptorset) ->
                descriptorset.bind(
                    3, this.renderer.getFramesInFlight().get(frame, FK_BUFFER_SPOT_LIGHTS)));
  }

  private void setupDescriptorLayout() throws ThemisException {
    this.descriptorSetLayout =
        new VkDescriptorSetLayout(
            getConfiguration(),
            this.renderer.getDevice(),
            VkDescriptorSetBinding.uniform(0, VK_SHADER_STAGE_FRAGMENT_BIT),
            VkDescriptorSetBinding.storageBuffer(1, VK_SHADER_STAGE_FRAGMENT_BIT),
            VkDescriptorSetBinding.storageBuffer(2, VK_SHADER_STAGE_FRAGMENT_BIT),
            VkDescriptorSetBinding.storageBuffer(3, VK_SHADER_STAGE_FRAGMENT_BIT));
    this.descriptorSetLayout.setup();
  }

  private void setupDescriptorPool() throws ThemisException {
    this.descriptorPool =
        new VkDescriptorPool(
            getConfiguration(),
            this.renderer.getDevice(),
            this.renderer.getFramesInFlight().getSize(),
            this.descriptorSetLayout);
    this.descriptorPool.setup();
  }

  @Override
  public void cleanup() throws ThemisException {
    this.renderer.getFramesInFlight().remove(FK_BUFFER_DATA);
    this.renderer.getFramesInFlight().remove(FK_DESCRIPTORSET);
    this.descriptorPool.cleanup();
    this.descriptorSetLayout.cleanup();
  }

  private void updateData(int frame, Scene scene) {
    VkBufferFiller buffer = this.renderer.getFramesInFlight().get(frame, FK_BUFFER_DATA).filler();
    buffer.put(scene.getLightData());
  }

  private void updateDirectionalLights(int frame, List<DirectionalLight> lights) {

    VkBufferFiller buffer =
        this.renderer.getFramesInFlight().get(frame, FK_BUFFER_DIRECTIONAL_LIGHTS).filler();

    for (DirectionalLight light : lights) {
      buffer.put(light.getAmbient(), MemorySizeUtils.VEC4F);
      buffer.put(light.getDiffuse(), MemorySizeUtils.VEC4F);
      buffer.put(light.getSpecular(), MemorySizeUtils.VEC4F);
      buffer.put(light.getData(), MemorySizeUtils.VEC4F);
      buffer.put(light.getPosition(), MemorySizeUtils.VEC4F);
    }
  }

  private void updatePointLights(int frame, List<PointLight> lights) {

    VkBufferFiller buffer =
        this.renderer.getFramesInFlight().get(frame, FK_BUFFER_POINT_LIGHTS).filler();

    for (PointLight light : lights) {
      buffer.put(light.getAmbient(), MemorySizeUtils.VEC4F);
      buffer.put(light.getDiffuse(), MemorySizeUtils.VEC4F);
      buffer.put(light.getSpecular(), MemorySizeUtils.VEC4F);
      buffer.put(light.getData(), MemorySizeUtils.VEC4F);
      buffer.put(light.getPosition(), MemorySizeUtils.VEC4F);
      buffer.put(light.getAttenuation().data(), MemorySizeUtils.VEC4F);
    }
  }

  private void updateSpotLights(int frame, List<SpotLight> lights) {

    VkBufferFiller buffer =
        this.renderer.getFramesInFlight().get(frame, FK_BUFFER_SPOT_LIGHTS).filler();

    for (SpotLight light : lights) {
      buffer.put(light.getAmbient(), MemorySizeUtils.VEC4F);
      buffer.put(light.getDiffuse(), MemorySizeUtils.VEC4F);
      buffer.put(light.getSpecular(), MemorySizeUtils.VEC4F);
      buffer.put(light.getData(), MemorySizeUtils.VEC4F);
      buffer.put(light.getPosition(), MemorySizeUtils.VEC4F);
      buffer.put(light.getDirection(), MemorySizeUtils.VEC4F);
      buffer.put(light.getAttenuation().data(), MemorySizeUtils.VEC4F);
      buffer.put(light.getInnerCutOff());
      buffer.put(light.getOuterCutOff(), MemorySizeUtils.VEC3F);
    }
  }

  private long getDirectionalLightsBufferSize(Scene scene) {
    return scene.getDirectionalLights().isEmpty()
        ? 1L
        : (long) scene.getDirectionalLights().size() * DirectionalLight.SIZE;
  }

  private long getPointLightsBufferSize(Scene scene) {
    return scene.getPointLights().isEmpty()
        ? 1L
        : (long) scene.getPointLights().size() * PointLight.SIZE;
  }

  private long getSpotLightsBufferSize(Scene scene) {
    return scene.getSpotLights().isEmpty()
        ? 1L
        : (long) scene.getSpotLights().size() * SpotLight.SIZE;
  }
}

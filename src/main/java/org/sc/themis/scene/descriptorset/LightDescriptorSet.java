package org.sc.themis.scene.descriptorset;

import org.joml.Matrix4f;
import org.sc.themis.renderer.Renderer;
import org.sc.themis.renderer.base.VulkanObject;
import org.sc.themis.renderer.base.frame.FrameKey;
import org.sc.themis.renderer.base.frame.Frames;
import org.sc.themis.renderer.pipeline.descriptorset.*;
import org.sc.themis.renderer.resource.buffer.VkBuffer;
import org.sc.themis.renderer.resource.buffer.VkBufferDescriptor;
import org.sc.themis.scene.Scene;
import org.sc.themis.scene.light.DirectionalLight;
import org.sc.themis.scene.light.PointLight;
import org.sc.themis.scene.light.SpotLight;
import org.sc.themis.shared.Configuration;
import org.sc.themis.shared.exception.ThemisException;
import org.sc.themis.shared.tobject.TObject;
import org.sc.themis.shared.utils.MemorySizeUtils;

import java.util.List;

import static org.lwjgl.vulkan.VK10.*;

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
 * <p>Shader source.</p>
 *
 * <pre>
 *  struct DirectionalLight {
 *      vec4 direction;
 *      vec3 ambient;
 *      float visible;
 *      vec4 diffuse;
 *      vec3 specular;
 *      float shininess;
 *  };
 *
 *  struct PointLight {
 *      vec4 position;
 *      float attenuationType;
 *      float attenuation1;
 *      float attenuation2;
 *      float attenuation3;
 *      vec3 ambient;
 *      float visible;
 *      vec4 diffuse;
 *      vec3 specular;
 *      float shininess;
 *  };
 *
 *  struct SpotLight {
 *      vec4 position;
 *      vec4 direction;
 *      vec4 attenuation; //x=constant, y=linear, z=quadratic
 *      float innerCutOff; //cos(rad(angle))
 *      float outerCutOff; //cos(rad(angle))
 *      vec2 pad;
 *      vec3 ambient;
 *      float visible;
 *      vec4 diffuse;
 *      vec3 specular;
 *      float shininess;
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
 *
 */
public class LightDescriptorSet extends TObject implements VkDescriptorSetProvider {

    private static final FrameKey<VkBuffer>        FK_BUFFER_DATA = FrameKey.of(VkBuffer.class);
    private static final FrameKey<VkBuffer>        FK_BUFFER_DIRECTIONAL_LIGHTS = FrameKey.of(VkBuffer.class);
    private static final FrameKey<VkBuffer>        FK_BUFFER_POINT_LIGHTS = FrameKey.of(VkBuffer.class);
    private static final FrameKey<VkBuffer>        FK_BUFFER_SPOT_LIGHTS = FrameKey.of(VkBuffer.class);
    private static final FrameKey<VkDescriptorSet> FK_DESCRIPTORSET = FrameKey.of(VkDescriptorSet.class);

    private final Renderer renderer;

    private VkDescriptorSetLayout descriptorSetLayout;
    private VkDescriptorPool descriptorPool;

    private final Matrix4f workInvProjection = new Matrix4f();
    private final Matrix4f workInvView = new Matrix4f();
    private Long utime = null;

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
    public void updateAll(Scene scene) throws ThemisException {
    }

    /**
     * Update frame with provided scene.
     *
     * @param scene scene
     */
    public void update(int frame, Scene scene) {
        updateDirectionalLights(getDescriptorSet(idx), scene.getDirectionalLights());
        updatePointLights(getDescriptorSet(idx), scene.getPointLights());
        updateSpotLights(getDescriptorSet(idx), scene.getSpotLights());
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
        return this.renderer.getFrames().get(frame, FK_DESCRIPTORSET);
    }

    @Override
    public void setup() throws ThemisException {
        setupDescriptorLayout();
        setupDescriptorPool();
        setupDescriptorSets();
    }

    public void setup(Scene scene) throws ThemisException {
        setupBuffersData(scene);
        setupBuffersDirectionalLights(scene);
        setupBuffersPointLights(scene);
        setupBuffersSpotLights(scene);
    }

    private void setupDescriptorSets() throws ThemisException {
        this.renderer.getFrames().create(FK_DESCRIPTORSET, () -> new VkDescriptorSet(getConfiguration(),
                this.renderer.getDevice(), this.descriptorPool, this.descriptorSetLayout));
    }

    private void setupBuffersData(Scene scene) throws ThemisException {

        VkBufferDescriptor descriptor = new VkBufferDescriptor(
                MemorySizeUtils.FLOAT * 3,
                VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT,
                VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT,
                0);

        this.renderer.getFrames().create(
            FK_BUFFER_DATA,
            () -> new VkBuffer(
                getConfiguration(), this.renderer.getDevice(),
                this.renderer.getMemoryAllocator(), descriptor
           )
       );

        this.renderer.getFrames().update(FK_DESCRIPTORSET,
                (frame, descriptorset) -> descriptorset.bind(0, this.renderer.getFrames().get(frame, FK_BUFFER_DATA)));

    }

    private void setupBuffersDirectionalLights(Scene scene) throws ThemisException {

        VkBufferDescriptor descriptor = new VkBufferDescriptor(
                getDirectionalLightsBufferSize(scene),
                VK_BUFFER_USAGE_STORAGE_BUFFER_BIT,
                VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_CACHED_BIT,
                VK_MEMORY_PROPERTY_HOST_COHERENT_BIT);

        this.renderer.getFrames().create(
                FK_BUFFER_DIRECTIONAL_LIGHTS,
                () -> new VkBuffer(
                        getConfiguration(), this.renderer.getDevice(),
                        this.renderer.getMemoryAllocator(), descriptor
               )
       );

        this.renderer.getFrames().update(FK_DESCRIPTORSET,
                (frame, descriptorset) ->
                        descriptorset.bind(1, this.renderer.getFrames().get(frame, FK_BUFFER_DIRECTIONAL_LIGHTS)));

    }

    private void setupBuffersPointLights(Scene scene) throws ThemisException {

        VkBufferDescriptor descriptor = new VkBufferDescriptor(
                getPointLightsBufferSize(scene),
                VK_BUFFER_USAGE_STORAGE_BUFFER_BIT,
                VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_CACHED_BIT,
                VK_MEMORY_PROPERTY_HOST_COHERENT_BIT);

        this.renderer.getFrames().create(
                FK_BUFFER_POINT_LIGHTS,
                () -> new VkBuffer(
                        getConfiguration(), this.renderer.getDevice(),
                        this.renderer.getMemoryAllocator(), descriptor
               )
       );

        this.renderer.getFrames().update(FK_DESCRIPTORSET,
                (frame, descriptorset) ->
                        descriptorset.bind(2, this.renderer.getFrames().get(frame, FK_BUFFER_POINT_LIGHTS)));

    }

    private void setupBuffersSpotLights(Scene scene) throws ThemisException {

        VkBufferDescriptor descriptor = new VkBufferDescriptor(
                getPointLightsBufferSize(scene),
                VK_BUFFER_USAGE_STORAGE_BUFFER_BIT,
                VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_CACHED_BIT,
                VK_MEMORY_PROPERTY_HOST_COHERENT_BIT);

        this.renderer.getFrames().create(
                FK_BUFFER_SPOT_LIGHTS,
                () -> new VkBuffer(
                        getConfiguration(), this.renderer.getDevice(),
                        this.renderer.getMemoryAllocator(), descriptor
               )
       );

        this.renderer.getFrames().update(FK_DESCRIPTORSET,
                (frame, descriptorset) ->
                        descriptorset.bind(3, this.renderer.getFrames().get(frame, FK_BUFFER_SPOT_LIGHTS)));

    }
    private void setupDescriptorLayout() throws ThemisException {
        this.descriptorSetLayout = new VkDescriptorSetLayout(
            getConfiguration(),
            this.renderer.getDevice(),
            VkDescriptorSetBinding.uniform(0, VK_SHADER_STAGE_FRAGMENT_BIT),
            VkDescriptorSetBinding.storageBuffer(1, VK_SHADER_STAGE_FRAGMENT_BIT),
            VkDescriptorSetBinding.storageBuffer(2, VK_SHADER_STAGE_FRAGMENT_BIT),
            VkDescriptorSetBinding.storageBuffer(3, VK_SHADER_STAGE_FRAGMENT_BIT)
      );
        this.descriptorSetLayout.setup();
    }

    private void setupDescriptorPool() throws ThemisException {
        this.descriptorPool = new VkDescriptorPool(getConfiguration(), this.renderer.getDevice(),
                this.renderer.getFrames().getSize(), this.descriptorSetLayout);
        this.descriptorPool.setup();
    }

    @Override
    public void cleanup() throws ThemisException {
        this.renderer.getFrames().remove(FK_BUFFER_DATA);
        this.renderer.getFrames().remove(FK_DESCRIPTORSET);
        this.descriptorPool.cleanup();
        this.descriptorSetLayout.cleanup();
    }

    private void updateDirectionalLights(int frame, List<DirectionalLight> lights) throws ThemisException {

        int offset = 0;
        VkBuffer buffer = this.renderer.getFrames().get(frame, FK_BUFFER_DIRECTIONAL_LIGHTS);
        
        for (DirectionalLight light : lights) {
            buffer.set(offset, light.getDirection());
            offset += MemorySizeUtils.VEC4F;
            buffer.set(offset, light.getAmbient());
            offset += MemorySizeUtils.VEC4F;
            buffer.set(offset, light.isVisible() ? 1.0f : 0.0f);
            offset += MemorySizeUtils.FLOAT;
            buffer.set(offset, light.getDiffuse());
            offset += MemorySizeUtils.VEC4F;
            buffer.set(offset, light.getSpecular());
            offset += MemorySizeUtils.VEC4F;
        }

    }

    private void updatePointLights(VkDescriptorSet descriptorSet, List<PointLight> lights) throws CoreException {
        int offset = 0;
        for (PointLight light : lights) {
            descriptorSet.set(2, offset, light.getPosition()); offset += VkSize.VEC4;
            descriptorSet.set(2, offset, light.getAttenuation().data()); offset += VkSize.VEC4;
            descriptorSet.set(2, offset, light.getAmbient());  offset += VkSize.VEC3;
            descriptorSet.set(2, offset, light.isVisible() ? 1.0f : 0.0f);  offset += VkSize.FLOAT;
            descriptorSet.set(2, offset, light.getDiffuse());  offset += VkSize.VEC4;
            descriptorSet.set(2, offset, light.getSpecular()); offset += VkSize.VEC3;
            descriptorSet.set(2, offset, light.getShininess()); offset += VkSize.FLOAT;
        }
    }

    private void updateSpotLights(VkDescriptorSet descriptorSet, List<SpotLight> lights) throws CoreException {
        int offset = 0;
        for (SpotLight light : lights) {
            descriptorSet.set(3, offset, light.getPosition());  offset += VkSize.VEC4;
            descriptorSet.set(3, offset, light.getDirection()); offset += VkSize.VEC4;
            descriptorSet.set(3, offset, light.getAttenuation()); offset += VkSize.VEC4;
            descriptorSet.set(3, offset, (float) Math.cos(Math.toRadians(light.getInnerCutOff()))); offset += VkSize.FLOAT;
            descriptorSet.set(3, offset, (float) Math.cos(Math.toRadians(light.getOuterCutOff()))); offset += VkSize.FLOAT;
            offset += VkSize.VEC2;
            descriptorSet.set(3, offset, light.getAmbient());  offset += VkSize.VEC3;
            descriptorSet.set(3, offset, light.isVisible() ? 1.0f : 0.0f);  offset += VkSize.FLOAT;
            descriptorSet.set(3, offset, light.getDiffuse());   offset += VkSize.VEC4;
            descriptorSet.set(3, offset, light.getSpecular());  offset += VkSize.VEC3;
            descriptorSet.set(3, offset, light.getShininess()); offset += VkSize.FLOAT;
        }
    }
    private long getDirectionalLightsBufferSize(Scene scene) {
        return scene.getDirectionalLights().isEmpty() ? 1L : (long) scene.getDirectionalLights().size() * DirectionalLight.SIZE;
    }

    private long getPointLightsBufferSize(Scene scene) {
        return scene.getPointLights().isEmpty() ? 1L : (long) scene.getPointLights().size() * PointLight.SIZE;
    }

    private long getSpotLightsBufferSize(Scene scene) {
        return scene.getSpotLights().isEmpty() ? 1L : (long) scene.getSpotLights().size() * SpotLight.SIZE;
    }
}

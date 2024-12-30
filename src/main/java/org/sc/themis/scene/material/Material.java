package org.sc.themis.scene.material;

import org.lwjgl.system.MemoryStack;
import org.sc.themis.renderer.Renderer;
import org.sc.themis.renderer.base.VulkanObject;
import org.sc.themis.renderer.base.frame.FrameKey;
import org.sc.themis.renderer.base.frame.Frames;
import org.sc.themis.renderer.device.VkDevice;
import org.sc.themis.renderer.device.VkMemoryAllocator;
import org.sc.themis.renderer.pipeline.*;
import org.sc.themis.renderer.pipeline.descriptorset.*;
import org.sc.themis.scene.Scene;
import org.sc.themis.scene.exception.MaterialException;
import org.sc.themis.shared.Configuration;
import org.sc.themis.shared.assertion.Assertions;
import org.sc.themis.shared.exception.ThemisException;

import java.util.*;

public abstract class Material extends VulkanObject {

    private final Renderer renderer;
    private final String identifier;

    private final List<VkShaderProgramStage> shaderProgramStages = new ArrayList<>();
    private final List<VkPushConstantRange>  pushConstantRanges = new ArrayList<>();
    private VkVertexInputStateDescriptor vertexInputStateDescriptor = null;
    private VkPipelineDescriptor pipelineDescriptor = null;

    private VkShaderProgram program;
    private VkPipelineLayout layout;
    private VkPipeline pipeline;

    public Material( Configuration configuration, Renderer renderer, String identifier ) {
        super( configuration );
        this.renderer = renderer;
        this.identifier = identifier;
    }

    public abstract VkDescriptorSetLayout [] getDescriptorSetLayout();

    /**
     * Material building methods
     **/

    /** Pipeline **/
    protected void addShader( int shaderStage, byte [] source ) {
        this.shaderProgramStages.add( new VkShaderProgramStage( shaderStage, source ) );
    }

    protected void addConstantRange( int stage, int offset, int size ) {
        this.pushConstantRanges.add( new VkPushConstantRange( stage, offset, size ) );
    }

    protected void setVertexInputDescriptor( VkVertexInputStateDescriptor descriptor ) {
        this.vertexInputStateDescriptor = descriptor;
    }

    protected void setPipelineDescriptor( VkPipelineDescriptor descriptor) {
        this.pipelineDescriptor = descriptor;
    }

    public String getIdentifier() {
        return this.identifier;
    }

    protected VkDevice getDevice() {
        return this.renderer.getDevice();
    }

    protected Frames getFrames() {
        return this.renderer.getFrames();
    }

    protected VkMemoryAllocator getAllocator() {
        return this.renderer.getMemoryAllocator();
    }

    @Override
    public void setup() throws ThemisException {

        setupShaderProgram();
        setupPipelineLayout();
        setupPipeline();
    }

    @Override
    public void cleanup() throws ThemisException {

        this.pipeline.cleanup();
        this.layout.cleanup();
        this.program.cleanup();

    }

    public void setupScene( Scene scene ) throws ThemisException {

    }

    public VkPipeline getPipeline() {
        return this.pipeline;
    }
/**
    public VkDescriptorSetLayout [] getDescriptorSetLayout() {

        int count = this.descriptorSetProviders.length;
        if ( this.mainDescriptorSetLayout != null ) count++;
        if ( this.descriptorSetLayout != null ) count++;

        VkDescriptorSetLayout [] layouts = new VkDescriptorSetLayout[count];
        if ( this.descriptorSetLayout != null ) layouts[--count] = this.descriptorSetLayout;
        if ( this.mainDescriptorSetLayout != null ) layouts[--count] = this.mainDescriptorSetLayout;
        for ( int i = count - 1; i >= 0; i-- ) layouts[i] = this.descriptorSetProviders[i].getDescriptorSetLayout();

        return layouts;

    }

    public VkDescriptorSet [] getDescriptorSet( Mesh mesh, int frame ) {

        int count = this.descriptorSetProviders.length;
        if ( this.mainDescriptorSetLayout != null ) count++;
        if ( this.descriptorSetLayout != null ) count++;

        VkDescriptorSet [] descriptorsets = new VkDescriptorSet[count];
        if ( this.descriptorSetLayout != null ) descriptorsets[--count] = getFrames().get( frame, this.fkDescriptorsets.get( getMaterialInstanceIdentifier( mesh ) ) );
        if ( this.mainDescriptorSetLayout != null ) descriptorsets[--count] = getFrames().get( frame, this.fkMainDescriptorSet );
        for ( int i = count - 1; i >= 0; i-- ) descriptorsets[i] = this.descriptorSetProviders[i].getDescriptorSet( frame );

        return descriptorsets;

    }

    public int [] getDynamicOffset( Mesh mesh, int frame ) {

        for ( int i=0; i<this.wDynamicOffset.length; i++ ) {
            this.wDynamicOffset[i] = getBackBufferDynamicOffset( getMaterialInstanceIdentifier( mesh ), frame, i );
        }

        return this.wDynamicOffset;

    }

    private void setupMainDescriptorLayout() throws ThemisException {

        VkDescriptorSetBinding [] bindings = getMainDescriptorSetBindings();

        if ( bindings.length > 0 ) {

            this.mainDescriptorSetLayout = new VkDescriptorSetLayout( getConfiguration(), getDevice(),bindings );
            this.mainDescriptorSetLayout.setup();

            int countDynamics = 0;

            for ( VkDescriptorSetBinding b : bindings ) {
                if ( b.getDescriptorType() == VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER_DYNAMIC ) countDynamics++;
            }

            this.wDynamicOffset = new int[countDynamics];

        }

    }

    private void setupDescriptorPool( List<Mesh> meshes ) throws ThemisException {

        if ( this.mainDescriptorSetLayout != null ) {
            this.mainDescriptorPool = new VkDescriptorPool(getConfiguration(), getDevice(), getFrames().getSize(), this.mainDescriptorSetLayout);
            this.mainDescriptorPool.setup();
        }

    }

    private void setupDescriptorSets( List<Mesh> meshes ) throws ThemisException {

        if ( this.mainDescriptorSetLayout != null ) {
            getFrames().create(this.fkMainDescriptorSet, () -> new VkDescriptorSet(getConfiguration(), getDevice(), this.mainDescriptorPool, this.mainDescriptorSetLayout));
            setupMainMaterialLayout(this.fkMainDescriptorSet, meshes.toArray(new Mesh[0]));
        }

    }
**/
    private void setupPipeline() throws ThemisException {

        Assertions.notNull( this.pipelineDescriptor, new MaterialException("No Pipeline Descriptor defined (call method setPipelineDescriptor)") );
        Assertions.notNull( this.vertexInputStateDescriptor, new MaterialException("No Vertex InputState defined (call method setVertexInputDescriptor)") );

        try (MemoryStack stack = MemoryStack.stackPush() ) {

            VkVertexInputState inputState = new VkVertexInputState(this.vertexInputStateDescriptor);
            inputState.setup( stack );

            this.pipeline = new VkPipeline(
                    getConfiguration(),
                    this.renderer.getDevice(),
                    this.pipelineDescriptor,
                    this.program,
                    this.layout,
                    inputState
            );

            this.pipeline.setup();

        }

    }

    private void setupShaderProgram() throws ThemisException {

        Assertions.notEmpty( this.shaderProgramStages, new MaterialException("No Shader Program Stage provided (call method addShader)") );

        this.program = new VkShaderProgram(getConfiguration(), renderer.getDevice(), this.shaderProgramStages.toArray(new VkShaderProgramStage[0] ) );
        this.program.setup();

    }

    private void setupPipelineLayout() throws ThemisException {

        this.layout = new VkPipelineLayout(
            getConfiguration(),
            this.renderer.getDevice(),
            this.pushConstantRanges.toArray( new VkPushConstantRange[0] ),
            getDescriptorSetLayout()
        );

        this.layout.setup();

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Material mesh = (Material) o;
        return Objects.equals(identifier, mesh.identifier);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(identifier);
    }

}

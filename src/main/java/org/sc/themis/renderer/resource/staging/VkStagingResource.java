package org.sc.themis.renderer.resource.staging;

import org.joml.Matrix4f;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.sc.themis.renderer.base.VulkanObject;
import org.sc.themis.renderer.command.VkCommand;
import org.sc.themis.renderer.device.VkDevice;
import org.sc.themis.renderer.device.VkMemoryAllocator;
import org.sc.themis.renderer.resource.buffer.VkBuffer;
import org.sc.themis.renderer.resource.buffer.VkBufferDescriptor;
import org.sc.themis.shared.Configuration;
import org.sc.themis.shared.exception.ThemisException;

import java.nio.ByteBuffer;

import static org.lwjgl.vulkan.VK10.*;

public sealed abstract class VkStagingResource
        extends VulkanObject
        permits VkStagingBuffer, VkStagingImage {

    private final VkDevice device;
    private final VkMemoryAllocator allocator;
    private int bufferSize;

    private VkStagingResourceStatus status = VkStagingResourceStatus.CREATED;
    private org.sc.themis.renderer.resource.buffer.VkBuffer stagingBuffer;

    public VkStagingResource(Configuration configuration, VkDevice device, VkMemoryAllocator allocator ) {
        super( configuration );
        this.device = device;
        this.allocator = allocator;
    }

    abstract public void doCommit(VkCommand command) throws ThemisException;

    @Override
    final public void setup() {
    }

    @Override
    final public void cleanup() throws ThemisException {
        if ( this.stagingBuffer != null ) {
            this.status = VkStagingResourceStatus.CREATED;
            cleanupStagingBuffer();
        }
    }

    public void commit( VkCommand command ) throws ThemisException {
        doCommit( command );
        setStatus( VkStagingResourceStatus.COMMITED );
    }

    public VkStagingResourceStatus getStatus() {
        return this.status;
    }

    void setStatus( VkStagingResourceStatus status ) {
        this.status = status;
    }

    protected void setBufferSize( int bufferSize ) {
        this.bufferSize = bufferSize;
    }

    public int getBufferSize() {
        return this.bufferSize;
    }

    public VkBuffer getStagingBuffer() {
        return this.stagingBuffer;
    }

    public void load( int buffersize, int [] data ) throws ThemisException {
        load( buffersize, 0, data );
    }

    public void load( int buffersize, int offset, int [] data ) throws ThemisException {
        this.recreateStagingBuffer( buffersize );
        set( offset, data );
    }

    public void load( int buffersize, float [] data ) throws ThemisException {
        load( buffersize, 0, data );
    }

    public void load( int buffersize, int offset, float [] data ) throws ThemisException {
        this.recreateStagingBuffer( buffersize );
        set( offset, data );
    }

    public void load( ByteBuffer bBuffer ) throws ThemisException {
        this.recreateStagingBuffer( bBuffer.capacity() );
        set( bBuffer );
    }

    public void set( ByteBuffer data ) {
        set( () -> this.stagingBuffer.set( data ) );
    }

    public void set( int offset, Vector3f value ) {
        set( () -> this.stagingBuffer.set( offset, value ) );
    }

    public void set( int offset, Vector4f value ) {
        set( () -> this.stagingBuffer.set( offset, value ) );
    }

    public void set( int offset, Matrix4f value ) {
        set( () -> this.stagingBuffer.set( offset, value ) );
    }

    public void set( int offset, Vector2i value ) {
        set( () -> this.stagingBuffer.set( offset, value ) );
    }

    public void set( int offset, float value ) {
        set( () -> this.stagingBuffer.set( offset, value ) );
    }

    public void set( int offset, int value ) {
        set( () -> this.stagingBuffer.set( offset, value ) );
    }

    public void set( int offset, float ... values ) {
        set( () -> this.stagingBuffer.set( offset, values ) );
    }

    public void set( int offset, int ... values ) {
        set( () -> this.stagingBuffer.set( offset, values ) );
    }

    protected void setupStagingBuffer() throws ThemisException {
        VkBufferDescriptor bufferDescriptor = new VkBufferDescriptor(this.bufferSize, VK_BUFFER_USAGE_TRANSFER_SRC_BIT, VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT, VK_MEMORY_PROPERTY_HOST_COHERENT_BIT );
        this.stagingBuffer = new VkBuffer( getConfiguration(), this.device, this.allocator, bufferDescriptor );
        this.stagingBuffer.setup();
    }

    protected void cleanupStagingBuffer() throws ThemisException {
        this.stagingBuffer.cleanup();
        this.stagingBuffer = null;
    }

    private void recreateStagingBuffer(int buffersize) throws ThemisException {

        if ( this.stagingBuffer == null || this.bufferSize < buffersize ) {

            if ( this.stagingBuffer != null ) {
                cleanupStagingBuffer();
            }

            this.bufferSize = buffersize;
            setupStagingBuffer();

        }

    }

    private void set( Runnable task ) {
        Thread.ofVirtual().start( () -> {
            task.run();
            setStatus( VkStagingResourceStatus.LOADED );
        });
    }
}

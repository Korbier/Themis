package org.sc.themis.scene;

import org.sc.themis.renderer.resource.buffer.VkBuffer;
import org.sc.themis.renderer.resource.staging.VkStagingBuffer;
import org.sc.themis.renderer.resource.staging.VkStagingResourceAllocator;
import org.sc.themis.shared.exception.ThemisException;
import org.sc.themis.shared.utils.MemorySizeUtils;

import java.util.Objects;

import static org.lwjgl.vulkan.VK10.*;

public class Mesh {

    private final String identifier;

    private final VkStagingBuffer vertexBuffer;
    private final VkStagingBuffer indiceBuffer;
    private String material;

    private boolean renderable = false;
    private int vertexCount = 0;
    private int indiceCount = 0;

    Mesh( VkStagingResourceAllocator resourceAllocator, String identifier ) {
        this.identifier = identifier;
        this.vertexBuffer = resourceAllocator.allocateBuffer( VK_BUFFER_USAGE_VERTEX_BUFFER_BIT );
        this.indiceBuffer = resourceAllocator.allocateBuffer( VK_BUFFER_USAGE_INDEX_BUFFER_BIT );
    }

    void set( Vertex[] vertices, int [] indices ) throws ThemisException {
        set( vertices, indices, null );
    }

    void set( Vertex[] vertices, int [] indices, String material ) throws ThemisException {

        this.material = material;
        this.vertexCount = vertices.length;
        this.indiceCount = indices.length;

        float[] aVertices  = toArray( vertices );

        this.vertexBuffer.load( aVertices.length * MemorySizeUtils.FLOAT, 0, aVertices  );
        this.indiceBuffer.load( indices.length * MemorySizeUtils.INT, 0, indices );

    }

    public void cleanup() throws ThemisException {
        this.renderable = false;
        this.vertexBuffer.cleanup();
        this.indiceBuffer.cleanup();
    }

    private float[] toArray(Vertex[] vertices) {
        float [] components = new float[vertices.length * Vertex.COMPONENTS];
        int i = 0;
        for ( Vertex vertex : vertices ) {
            components[i++] = vertex.position().x();
            components[i++] = vertex.position().y();
            components[i++] = vertex.position().z();
            components[i++] = vertex.normale().x();
            components[i++] = vertex.normale().y();
            components[i++] = vertex.normale().z();
            components[i++] = vertex.texture().x();
            components[i++] = vertex.texture().y();
            components[i++] = vertex.tangent().x();
            components[i++] = vertex.tangent().y();
            components[i++] = vertex.tangent().z();
            components[i++] = vertex.bitangent().x();
            components[i++] = vertex.bitangent().y();
            components[i++] = vertex.bitangent().z();
        }
        return components;
    }

    public String getIdentifier() {
        return this.identifier;
    }

    public boolean isRenderable() {

        if ( this.renderable ) {
            return true;
        }

        if ( !this.indiceBuffer.isRenderable() ) return false;
        if ( !this.vertexBuffer.isRenderable() ) return false;

        this.renderable = true;

        return true;

    }

    public int getVertexCount() {
        return this.vertexCount;
    }

    public int getIndiceCount() {
        return this.indiceCount;
    }

    public VkBuffer getVerticesBuffer() {
        return this.vertexBuffer.getBuffer();
    }

    public VkBuffer getIndicesBuffer() {
        return this.indiceBuffer.getBuffer();
    }

    public String getMaterial() {
        return this.material;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Mesh mesh = (Mesh) o;
        return Objects.equals(identifier, mesh.identifier);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(identifier);
    }

}

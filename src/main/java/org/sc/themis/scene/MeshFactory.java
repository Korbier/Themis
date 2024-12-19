package org.sc.themis.scene;

import org.sc.themis.renderer.resource.staging.VkStagingResourceAllocator;
import org.sc.themis.shared.exception.ThemisException;

public class MeshFactory {

    public Mesh create( VkStagingResourceAllocator allocator, String identifier, Vertex [] vertices, int [] indices ) throws ThemisException {
        return create( allocator, identifier, vertices, indices, null );
    }

    public Mesh create( VkStagingResourceAllocator allocator, String identifier, Vertex [] vertices, int [] indices, String material ) throws ThemisException {
        Mesh mesh = new Mesh( allocator, identifier );
        mesh.set( vertices, indices, material );
        return mesh;
    }

    public Mesh createTriangle( VkStagingResourceAllocator allocator, String identifier ) throws ThemisException {
        return createTriangle( allocator, identifier, null );
    }

    public Mesh createTriangle( VkStagingResourceAllocator allocator, String identifier, String material ) throws ThemisException {
        return create(
            allocator,
            identifier,
            new Vertex[] {
                Vertex.of( -1.0f, -1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -1.0f ),
                Vertex.of(  1.0f, -1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -1.0f ),
                Vertex.of(  0.0f,  1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -1.0f )
            },
            new int[] { 0, 1, 2 },
            material
        );
    }

    public Mesh createCube( VkStagingResourceAllocator allocator, String identifier ) throws ThemisException {
        return createCube( allocator, identifier, null );
    }

    public Mesh createCube( VkStagingResourceAllocator allocator, String identifier, String material ) throws ThemisException {
        return create(
                allocator,
                identifier,
                new Vertex[] {
                        Vertex.of(-1f,  1f,  1f, 0f, 0f, 0.0f, 0.0f, 1.0f), //Front 0 - 0
                        Vertex.of( 1f,  1f,  1f, 1f, 0f, 0.0f, 0.0f, 1.0f), //Front 1 - 1
                        Vertex.of( 1f, -1f,  1f, 1f, 1f, 0.0f, 0.0f, 1.0f), //Front 2 - 2
                        Vertex.of(-1f, -1f,  1f, 0f, 1f, 0.0f, 0.0f, 1.0f), //Front 3 - 3
                        Vertex.of( 1f,  1f,  1f, 0f, 0f, 1.0f, 0.0f, 0.0f), //Right 0 - 4
                        Vertex.of( 1f,  1f, -1f, 1f, 0f, 1.0f, 0.0f, 0.0f), //Right 1 - 5
                        Vertex.of( 1f, -1f, -1f, 1f, 1f, 1.0f, 0.0f, 0.0f), //Right 2 - 6
                        Vertex.of( 1f, -1f,  1f, 0f, 1f, 1.0f, 0.0f, 0.0f), //Right 3 - 7
                        Vertex.of( 1f,  1f, -1f, 0f, 0f, 0.0f, 0.0f, -1.0f), //Back 0 - 8
                        Vertex.of(-1f,  1f, -1f, 1f, 0f, 0.0f, 0.0f, -1.0f), //Back 1 - 9
                        Vertex.of(-1f, -1f, -1f, 1f, 1f, 0.0f, 0.0f, -1.0f), //Back 2 - 10
                        Vertex.of( 1f, -1f, -1f, 0f, 1f, 0.0f, 0.0f, -1.0f), //Back 3 - 11
                        Vertex.of(-1f,  1f, -1f, 0f, 0f, -1.0f, 0.0f, 0.0f), //Left 0 - 12
                        Vertex.of(-1f,  1f,  1f, 1f, 0f, -1.0f, 0.0f, 0.0f), //Left 1 - 13
                        Vertex.of(-1f, -1f,  1f, 1f, 1f, -1.0f, 0.0f, 0.0f), //Left 2 - 14
                        Vertex.of(-1f, -1f, -1f, 0f, 1f, -1.0f, 0.0f, 0.0f), //Left 3 - 15
                        Vertex.of( 1f, -1f, -1f, 0f, 0f, 0.0f, -1.0f, 0.0f), //Bottom 0 - 16
                        Vertex.of(-1f, -1f, -1f, 1f, 0f, 0.0f, -1.0f, 0.0f), //Bottom 1 - 17
                        Vertex.of(-1f, -1f,  1f, 1f, 1f, 0.0f, -1.0f, 0.0f), //Bottom 2 - 18
                        Vertex.of( 1f, -1f,  1f, 0f, 1f, 0.0f, -1.0f, 0.0f), //Bottom 3 - 19
                        Vertex.of(-1f,  1f, -1f, 0f, 0f, 0.0f, 1.0f, 0.0f), //Top 0 - 20
                        Vertex.of( 1f,  1f, -1f, 1f, 0f, 0.0f, 1.0f, 0.0f), //Top 1 - 21
                        Vertex.of( 1f,  1f,  1f, 1f, 1f, 0.0f, 1.0f, 0.0f), //Top 2 - 22
                        Vertex.of(-1f,  1f,  1f, 0f, 1f, 0.0f, 1.0f, 0.0f), //Top 3 - 23
                },
                new int[] {
                    0, 1, 3, 3, 1, 2,
                    4, 5, 7, 7, 5, 6,
                    8, 9, 11, 11, 9, 10,
                    12, 13, 15, 15, 13, 14,
                    16, 17, 19, 19, 17, 18,
                    20, 21, 23, 23, 21, 22,
                },
                material
        );
    }


}

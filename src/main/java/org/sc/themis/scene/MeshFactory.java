package org.sc.themis.scene;

import org.sc.themis.renderer.resource.staging.VkStagingResourceAllocator;
import org.sc.themis.shared.exception.ThemisException;

public class MeshFactory {

    public Mesh createTriangle(VkStagingResourceAllocator allocator, String identifier ) throws ThemisException {

        Mesh mesh = new Mesh( allocator, identifier );

        Vertex [] vertices = new Vertex[] {
            Vertex.of( -1.0f, -1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -1.0f ),
            Vertex.of(  1.0f, -1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -1.0f ),
            Vertex.of(  0.0f,  1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -1.0f )
        };

        int [] indices = new int[] {
            0, 1, 2
        };

        mesh.setup( vertices, indices );

        return mesh;


    }


}

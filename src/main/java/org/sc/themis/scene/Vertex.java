package org.sc.themis.scene;

import org.joml.Vector2f;
import org.joml.Vector3f;

public record Vertex(Vector3f position, Vector3f normale, Vector2f texture, Vector3f tangent, Vector3f bitangent) {

    public final static int COMPONENTS = 3 + 3 + 2 + 3 + 3;

    public static Vertex of( Vertex vertex ) {
        return new Vertex( vertex.position, vertex.normale, vertex.texture, vertex.tangent, vertex.bitangent );
    }

    public static Vertex of(Vector3f position, Vector3f normale, Vector2f texture, Vector3f tanget, Vector3f bitangent ) {
        return new Vertex( position, normale, texture, tanget, bitangent );
    }

    public static Vertex of(float x, float y, float z ) {
        return new Vertex( new Vector3f(x, y, z ), new Vector3f(), new Vector2f(), new Vector3f(), new Vector3f() );
    }

    public static Vertex of(float x, float y, float z, float u, float v ) {
        return new Vertex( new Vector3f(x, y, z ), new Vector3f(), new Vector2f(u,v), new Vector3f(), new Vector3f() );
    }

    public static Vertex of(float x, float y, float z, float u, float v, float nx, float ny, float nz ) {
        return new Vertex( new Vector3f(x, y, z ), new Vector3f(nx, ny, nz ), new Vector2f(u,v), new Vector3f(), new Vector3f() );
    }

    public Vertex(Vector3f position, Vector3f normale, Vector2f texture, Vector3f tangent, Vector3f bitangent ) {
        this.position = new Vector3f( position );
        this.normale  = new Vector3f(  normale );
        this.texture  = new Vector2f( texture );
        this.tangent   = new Vector3f(  tangent );
        this.bitangent = new Vector3f(  bitangent );
    }

}

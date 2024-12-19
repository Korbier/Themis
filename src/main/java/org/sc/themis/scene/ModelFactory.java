package org.sc.themis.scene;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.*;
import org.sc.themis.renderer.resource.staging.VkStagingResourceAllocator;
import org.sc.themis.scene.exception.ModelFileNotFoundException;
import org.sc.themis.shared.assertion.Assertions;
import org.sc.themis.shared.exception.ThemisException;
import static org.lwjgl.assimp.Assimp.*;

import java.nio.IntBuffer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ModelFactory {

    final private static int flags =
              aiProcess_GenSmoothNormals | aiProcess_JoinIdenticalVertices | aiProcess_Triangulate
            | aiProcess_FixInfacingNormals | aiProcess_CalcTangentSpace | aiProcess_PreTransformVertices;


    public Model create( String identifier, Mesh [] meshes, Material [] materials ) {
        return new Model( identifier, meshes, materials );
    }

    public Model create( String identifier, Mesh ... meshes ) {
        return new Model( identifier, meshes, new Material[0] );
    }

    public Model create(String identifier, VkStagingResourceAllocator allocator, Path modelFile ) throws ThemisException {

        Assertions.isTrue( modelFile.toFile()::exists, new ModelFileNotFoundException( modelFile ) );

        try ( AIScene scene = aiImportFile( modelFile.toAbsolutePath().toString(), flags ) ) {

            Path workdir = modelFile.getParent();

            Material [] materials = loadMaterials( allocator, identifier, scene, workdir );
            Mesh [] meshes = loadMeshs( allocator, identifier, scene, materials );

            return new Model( identifier, meshes, materials );

        }

    }

    private String getMaterialIdentifier( String modelIdentifier, int inc ) {
        return modelIdentifier + ".material." + inc;
    }

    private String getMeshIdentifier( String modelIdentifier, int inc ) {
        return modelIdentifier + ".mesh." + inc;
    }

    private Mesh[] loadMeshs( VkStagingResourceAllocator allocator, String modelIdentifier, AIScene scene, Material[] materials ) throws ThemisException {

        PointerBuffer aiMeshesBuffer = scene.mMeshes();
        int numMeshes = scene.mNumMeshes();

        Mesh [] meshes = new Mesh[numMeshes];

        for ( int i=0; i<numMeshes; i++) {

            AIMesh aiMesh = AIMesh.create( aiMeshesBuffer.get(i) );

            Vertex [] vertices = getVertices( aiMesh );
            int [] indices = getIndices( aiMesh );

            meshes[i] = new Mesh( allocator, getMeshIdentifier(modelIdentifier, i) );
            meshes[i].set( vertices, indices, getMaterialIdentifier(modelIdentifier, aiMesh.mMaterialIndex()) );

        }

        return meshes;

    }


    private Vertex[] getVertices( AIMesh aiMesh ) {

        List<Vertex> vertices = new ArrayList<>();

        AIVector3D.Buffer aiVertices   = aiMesh.mVertices();
        AIVector3D.Buffer aiNormals    = aiMesh.mNormals();
        AIVector3D.Buffer aiTextCoords = aiMesh.mTextureCoords(0);
        AIVector3D.Buffer aiTangents   = aiMesh.mTangents();
        AIVector3D.Buffer aiBitangents = aiMesh.mBitangents();

        while ( aiVertices.remaining() > 0 ) {

            AIVector3D aiVertex = aiVertices.get();
            AIVector3D textCoord = aiTextCoords != null ? aiTextCoords.get() : null;
            AIVector3D normal    = aiNormals != null    ? aiNormals.get()    : null;
            AIVector3D tangent   = aiTangents != null   ? aiTangents.get()   : null;
            AIVector3D bitangent = aiBitangents != null ? aiBitangents.get() : null;

            vertices.add(
                    Vertex.of(
                            new Vector3f( aiVertex.x(),aiVertex.y(),aiVertex.z() ),
                            normal != null ? new Vector3f( normal.x(),normal.y(),normal.z() ) : new Vector3f(),
                            textCoord != null ? new Vector2f( textCoord.x(), 1 - textCoord.y() ) : new Vector2f(),
                            tangent != null ? new Vector3f( tangent.x(),tangent.y(),tangent.z() ) : new Vector3f(),
                            bitangent != null ? new Vector3f( bitangent.x(),bitangent.y(),bitangent.z() ) : new Vector3f()
                    )
            );

        }

        return vertices.toArray( new Vertex[0] );

    }


    protected int [] getIndices(AIMesh aiMesh) {

        List<Integer> indices  = new ArrayList<>();
        int           numFaces = aiMesh.mNumFaces();
        AIFace.Buffer aiFaces  = aiMesh.mFaces();

        for (int i = 0; i < numFaces; i++) {

            AIFace    aiFace = aiFaces.get(i);
            IntBuffer buffer = aiFace.mIndices();

            while (buffer.remaining() > 0) {
                indices.add(buffer.get());
            }

        }

        return indices.stream().mapToInt(Integer::intValue).toArray();

    }

    private Material[] loadMaterials( VkStagingResourceAllocator allocator, String modelIdentifier, AIScene scene, Path workdir) {

        PointerBuffer aiMaterialsBuffer = scene.mMaterials();
        int numMaterials = scene.mNumMaterials();

        Material[] materials = new Material[numMaterials];

        for (int i = 0; i < numMaterials; i++) {

            Material   material   = new Material( allocator, getMaterialIdentifier( modelIdentifier, i ) );
            AIMaterial aiMaterial = AIMaterial.create(aiMaterialsBuffer.get(i));

            setColor( aiMaterial, AI_MATKEY_BASE_COLOR, material, MaterialAttribute.Color.BASE );
            setColor( aiMaterial, AI_MATKEY_COLOR_DIFFUSE, material, MaterialAttribute.Color.DIFFUSE );
            setColor( aiMaterial, AI_MATKEY_COLOR_EMISSIVE, material, MaterialAttribute.Color.EMISSIVE );
            setColor( aiMaterial, AI_MATKEY_COLOR_SPECULAR, material, MaterialAttribute.Color.SPECULAR );
            setColor( aiMaterial, AI_MATKEY_SHININESS, material, MaterialAttribute.Color.SHININESS );

            materials[i] = material;

        }

        return materials;

    }

    private void setColor( AIMaterial assimpMaterial, String assimpAttr, Material material, MaterialAttribute attribute ) {
        AIColor4D  workColor = AIColor4D.create();
        aiGetMaterialColor( assimpMaterial, assimpAttr, 0, 0, workColor );
        Vector4f color =  new Vector4f( workColor.r(), workColor.g(), workColor.b(), workColor.a() );
        material.setColor( MaterialAttribute.Color.BASE, color );
    }


}

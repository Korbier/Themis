package org.sc.themis.scene.factory;

import org.jboss.logging.Logger;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.*;
import org.lwjgl.system.MemoryStack;
import org.sc.themis.renderer.resource.staging.VkStagingImage;
import org.sc.themis.renderer.resource.staging.VkStagingResourceAllocator;
import org.sc.themis.scene.Mesh;
import org.sc.themis.scene.Model;
import org.sc.themis.scene.Vertex;
import org.sc.themis.scene.exception.ModelFileNotFoundException;
import org.sc.themis.renderer.material.MaterialProperties;
import org.sc.themis.renderer.material.MaterialProperty;
import org.sc.themis.shared.assertion.Assertions;
import org.sc.themis.shared.exception.ThemisException;
import org.sc.themis.shared.resource.Image;

import static org.lwjgl.assimp.Assimp.*;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R8G8B8A8_SRGB;

import java.nio.IntBuffer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ModelFactory {

    private static final org.jboss.logging.Logger LOG = Logger.getLogger(ModelFactory.class);

    final private static int flags =
              aiProcess_GenSmoothNormals | aiProcess_JoinIdenticalVertices | aiProcess_Triangulate
            | aiProcess_FixInfacingNormals | aiProcess_CalcTangentSpace | aiProcess_PreTransformVertices;

    public Model create(String identifier, Mesh... meshes ) {
        return new Model( identifier, meshes);
    }

    public Model create(String identifier, VkStagingResourceAllocator allocator, Path modelFile ) throws ThemisException {

        LOG.infof("Loading model from file %s", modelFile.toAbsolutePath().toString() );

        Assertions.isTrue( modelFile.toFile()::exists, new ModelFileNotFoundException( modelFile ) );

        try ( AIScene scene = aiImportFile( modelFile.toAbsolutePath().toString(), flags ) ) {

            List<MaterialProperties> properties = loadProperties( scene, allocator, modelFile.getParent() );
            Mesh [] meshes = loadMeshs( allocator, identifier, scene, properties );

            return new Model( identifier, meshes );

        }

    }

    private String getMeshIdentifier( String modelIdentifier, int inc ) {
        return modelIdentifier + ".mesh." + inc;
    }

    private Mesh[] loadMeshs( VkStagingResourceAllocator allocator, String modelIdentifier, AIScene scene, List<MaterialProperties> properties ) throws ThemisException {

        PointerBuffer aiMeshesBuffer = scene.mMeshes();
        int numMeshes = scene.mNumMeshes();

        Mesh [] meshes = new Mesh[numMeshes];

        for ( int i=0; i<numMeshes; i++) {

            AIMesh aiMesh = AIMesh.create( aiMeshesBuffer.get(i) );

            Vertex[] vertices = getVertices( aiMesh );
            int [] indices = getIndices( aiMesh );

            meshes[i] = new Mesh( allocator, getMeshIdentifier(modelIdentifier, i) );
            meshes[i].set( vertices, indices );
            meshes[i].setProperties( properties.get( aiMesh.mMaterialIndex() ) );

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

    private List<MaterialProperties> loadProperties( AIScene scene, VkStagingResourceAllocator allocator, Path workdir ) throws ThemisException {

        List<MaterialProperties> result = new ArrayList<>();

        PointerBuffer aiMaterialsBuffer = scene.mMaterials();
        int numMaterials = scene.mNumMaterials();

        for (int i = 0; i < numMaterials; i++) {

            LOG.infof("Loading material #%d", i );

            AIMaterial aiMaterial = AIMaterial.create(aiMaterialsBuffer.get(i));
            MaterialProperties properties = new MaterialProperties();

            setColor( aiMaterial, AI_MATKEY_BASE_COLOR, properties, MaterialProperty.Color.BASE );
            setColor( aiMaterial, AI_MATKEY_COLOR_DIFFUSE, properties, MaterialProperty.Color.DIFFUSE );
            setColor( aiMaterial, AI_MATKEY_COLOR_EMISSIVE, properties, MaterialProperty.Color.EMISSIVE );
            setColor( aiMaterial, AI_MATKEY_COLOR_SPECULAR, properties, MaterialProperty.Color.SPECULAR );
            setFloat( aiMaterial, AI_MATKEY_SHININESS, properties, MaterialProperty.Property.SHININESS );

            setImage( workdir, allocator, aiMaterial, aiTextureType_BASE_COLOR, properties, MaterialProperty.Texture.BASE );
            setImage( workdir, allocator, aiMaterial, aiTextureType_NORMALS, properties, MaterialProperty.Texture.NORMALS );

            result.add( properties );

        }

        return result;

    }

    private void setImage(Path workdir, VkStagingResourceAllocator allocator, AIMaterial aiMaterial, int assimpAttr, MaterialProperties properties, MaterialProperty<VkStagingImage> property) throws ThemisException {

        String path = getTexturePath( workdir, aiMaterial, assimpAttr );

        if ( path != null ) {
            LOG.infof("Loading texture property %s (%s)", property.getName(), path );
            VkStagingImage stgImage = allocator.allocateImage( VK_FORMAT_R8G8B8A8_SRGB);
            stgImage.load( Image.of( path ) );
            properties.put( property, stgImage );
        }

    }

    private String getTexturePath(Path workdir, AIMaterial aiMaterial, int assimpAttr) {

        try (MemoryStack stack = MemoryStack.stackPush() ) {

            AIString aiTexturePath = AIString.calloc(stack);
            aiGetMaterialTexture(aiMaterial, assimpAttr, 0, aiTexturePath, (IntBuffer) null, null, null, null, null, null);

            String texturePath = aiTexturePath.dataString();

            if ( !texturePath.isBlank() ) {
                return workdir.resolve( texturePath ).toAbsolutePath().toString();
            } else {
                return null;
            }

        }

    }

    private void setColor(AIMaterial assimpMaterial, String assimpAttr, Map<MaterialProperty<?>, Object> properties, MaterialProperty<Vector4f> property ) {

        AIColor4D  workColor = AIColor4D.create();
        aiGetMaterialColor( assimpMaterial, assimpAttr, 0, 0, workColor );

        if ( workColor.r() != 0.0f || workColor.g() != 0.0f && workColor.b() != 0.0f || workColor.a() != 0.0f ) {
            Vector4f color = new Vector4f(workColor.r(), workColor.g(), workColor.b(), workColor.a());
            LOG.infof("Loading color property %s (%s)", property.getName(), color );
            properties.put(property, color);
        }

    }

    private void setFloat(AIMaterial assimpMaterial, String assimpAttr, Map<MaterialProperty<?>, Object> properties, MaterialProperty<Float> property ) {
        AIColor4D  workColor = AIColor4D.create();
        aiGetMaterialColor( assimpMaterial, assimpAttr, 0, 0, workColor );
        properties.put( property, workColor.r() );
    }

}

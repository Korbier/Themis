package org.sc.themis.scene.factory;

import static org.lwjgl.assimp.Assimp.*;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R8G8B8A8_SRGB;

import java.nio.IntBuffer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.AIColor4D;
import org.lwjgl.assimp.AIFace;
import org.lwjgl.assimp.AIMaterial;
import org.lwjgl.assimp.AIMesh;
import org.lwjgl.assimp.AIScene;
import org.lwjgl.assimp.AIString;
import org.lwjgl.assimp.AIVector3D;
import org.lwjgl.system.MemoryStack;
import org.sc.themis.renderer.material.MaterialManager;
import org.sc.themis.renderer.resource.material.Material;
import org.sc.themis.renderer.resource.material.MaterialProperties;
import org.sc.themis.renderer.resource.material.MaterialProperty;
import org.sc.themis.renderer.base.resource.staging.VkStagingImage;
import org.sc.themis.renderer.base.resource.staging.VkStagingResourceAllocator;
import org.sc.themis.renderer.resource.model.Mesh;
import org.sc.themis.renderer.resource.model.Model;
import org.sc.themis.renderer.resource.model.Vertex;
import org.sc.themis.scene.exception.ModelFileNotFoundException;
import org.sc.themis.shared.assertion.Assertions;
import org.sc.themis.shared.exception.ThemisException;
import org.sc.themis.renderer.resource.image.Image;
import org.sc.themis.renderer.resource.image.ImageResourceDescriptor;
import org.sc.themis.renderer.resource.ResourceEnum;
import org.sc.themis.renderer.resource.ResourceLoader;
import org.slf4j.LoggerFactory;

public class ModelFactory {

  private static final org.slf4j.Logger logger = LoggerFactory.getLogger(ModelFactory.class);

  private static final int flags =
      aiProcess_GenSmoothNormals
          | aiProcess_JoinIdenticalVertices
          | aiProcess_Triangulate
          | aiProcess_FixInfacingNormals
          | aiProcess_CalcTangentSpace
          | aiProcess_PreTransformVertices;

  private static final ModelFactory instance = new ModelFactory();

  public Model create(String identifier, Mesh... meshes) {
    return new Model(identifier, meshes);
  }

  public static Model create(String identifier, VkStagingResourceAllocator allocator, Path modelFile) throws ThemisException {
    return instance.doCreate(identifier, allocator, null, modelFile);
  }

  public static Model create(String identifier, VkStagingResourceAllocator allocator, MaterialManager manager, Path modelFile) throws ThemisException {
    return instance.doCreate(identifier, allocator, manager, modelFile);
  }

  public Model doCreate(String identifier, VkStagingResourceAllocator allocator, MaterialManager manager, Path modelFile) throws ThemisException {

    Assertions.isTrue(modelFile.toFile()::exists, new ModelFileNotFoundException(modelFile));

    try (AIScene scene = aiImportFile(modelFile.toAbsolutePath().toString(), flags)) {
      List<Material> properties = loadProperties(identifier, scene, allocator, manager, modelFile.getParent());
      Mesh[] meshes = loadMeshs(allocator, identifier, scene, properties);
      return new Model(identifier, meshes);
    }

  }

  private String getMeshIdentifier(String modelIdentifier, int inc) {
    return modelIdentifier + ".mesh." + inc;
  }

  public Mesh[] loadMeshs(VkStagingResourceAllocator allocator, String modelIdentifier, AIScene scene, List<Material> properties)
      throws ThemisException {

    PointerBuffer aiMeshesBuffer = scene.mMeshes();
    int numMeshes = scene.mNumMeshes();

    Mesh[] meshes = new Mesh[numMeshes];

    for (int i = 0; i < numMeshes; i++) {

      AIMesh aiMesh = AIMesh.create(aiMeshesBuffer.get(i));

      Vertex[] vertices = getVertices(aiMesh);
      int[] indices = getIndices(aiMesh);

      meshes[i] = new Mesh(allocator, getMeshIdentifier(modelIdentifier, i));
      meshes[i].set(vertices, indices);
      meshes[i].setProperties(properties.get(aiMesh.mMaterialIndex()));

    }

    return meshes;
  }

  private Vertex[] getVertices(AIMesh aiMesh) {

    Vector3f min = new Vector3f(Float.MAX_VALUE);
    Vector3f max = new Vector3f(Float.MIN_VALUE);

    List<Vertex> vertices = new ArrayList<>();

    AIVector3D.Buffer aiVertices = aiMesh.mVertices();
    AIVector3D.Buffer aiNormals = aiMesh.mNormals();
    AIVector3D.Buffer aiTextCoords = aiMesh.mTextureCoords(0);
    AIVector3D.Buffer aiTangents = aiMesh.mTangents();
    AIVector3D.Buffer aiBitangents = aiMesh.mBitangents();

    while (aiVertices.remaining() > 0) {

      AIVector3D aiVertex = aiVertices.get();
      AIVector3D textCoord = aiTextCoords != null ? aiTextCoords.get() : null;
      AIVector3D normal = aiNormals != null ? aiNormals.get() : null;
      AIVector3D tangent = aiTangents != null ? aiTangents.get() : null;
      AIVector3D bitangent = aiBitangents != null ? aiBitangents.get() : null;

      Vertex v =  Vertex.of(
          new Vector3f(aiVertex.x(), aiVertex.y(), aiVertex.z()),
          normal != null ? new Vector3f(normal.x(), normal.y(), normal.z()) : new Vector3f(),
          textCoord != null ? new Vector2f(textCoord.x(), 1 - textCoord.y()) : new Vector2f(),
          tangent != null ? new Vector3f(tangent.x(), tangent.y(), tangent.z()) : new Vector3f(),
          bitangent != null ? new Vector3f(bitangent.x(), bitangent.y(), bitangent.z()) : new Vector3f()
      );

      min.x = Float.min(min.x, v.position().x);
      min.y = Float.min(min.y, v.position().y);
      min.z = Float.min(min.z, v.position().z);

      max.x = Float.max(max.x, v.position().x);
      max.y = Float.max(max.y, v.position().y);
      max.z = Float.max(max.z, v.position().z);

      vertices.add(v);

    }

    System.out.println("MESH MIN ==> " + display(min));
    System.out.println("MESH MAX ==> " + display(max));

    return vertices.toArray(Vertex[]::new);

  }

  private String display(Vector3f v) {
    return "V(%.2f, %.2f, %.2f)".formatted(v.x, v.y, v.z);
  }

  protected int[] getIndices(AIMesh aiMesh) {

    List<Integer> indices = new ArrayList<>();
    int numFaces = aiMesh.mNumFaces();
    AIFace.Buffer aiFaces = aiMesh.mFaces();

    for (int i = 0; i < numFaces; i++) {

      AIFace aiFace = aiFaces.get(i);
      IntBuffer buffer = aiFace.mIndices();

      while (buffer.remaining() > 0) {
        indices.add(buffer.get());
      }
    }

    return indices.stream().mapToInt(Integer::intValue).toArray();

  }

  private List<Material> loadProperties( String identifier, AIScene scene, VkStagingResourceAllocator allocator, MaterialManager manager, Path workdir) throws ThemisException {

    List<Material> result = new ArrayList<>();

    PointerBuffer aiMaterialsBuffer = scene.mMaterials();
    int numMaterials = scene.mNumMaterials();

    for (int i = 0; i < numMaterials; i++) {

      logger.info("Loading material #{}", i);

      AIMaterial aiMaterial = AIMaterial.create(aiMaterialsBuffer.get(i));
      Material properties = new Material( identifier + "#" + i);

      setColor(aiMaterial, AI_MATKEY_BASE_COLOR, properties, MaterialProperties.COLOR_AMBIENT);
      setColor(aiMaterial, AI_MATKEY_COLOR_DIFFUSE, properties, MaterialProperties.COLOR_DIFFUSE);
      setColor(aiMaterial, AI_MATKEY_COLOR_EMISSIVE, properties, MaterialProperties.COLOR_EMISSIVE);
      setColor(aiMaterial, AI_MATKEY_COLOR_SPECULAR, properties, MaterialProperties.COLOR_SPECULAR);
      setFloat(aiMaterial, AI_MATKEY_SHININESS, properties, MaterialProperties.FLOAT_SHININESS);

      setImage(workdir, allocator, aiMaterial, aiTextureType_BASE_COLOR, properties, MaterialProperties.TEXTURE_ALBEDO);
      setImage( workdir, allocator, aiMaterial, aiTextureType_NORMALS, properties, MaterialProperties.TEXTURE_NORMAL);

      if (manager != null) {
        manager.addMaterials(properties);
      }

      result.add(properties);

    }

    return result;
  }

  private void
  setImage(
      Path workdir,
      VkStagingResourceAllocator allocator,
      AIMaterial aiMaterial,
      int assimpAttr,
      Material properties,
      MaterialProperty<VkStagingImage> property
  ) throws ThemisException {

    String path = getTexturePath(aiMaterial, assimpAttr);

    if (path != null) {
      logger.info("Loading texture property {} ({})", property.getName(), path);
      VkStagingImage stgImage = allocator.allocateImage(property.getImageFormat());
      Image image = ResourceLoader.get().get(ResourceEnum.IMAGE, ImageResourceDescriptor.of(path), workdir);
      stgImage.load(image);
      properties.put(property, stgImage);
    }

  }

  private String getTexturePath(AIMaterial aiMaterial, int assimpAttr) {

    try (MemoryStack stack = MemoryStack.stackPush()) {

      AIString aiTexturePath = AIString.calloc(stack);
      aiGetMaterialTexture(aiMaterial, assimpAttr, 0, aiTexturePath, (IntBuffer) null, null, null, null, null, null);

      String texturePath = aiTexturePath.dataString();

      if (!texturePath.isBlank()) {
        return texturePath;
      } else {
        return null;
      }
    }
  }

  private void setColor(AIMaterial assimpMaterial, String assimpAttr, Map<MaterialProperty<?>, Object> properties, MaterialProperty<Vector4f> property) {

    AIColor4D workColor = AIColor4D.create();
    aiGetMaterialColor(assimpMaterial, assimpAttr, 0, 0, workColor);

    if (workColor.r() != 0.0f || workColor.g() != 0.0f && workColor.b() != 0.0f || workColor.a() != 0.0f) {
      Vector4f color = new Vector4f(workColor.r(), workColor.g(), workColor.b(), workColor.a());
      logger.info("Loading color property {} ({})", property.getName(), color);
      properties.put(property, color);
    }

  }

  private void setFloat(AIMaterial assimpMaterial, String assimpAttr, Map<MaterialProperty<?>, Object> properties, MaterialProperty<Float> property) {
    AIColor4D workColor = AIColor4D.create();
    aiGetMaterialColor(assimpMaterial, assimpAttr, 0, 0, workColor);
    properties.put(property, workColor.r());
  }

}

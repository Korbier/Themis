package org.sc.themis.renderer.resource.material;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.sc.themis.renderer.base.resource.staging.VkStagingImage;
import org.sc.themis.renderer.resource.ResourceEnum;
import org.sc.themis.renderer.resource.ResourceLoader;
import org.sc.themis.renderer.resource.image.Image;
import org.sc.themis.renderer.resource.image.ImageResourceDescriptor;
import org.sc.themis.shared.exception.ThemisException;
import org.sc.themis.shared.function.BiFunctionWithException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

public class MaterialResourceLoader implements BiFunctionWithException<Path, MaterialResourceDescriptor, Material> {

  private final ObjectMapper oMapper = new ObjectMapper();

  @Override
  public Material apply(Path path, MaterialResourceDescriptor descriptor) throws ThemisException {

    try {

      MaterialFile file = this.oMapper.readValue(path.toFile(), MaterialFile.class);

      Material material = new Material(file.name, file.author, file.source);
      Path directory = path.getParent().resolve(file.dir);

      //Default texture
      VkStagingImage stgImage = descriptor.getAllocator().allocateImage(MaterialProperties.TEXTURE_NORMAL.getImageFormat());
      Image image = Image.of(1.0f,1.0f,1.0f,1.0f);
      stgImage.load(image);

      for (String key : MaterialProperties.keys()) {

        String keyInFile = key.replace("texture.", "");
        MaterialProperty<?> materialProperty = MaterialProperties.get(key);

        if (file.properties.containsKey(keyInFile)) {
          String value = file.properties.get(keyInFile);

          VkStagingImage stgTextureImage = descriptor.getAllocator().allocateImage(materialProperty.getImageFormat());
          Image textureImage = ResourceLoader.get().get(ResourceEnum.IMAGE, ImageResourceDescriptor.of(value), directory);
          stgTextureImage.load(textureImage);

          material.put(materialProperty, stgTextureImage);

        } else {
          material.put(materialProperty, stgImage);
        }

      }

      return material;

    } catch (IOException e) {
      throw new ThemisException("An exception was thrown while loading a material", e);
    }

  }

  public static class MaterialFile {
    public String name;
    public String author;
    public String source;
    public String dir;
    public Map<String, String> properties;
  }

}

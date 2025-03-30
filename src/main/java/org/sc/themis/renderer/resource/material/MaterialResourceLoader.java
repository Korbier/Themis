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

import static org.lwjgl.vulkan.VK10.VK_FORMAT_R8G8B8A8_SRGB;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R8G8B8A8_UNORM;

public class MaterialResourceLoader implements BiFunctionWithException<Path, MaterialResourceDescriptor, Material> {

  private final ObjectMapper oMapper = new ObjectMapper();

  @Override
  public Material apply(Path path, MaterialResourceDescriptor descriptor) throws ThemisException {

    try {

      MaterialFile file = this.oMapper.readValue(path.toFile(), MaterialFile.class);

      Material material = new Material(file.name, file.author, file.source);
      Path directory = path.getParent().resolve(file.dir);

      for (Map.Entry<String,String> entry : file.properties.entrySet()) {

        String key = entry.getKey();
        String value = entry.getValue();

        MaterialProperty<VkStagingImage> materialProperty = MaterialProperties.get("texture." + key);

        VkStagingImage stgImage = descriptor.getAllocator().allocateImage(materialProperty.getImageFormat());
        Image image = ResourceLoader.get().get(ResourceEnum.IMAGE, ImageResourceDescriptor.of(value), directory);
        stgImage.load(image);

        material.put(materialProperty, stgImage);

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

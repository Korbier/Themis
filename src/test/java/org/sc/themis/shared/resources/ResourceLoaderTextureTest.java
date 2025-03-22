package org.sc.themis.shared.resources;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sc.TestWithConfiguration;
import org.sc.themis.shared.exception.ThemisException;
import org.sc.themis.shared.resource.Image;
import org.sc.themis.shared.resource.ResourceLoader;
import org.sc.themis.shared.resource.exception.ResourceFileNotFoundException;
import org.sc.themis.shared.resource.loader.ResourceEnum;
import org.sc.themis.shared.resource.loader.descriptor.TextureResourceDescriptor;

public class ResourceLoaderTextureTest extends TestWithConfiguration {

  @BeforeEach
  public void configureResourceLoader() {
    ResourceLoader.get().apply(getConfiguration());
  }

  @Test
  @DisplayName("Load texture - texture correctly loaded")
  void testLoadTexture_01() throws ThemisException {
    //Given
    //When
    Image image = ResourceLoader.get().get(ResourceEnum.TEXTURE, TextureResourceDescriptor.of("vulkan.png"));
    //Then
    Assertions.assertEquals(300, image.getWidth());
    Assertions.assertEquals(300, image.getHeight());
    Assertions.assertNotNull(image.getBuffer());
  }


  @Test
  @DisplayName("Load texture - font file does not exist")
  void testLoadTexture_02() {
    //Given
    //When
    //Then
    Assertions.assertThrows(ResourceFileNotFoundException.class, () -> ResourceLoader.get().get(ResourceEnum.TEXTURE, TextureResourceDescriptor.of("blabla.png")));
  }

}

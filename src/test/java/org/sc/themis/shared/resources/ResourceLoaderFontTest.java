package org.sc.themis.shared.resources;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sc.TestWithConfiguration;
import org.sc.themis.shared.exception.ThemisException;
import org.sc.themis.renderer.resource.font.Font;
import org.sc.themis.renderer.resource.ResourceLoader;
import org.sc.themis.renderer.resource.base.exception.ResourceFileNotFoundException;
import org.sc.themis.renderer.resource.ResourceEnum;
import org.sc.themis.renderer.resource.font.FontResourceDescriptor;

import java.nio.file.Path;

public class ResourceLoaderFontTest extends TestWithConfiguration {

  @BeforeEach
  public void configureResourceLoader() {
    ResourceLoader.get().apply(getConfiguration());
  }

  @Test
  @DisplayName("Load font [sdf] - font correctly loaded")
  void testLoadFontSdf_01() throws ThemisException {

    //Given
    int size = 12;
    float width = 0.2f;
    float edge = 0.1f;

    //When
    Font font = ResourceLoader.get().get(ResourceEnum.FONT, FontResourceDescriptor.sdf(Path.of("CenturyGothic.ttf"), size, width, edge) );

    //Then
    Assertions.assertTrue(font.isSdfFont());
    Assertions.assertEquals(size, font.getFontSize());
    Assertions.assertEquals(width, font.getSdfWidth());
    Assertions.assertEquals(edge, font.getSdfEdge());
    Assertions.assertNotNull(font.getTexture());

  }

  @Test
  @DisplayName("Load font [normal] - font correctly loaded")
  void testLoadFontNormal_02() throws ThemisException {

    //Given
    int size = 12;

    //When
    Font font = ResourceLoader.get().get(ResourceEnum.FONT, FontResourceDescriptor.normal(Path.of("CenturyGothic.ttf"), size) );

    //Then
    Assertions.assertFalse(font.isSdfFont());
    Assertions.assertEquals(size, font.getFontSize());
    Assertions.assertNotNull(font.getTexture());

  }

  @Test
  @DisplayName("Load font [sdf] - font file does not exist")
  void testLoadFontSdf_03(){

    //Given
    int size = 12;
    float width = 0.2f;
    float edge = 0.1f;

    //When

    //Then
    Assertions.assertThrows(ResourceFileNotFoundException.class, () -> ResourceLoader.get().get(ResourceEnum.FONT, FontResourceDescriptor.sdf(Path.of("blablabla.ttf"), size, width, edge)));

  }

}

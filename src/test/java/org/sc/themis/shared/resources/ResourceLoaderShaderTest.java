package org.sc.themis.shared.resources;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sc.TestWithConfiguration;
import org.sc.themis.shared.exception.ThemisException;
import org.sc.themis.shared.resource.ResourceLoader;
import org.sc.themis.shared.resource.exception.ResourceFileNotFoundException;
import org.sc.themis.shared.resource.loader.ResourceEnum;
import org.sc.themis.shared.resource.loader.descriptor.ShaderResourceDescriptor;

public class ResourceLoaderShaderTest extends TestWithConfiguration {

  @BeforeEach
  public void configureResourceLoader() {
    ResourceLoader.get().apply(getConfiguration());
  }

  @Test
  @DisplayName("Load shader source - shader correctly loaded")
  void testLoadShader_01() throws ThemisException {
    //Given
    //When
    byte [] shaderSrc = ResourceLoader.get().get(ResourceEnum.SHADER, ShaderResourceDescriptor.of("shader.glsl"));
    //Then
    Assertions.assertNotNull(shaderSrc);
  }


  @Test
  @DisplayName("Load shader source - shader file does not exist")
  void testLoadShader_02() {
    //Given
    //When
    //Then
    Assertions.assertThrows(ResourceFileNotFoundException.class, () -> ResourceLoader.get().get(ResourceEnum.SHADER, ShaderResourceDescriptor.of("blabla.glsl")));
  }

}

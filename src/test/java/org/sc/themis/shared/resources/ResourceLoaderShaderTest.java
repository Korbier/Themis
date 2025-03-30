package org.sc.themis.shared.resources;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sc.TestWithConfiguration;
import org.sc.themis.renderer.resource.shader.ShaderSource;
import org.sc.themis.shared.exception.ThemisException;
import org.sc.themis.renderer.resource.ResourceLoader;
import org.sc.themis.renderer.resource.base.exception.ResourceFileNotFoundException;
import org.sc.themis.renderer.resource.ResourceEnum;
import org.sc.themis.renderer.resource.shader.ShaderSourceResourceDescriptor;

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
    ShaderSource shaderSrc = ResourceLoader.get().get(ResourceEnum.SHADER, ShaderSourceResourceDescriptor.of("shader.glsl"));
    //Then
    Assertions.assertNotNull(shaderSrc);
    Assertions.assertNotNull(shaderSrc.getContent());
  }


  @Test
  @DisplayName("Load shader source - shader file does not exist")
  void testLoadShader_02() {
    //Given
    //When
    //Then
    Assertions.assertThrows(ResourceFileNotFoundException.class, () -> ResourceLoader.get().get(ResourceEnum.SHADER, ShaderSourceResourceDescriptor.of("blabla.glsl")));
  }

}

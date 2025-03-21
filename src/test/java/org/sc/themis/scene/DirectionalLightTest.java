package org.sc.themis.scene;

import org.joml.Vector3f;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sc.themis.scene.light.DirectionalLight;
import org.sc.themis.shared.Configuration;

public class DirectionalLightTest {

  Configuration configuration = Configuration.defaultConfiguration();

  @Test
  @DisplayName("New - nominal case")
  public void testConstructor_01() {

    // Given
    Vector3f ambiantColor = new Vector3f(1.0f, 0.0f, 0.0f);
    Vector3f diffuseColor = new Vector3f(0.0f, 1.0f, 0.0f);
    Vector3f specularColor = new Vector3f(0.0f, 0.0f, 1.0f);
    Vector3f direction = new Vector3f(1.0f, 1.0f, 1.0f);

    // When
    DirectionalLight light =
        new DirectionalLight(ambiantColor, diffuseColor, specularColor, direction);

    // Then
    Assertions.assertEquals(ambiantColor, light.getAmbient());
    Assertions.assertEquals(diffuseColor, light.getDiffuse());
    Assertions.assertEquals(specularColor, light.getSpecular());
    Assertions.assertEquals(direction, light.getDirection());
  }

  @Test
  @DisplayName("Scene.add - nominal case")
  public void testSceneAdd_01() {

    // Given
    Vector3f ambiantColor = new Vector3f(1.0f, 0.0f, 0.0f);
    Vector3f diffuseColor = new Vector3f(0.0f, 1.0f, 0.0f);
    Vector3f specularColor = new Vector3f(0.0f, 0.0f, 1.0f);
    Vector3f direction = new Vector3f(1.0f, 1.0f, 1.0f);

    // When
    DirectionalLight light =
        new DirectionalLight(ambiantColor, diffuseColor, specularColor, direction);
    Scene scene = new Scene(configuration);
    scene.add(light);

    // Then
    Assertions.assertEquals(1, scene.getDirectionalLights().size());
    Assertions.assertEquals(
        scene.getDirectionalLights().getFirst().getAmbient(), light.getAmbient());
    Assertions.assertEquals(
        scene.getDirectionalLights().getFirst().getDiffuse(), light.getDiffuse());
    Assertions.assertEquals(
        scene.getDirectionalLights().getFirst().getSpecular(), light.getSpecular());
    Assertions.assertEquals(
        scene.getDirectionalLights().getFirst().getDirection(), light.getDirection());
  }
}

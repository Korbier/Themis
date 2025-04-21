package org.sc.themis.scene;

import org.joml.Vector3f;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sc.TestWithConfiguration;
import org.sc.themis.scene.light.SpotLight;
import org.sc.themis.scene.light.attenuation.Attenuation;
import org.sc.themis.shared.configuration.Configuration;

public class SpotLightTest extends TestWithConfiguration {

  @Test
  @DisplayName("New - nominal case")
  public void testConstructor_01() {

    // Given
    Vector3f ambiantColor = new Vector3f(1.0f, 0.0f, 0.0f);
    Vector3f diffuseColor = new Vector3f(0.0f, 1.0f, 0.0f);
    Vector3f specularColor = new Vector3f(0.0f, 0.0f, 1.0f);
    Vector3f direction = new Vector3f(1.0f, 1.0f, 1.0f);
    Vector3f position = new Vector3f(1.0f, 1.0f, 1.0f);
    Attenuation attenuation = Attenuation.type1(10.0f, 10.0f);
    float innerCutOff = 12.0f;
    float outerCutOff = 20.0f;

    // When
    SpotLight light =
        new SpotLight(
            ambiantColor,
            diffuseColor,
            specularColor,
            direction,
            position,
            attenuation,
            innerCutOff,
            outerCutOff);

    // Then
    Assertions.assertEquals(ambiantColor, light.getAmbient());
    Assertions.assertEquals(diffuseColor, light.getDiffuse());
    Assertions.assertEquals(specularColor, light.getSpecular());
    Assertions.assertEquals(direction, light.getDirection());
    Assertions.assertEquals(position, light.getPosition());
    Assertions.assertEquals(attenuation, light.getAttenuation());
    Assertions.assertEquals(innerCutOff, light.getInnerCutOff());
    Assertions.assertEquals(outerCutOff, light.getOuterCutOff());
  }

  @Test
  @DisplayName("Scene.add - nominal case")
  @Disabled
  public void testSceneAdd_01() {

    // Given
    Vector3f ambiantColor = new Vector3f(1.0f, 0.0f, 0.0f);
    Vector3f diffuseColor = new Vector3f(0.0f, 1.0f, 0.0f);
    Vector3f specularColor = new Vector3f(0.0f, 0.0f, 1.0f);
    Vector3f direction = new Vector3f(1.0f, 1.0f, 1.0f);
    Vector3f position = new Vector3f(1.0f, 1.0f, 1.0f);
    Attenuation attenuation = Attenuation.type1(10.0f, 10.0f);
    float innerCutOff = 12.0f;
    float outerCutOff = 20.0f;

    // When
    SpotLight light =
        new SpotLight(
            ambiantColor,
            diffuseColor,
            specularColor,
            direction,
            position,
            attenuation,
            innerCutOff,
            outerCutOff);
    Scene scene = new Scene(getConfiguration());
    scene.add(light);

    // Then
    Assertions.assertEquals(1, scene.getSpotLights().size());
    Assertions.assertEquals(scene.getSpotLights().getFirst().getAmbient(), light.getAmbient());
    Assertions.assertEquals(scene.getSpotLights().getFirst().getDiffuse(), light.getDiffuse());
    Assertions.assertEquals(scene.getSpotLights().getFirst().getSpecular(), light.getSpecular());
    Assertions.assertEquals(scene.getSpotLights().getFirst().getDirection(), light.getDirection());
    Assertions.assertEquals(scene.getSpotLights().getFirst().getPosition(), light.getPosition());
    Assertions.assertEquals(
        scene.getSpotLights().getFirst().getAttenuation(), light.getAttenuation());
    Assertions.assertEquals(
        scene.getSpotLights().getFirst().getInnerCutOff(), light.getInnerCutOff());
    Assertions.assertEquals(
        scene.getSpotLights().getFirst().getOuterCutOff(), light.getOuterCutOff());
  }
}

package org.sc.themis.scene;

import org.joml.Vector3f;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sc.themis.scene.light.Light;

public class LightTest {

    @Test
    @DisplayName("New - nominal case")
    public void testConstructor_01() {

        //Given
        Vector3f ambiantColor = new Vector3f( 1.0f, 0.0f, 0.0f );
        Vector3f diffuseColor = new Vector3f( 0.0f, 1.0f, 0.0f );
        Vector3f specularColor = new Vector3f( 0.0f, 0.0f, 1.0f );

        //When
        Light light = new TestLight( ambiantColor, diffuseColor, specularColor );

        //Then
        Assertions.assertEquals( ambiantColor, light.getAmbient() );
        Assertions.assertEquals( diffuseColor, light.getDiffuse() );
        Assertions.assertEquals( specularColor, light.getSpecular() );

    }

    private static class TestLight extends Light {

        public TestLight(Vector3f ambient, Vector3f diffuse, Vector3f specular) {
            super(ambient, diffuse, specular);
        }

    }


}

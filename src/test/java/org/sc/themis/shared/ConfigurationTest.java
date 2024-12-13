package org.sc.themis.shared;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sc.themis.Profiles;

@QuarkusTest
@TestProfile(Profiles.TagWithoutUiTest.class)
class ConfigurationTest {

    private final static String APPLICATION_NAME = "Themis Application";
    private final static int APPLICATION_VERSION = 1;

    private final static String ENGINE_NAME = "Themis Engine";
    private final static int ENGINE_VERSION = 1;

    private final static int WINDOW_WIDTH = 800;
    private final static int WINDOW_HEIGHT = 600;
    private final static boolean WINDOW_RESIZABLE = true;
    private final static boolean WINDOW_MAXIMIZED = false;

    private final static float SCENE_PROJECTION_FOV = 60.0f;
    private final static float SCENE_PROJECTION_ZNEAR = 0.1f;
    private final static float SCENE_PROJECTION_ZFAR  = 1400.0f;

    @Inject
    Configuration configuration;

    @Test
    @DisplayName("Get application - nominal case")
    void testGetApplication_01() {
        //Given
        //When
        //Then
        Assertions.assertEquals(APPLICATION_NAME, this.configuration.application().name());
        Assertions.assertEquals(APPLICATION_VERSION, this.configuration.application().version());
    }

    @Test
    @DisplayName("Get engine - nominal case")
    void testGetEngine_01() {
        //Given
        //When
        //Then
        Assertions.assertEquals(ENGINE_NAME, this.configuration.engine().name());
        Assertions.assertEquals(ENGINE_VERSION, this.configuration.engine().version());
    }

    @Test
    @DisplayName("Get window - nominal case")
    void testGetWindow_01() {
        //Given
        //When
        //Then
        Assertions.assertEquals(WINDOW_WIDTH, this.configuration.window().width());
        Assertions.assertEquals(WINDOW_HEIGHT, this.configuration.window().height());
        Assertions.assertEquals(WINDOW_RESIZABLE, this.configuration.window().resizable());
        Assertions.assertEquals(WINDOW_MAXIMIZED, this.configuration.window().maximized());
    }

    @Test
    @DisplayName("Get scene - nominal case")
    void testGetScene_01() {
        //Given
        //When
        //Then
        Assertions.assertEquals(SCENE_PROJECTION_FOV, this.configuration.scene().projection().fov());
        Assertions.assertEquals(SCENE_PROJECTION_ZNEAR, this.configuration.scene().projection().znear());
        Assertions.assertEquals(SCENE_PROJECTION_ZFAR, this.configuration.scene().projection().zfar());
    }
}

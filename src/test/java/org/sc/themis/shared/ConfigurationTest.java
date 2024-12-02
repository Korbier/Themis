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

    @Inject
    Configuration configuration;

    @Test
    @DisplayName("Get application.name - nominal case")
    void testGetApplicationName_01() {
        //Given
        //When
        //Then
        Assertions.assertEquals(APPLICATION_NAME, this.configuration.application().name());
    }

    @Test
    @DisplayName("Get application.version - nominal case")
    void testGetApplicationVersion_01() {
        //Given
        //When
        //Then
        Assertions.assertEquals(APPLICATION_VERSION, this.configuration.application().version());
    }

    @Test
    @DisplayName("Get engine.name - nominal case")
    void testGetEngineName_01() {
        //Given
        //When
        //Then
        Assertions.assertEquals(ENGINE_NAME, this.configuration.engine().name());
    }

    @Test
    @DisplayName("Get engine.version - nominal case")
    void testGetEngineVersion_01() {
        //Given
        //When
        //Then
        Assertions.assertEquals(ENGINE_VERSION, this.configuration.engine().version());
    }

    @Test
    @DisplayName("Get window.width - nominal case")
    void testGetWindowWidth_01() {
        //Given
        //When
        //Then
        Assertions.assertEquals(WINDOW_WIDTH, this.configuration.window().width());
    }

    @Test
    @DisplayName("Get window.height - nominal case")
    void testGetWindowHeight_01() {
        //Given
        //When
        //Then
        Assertions.assertEquals(WINDOW_HEIGHT, this.configuration.window().height());
    }

    @Test
    @DisplayName("Get window.resizable - nominal case")
    void testGetWindowResizable_01() {
        //Given
        //When
        //Then
        Assertions.assertEquals(WINDOW_RESIZABLE, this.configuration.window().resizable());
    }

    @Test
    @DisplayName("Get window.maximized - nominal case")
    void testGetWindowMaximized_01() {
        //Given
        //When
        //Then
        Assertions.assertEquals(WINDOW_MAXIMIZED, this.configuration.window().maximized());
    }

}

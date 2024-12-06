package org.sc.themis.window;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sc.themis.Profiles;
import org.sc.themis.shared.Configuration;
import org.sc.themis.shared.exception.ThemisException;

@QuarkusTest
@TestProfile(Profiles.TagWithUiTest.class)
public class WindowTest {

    @Inject
    Configuration configuration;

    @Test
    @DisplayName("Create window - nominal case")
    void testCreateWindow_01() throws ThemisException {

        //Given
        Window window = new Window( configuration );

        //When
        window.setup();

        //Then
        Assertions.assertNotNull( window.getHandle() );
        Assertions.assertEquals( 320, window.getSize().x );
        Assertions.assertEquals( 200, window.getSize().y );
        Assertions.assertNotEquals( 0, window.getResolution().x );
        Assertions.assertNotEquals( 0, window.getResolution().y );

        //Cleanup
        window.cleanup();

    }

}

package org.sc.themis.window;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sc.themis.shared.exception.ThemisException;

@QuarkusTest
public class WindowTest {

    @Test
    @DisplayName("Create window - nominal case")
    void testCreateWindow_01() throws ThemisException {

        //Given
        WindowDescriptor descriptor = new WindowDescriptor(800, 600, "Mon application");
        Window window = new Window( descriptor );

        //When
        window.setup();

        //Then
        Assertions.assertNotNull( window.getHandle() );
        Assertions.assertEquals( 800, window.getSize().x );
        Assertions.assertEquals( 600, window.getSize().y );
        Assertions.assertNotEquals( 0, window.getResolution().x );
        Assertions.assertNotEquals( 0, window.getResolution().y );

        //Cleanup
        window.cleanup();

    }

}

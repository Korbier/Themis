package org.sc.themis.engine;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sc.themis.engine.exception.EngineGamestateNotFoundException;
import org.sc.themis.renderer.RendererDescriptor;
import org.sc.themis.renderer.activity.RendererActivity;
import org.sc.themis.scene.SceneDescriptor;
import org.sc.themis.shared.exception.ThemisException;
import org.sc.themis.window.WindowDescriptor;

@QuarkusTest
public class EngineTest {

    @Test
    @DisplayName("Create engine - nominal case")
    void testCreateEngine_01() throws ThemisException {

        //Given
        EngineDescriptor descriptor = new EngineDescriptor(
            new WindowDescriptor(800, 600, "Mon application"),
            new RendererDescriptor( new RendererActivity() {} ),
            new SceneDescriptor()
        );

        Engine engine = new Engine( descriptor );

        //When
        engine.setup();
        engine.setGamestate( new EngineTestGamestate( engine, 5 ) );
        engine.run();

        //Then
        /**
        Assertions.assertNotNull( window.getHandle() );
        Assertions.assertEquals( 800, window.getSize().x );
        Assertions.assertEquals( 600, window.getSize().y );
        Assertions.assertNotEquals( 0, window.getResolution().x );
        Assertions.assertNotEquals( 0, window.getResolution().y );
        **/

        //Cleanup
        engine.cleanup();

    }


    @Test
    @DisplayName("Create engine - no gamestate found")
    void testCreateEngine_02() throws ThemisException {

        //Given
        EngineDescriptor descriptor = new EngineDescriptor(
            new WindowDescriptor(800, 600, "Mon application"),
            new RendererDescriptor( new RendererActivity() {} ),
            new SceneDescriptor()
        );

        Engine engine = new Engine( descriptor );

        //When
        engine.setup();

        //Then
        Assertions.assertThrows( EngineGamestateNotFoundException.class,  engine::run );
        /**
         Assertions.assertNotNull( window.getHandle() );
         Assertions.assertEquals( 800, window.getSize().x );
         Assertions.assertEquals( 600, window.getSize().y );
         Assertions.assertNotEquals( 0, window.getResolution().x );
         Assertions.assertNotEquals( 0, window.getResolution().y );
         **/

        //Cleanup
        engine.cleanup();

    }

}

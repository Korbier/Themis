package org.sc.themis.engine;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sc.playground.noop.NoopRendererActivity;
import org.sc.playground.triangle.TriangleRendererActivity;
import org.sc.themis.engine.exception.EngineGamestateNotFoundException;
import org.sc.themis.shared.Configuration;
import org.sc.themis.shared.exception.ThemisException;

@QuarkusTest
public class EngineTest {

    @Inject
    Configuration configuration;

    @Test
    @DisplayName("Create engine - nominal case")
    void testCreateEngine_01() throws ThemisException {

        //Given

        Engine engine = new Engine( configuration, new TriangleRendererActivity( this.configuration ) );

        //When
        engine.setup();
        engine.setGamestate( new EngineTestGamestate( engine, 5 ) );
        engine.run();

        //Then

        //Cleanup
        engine.cleanup();

    }


    @Test
    @DisplayName("Create engine - no gamestate found")
    void testCreateEngine_02() throws ThemisException {

        //Given

        Engine engine = new Engine( configuration, new NoopRendererActivity( this.configuration ) );

        //When
        engine.setup();

        //Then
        Assertions.assertThrows( EngineGamestateNotFoundException.class,  engine::run );

        //Cleanup
        engine.cleanup();

    }

}

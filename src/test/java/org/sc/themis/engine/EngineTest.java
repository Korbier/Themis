package org.sc.themis.engine;

import io.quarkus.runtime.configuration.ConfigUtils;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sc.playground.noop.NoopRendererActivity;
import org.sc.playground.pushconstant.PushConstantRendererActivity;
import org.sc.playground.triangle.TriangleRendererActivity;
import org.sc.themis.Profiles;
import org.sc.themis.engine.exception.EngineGamestateNotFoundException;
import org.sc.themis.shared.Configuration;
import org.sc.themis.shared.exception.ThemisException;

import java.util.stream.Collectors;

@QuarkusTest
@TestProfile(Profiles.TagWithUiTest.class)
public class EngineTest {

    @Inject
    Configuration configuration;

    @Test
    @DisplayName("Create engine - no gamestate found")
    void testCreateEngine_01() throws ThemisException {

        //Given

        Engine engine = new Engine( configuration, new NoopRendererActivity( this.configuration ) );

        //When
        engine.setup();

        //Then
        Assertions.assertThrows( EngineGamestateNotFoundException.class,  engine::run );

        //Cleanup
        engine.cleanup();

    }

    @Test
    @DisplayName("Create engine - nominal case - triangle")
    void testCreateEngine_02() throws ThemisException {

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
    @DisplayName("Create engine - nominal case - pushconstant")
    void testCreateEngine_03() throws ThemisException {

        //Given
        Engine engine = new Engine( configuration, new PushConstantRendererActivity( this.configuration ) );

        //When
        engine.setup();
        engine.setGamestate( new EngineTestGamestate( engine, 5 ) );
        engine.run();

        //Then

        //Cleanup
        engine.cleanup();

    }


}

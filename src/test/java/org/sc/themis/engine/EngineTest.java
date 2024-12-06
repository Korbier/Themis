package org.sc.themis.engine;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.sc.playground.Playgrounds;
import org.sc.playground.noop.NoopRendererActivity;
import org.sc.themis.Profiles;
import org.sc.themis.engine.exception.EngineGamestateNotFoundException;
import org.sc.themis.renderer.activity.RendererActivity;
import org.sc.themis.shared.Configuration;
import org.sc.themis.shared.exception.ThemisException;

@QuarkusTest
@TestProfile(Profiles.TagWithUiTest.class)
public class EngineTest {

    @Inject
    Configuration configuration;

    @ParameterizedTest
    @EnumSource(value=Playgrounds.class, names="DESC_UNIFORM")
    void testRenderActivity( Playgrounds playground ) throws ThemisException {

        //Given
        RendererActivity activity = playground.getFactory().apply( this.configuration );
        Engine engine = new Engine( configuration, activity );

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


}

package org.sc.viewer;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.sc.themis.Profiles;
import org.sc.themis.engine.Engine;
import org.sc.themis.shared.Configuration;
import org.sc.themis.shared.exception.ThemisException;
import org.sc.viewer.gamestate.ViewerGamestate;
import org.sc.viewer.renderactivity.ViewerRendererActivity;

@QuarkusTest
@TestProfile(Profiles.TagWithUiTest.class)
public class ViewerTest {

    @Inject
    Configuration configuration;

    @Test
//    @Disabled
    void runViewer() throws ThemisException {

        //Given
        ViewerGamestate gamestate = new ViewerGamestate();
        Engine engine = new Engine( configuration, new ViewerRendererActivity( this.configuration, gamestate ) );

        //When
        engine.setup();
        engine.setGamestate( gamestate );
        engine.run();

        //Then

        //Cleanup
        engine.cleanup();

    }

}

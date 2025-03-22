package org.sc.themis.engine;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.sc.TestWithConfiguration;
import org.sc.playground.Playgrounds;
import org.sc.playground.noop.NoopRendererActivity;
import org.sc.themis.engine.exception.EngineGamestateNotFoundException;
import org.sc.themis.renderer.RendererActivity;
import org.sc.themis.shared.configuration.Configuration;
import org.sc.themis.shared.exception.ThemisException;

public class EngineTest extends TestWithConfiguration {

  @ParameterizedTest
  @EnumSource(value = Playgrounds.class, names = "NOOP", mode = EnumSource.Mode.EXCLUDE)
  @Disabled
  void testRenderActivity(Playgrounds playground) throws ThemisException {

    // Given
    RendererActivity activity = playground.getFactory().apply(getConfiguration());
    Engine engine = new Engine(getConfiguration(), activity);

    // When
    engine.setup();
    engine.setGamestate(new EngineTestGamestate(
        engine,
        playground.getGamestate(),
        5));
    engine.run();

    // Then

    // Cleanup
    engine.cleanup();
  }

  @Test
  @DisplayName("Create engine - no gamestate found")
  @Disabled
  void testCreateEngine_01() throws ThemisException {

    // Given

    Engine engine = new Engine(getConfiguration(), new NoopRendererActivity(getConfiguration()));

    // When
    engine.setup();

    // Then
    Assertions.assertThrows(EngineGamestateNotFoundException.class, engine::run);

    // Cleanup
    engine.cleanup();
  }
}

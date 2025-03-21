package org.sc.themis.window;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sc.TestWithConfiguration;
import org.sc.themis.shared.exception.ThemisException;

public class WindowTest extends TestWithConfiguration {

  @Test
  @DisplayName("Create window - nominal case")
  @Disabled
  void testCreateWindow_01() throws ThemisException {

    // Given
    Window window = new Window(getConfiguration());

    // When
    window.setup();

    // Then
    Assertions.assertNotNull(window.getHandle());
    Assertions.assertEquals(800, window.getSize().x);
    Assertions.assertEquals(600, window.getSize().y);
    Assertions.assertNotEquals(0, window.getResolution().x);
    Assertions.assertNotEquals(0, window.getResolution().y);

    // Cleanup
    window.cleanup();
  }
}

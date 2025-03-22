package org.sc.themis.resource;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sc.themis.shared.resource.font.Font;

import java.nio.file.Path;

class FontTest {

  @Test
  @DisplayName("Setup - nominal case")
  void testSetup_01() {
    Font.normal(12, Path.of("./src/main/resources/playground/font/CenturyGothic.ttf"));
  }

}

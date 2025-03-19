package org.sc.themis.resource;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sc.themis.Profiles;
import org.sc.themis.shared.resource.font.Font;

import java.nio.file.Path;

@QuarkusTest
@TestProfile(Profiles.TagWithoutUiTest.class)
class FontTest {

  @Test
  @DisplayName("Setup - nominal case")
  void testSetup_01() {
    Font.normal(12, Path.of("./src/main/resources/playground/font/CenturyGothic.ttf"));
  }

}

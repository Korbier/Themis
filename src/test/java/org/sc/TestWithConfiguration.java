package org.sc;

import org.junit.jupiter.api.BeforeEach;
import org.sc.themis.shared.configuration.Configuration;

public class TestWithConfiguration {

  private Configuration configuration;

  @BeforeEach
  public void setConfiguration() {
      this.configuration = new Configuration("./src/test/resources/application.properties");
  }

  public Configuration getConfiguration() {
    return this.configuration;
  }

}

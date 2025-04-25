package org.sc.themis.shared.configuration;


import jakarta.enterprise.context.ApplicationScoped;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@ApplicationScoped
public class Configuration {

  private static final String DEFAULT_FILE = "application.properties";

  private java.util.Properties properties;
/**
  public Configuration() {
    load(DEFAULT_FILE);
  }

  public Configuration(String filename) {
    load(filename);
  }
**/
  public Configuration load(String filename) {

    try (InputStream stream = new FileInputStream(filename) ){
      this.properties = new java.util.Properties();
      this.properties.load( stream );
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    return this;

  }

  public <T> T get(ConfigurationKey<T> property, T defaultValue ) {
    if ( this.properties.containsKey( property.key() ) ) {
      return property.parse(this.properties.getProperty(property.key()));
    } else {
      return defaultValue;
    }
  }

  public <T> T get(ConfigurationKey<T> property ) {
    return get(property, property.defaultValue());
  }

}

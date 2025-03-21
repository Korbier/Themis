package org.sc.themis.shared.configuration;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class Configuration {

  private static final String DEFAULT_FILE = "application.properties";

  private java.util.Properties properties;

  public Configuration() {
    load(DEFAULT_FILE);
  }

  public Configuration(String filename) {
    load(filename);
  }


  public Configuration load(String filename) {

    try (InputStream stream = new FileInputStream(filename) ){
      this.properties = new java.util.Properties();
      this.properties.load( stream );
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    return this;

  }

  public String get(ConfigurationEnum property, String defaultValue ) {
    return this.properties.getProperty( property.key(), defaultValue );
  }

  public int get(ConfigurationEnum property, int defaultValue ) {

    if ( this.properties.containsKey( property.key() ) ) {
      return Integer.parseInt( this.properties.getProperty(property.key() ) );
    }

    return defaultValue;

  }

  public float get(ConfigurationEnum property, float defaultValue ) {

    if ( this.properties.containsKey( property.key() ) ) {
      return Float.parseFloat( this.properties.getProperty(property.key() ) );
    }

    return defaultValue;

  }

  public boolean get(ConfigurationEnum property, boolean defaultValue ) {

    if ( this.properties.containsKey( property.key() ) ) {
      return Boolean.parseBoolean( this.properties.getProperty(property.key() ) );
    }

    return defaultValue;

  }
}

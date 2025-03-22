package org.sc.themis.shared.configuration;

import java.util.function.Function;

public class ConfigurationKey<T> {

  private Class<T> type;
  private String key;
  private T defaultValue;

  private Function<String, T> parseFunction;

  public static <O> ConfigurationKey<O> of( String key, Class<O> type, O defaultValue, Function<String, O> parseFunction ) {
    ConfigurationKey<O> ckey = new ConfigurationKey<>();
    ckey.key = key;
    ckey.type = type;
    ckey.defaultValue = defaultValue;
    ckey.parseFunction = parseFunction;
    return ckey;
  }

  public Class<T> type() {
    return this.type;
  }

  public String key() {
    return key;
  }

  public T defaultValue() {
    return defaultValue;
  }

  public T parse(String input) {
    return this.parseFunction.apply(input);
  }

}

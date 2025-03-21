package org.sc.themis.shared;

public interface Configuration {

  public static Configuration defaultConfiguration() {
    return new Configuration() {
      @Override
      public Application application() {
        return null;
      }
      @Override
      public Engine engine() {
        return null;
      }
      @Override
      public Window window() {
        return null;
      }
      @Override
      public Renderer renderer() {
        return null;
      }
      @Override
      public Scene scene() {
        return null;
      }
    };
  }

  Application application();
  Engine engine();
  Window window();
  Renderer renderer();
  Scene scene();

  interface Application {
    String name();
    int version();
  }

  interface Engine {
    String name();
    int version();
  }

  interface Window {
    int width();
    int height();
    boolean resizable();
    boolean maximized();
  }

  interface Renderer {
    boolean debug();
    int imageCount();
    boolean vsyncEnabled();
    Feature feature();
  }

  interface Feature {
    boolean samplerAnisotropy();
    boolean geometryShader();
    boolean fragmentStoresAndAtomics();
  }

  interface Scene {
    Projection projection();
  }

  interface Projection {
    float fov();
    float znear();
    float zfar();
  }
}

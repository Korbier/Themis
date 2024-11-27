package org.sc.themis.shared;

import io.smallrye.config.ConfigMapping;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
@ConfigMapping( prefix = "themis" )
public interface Configuration {

    Application application();
    Engine engine();
    Window window();
    Renderer renderer();

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
    }


}

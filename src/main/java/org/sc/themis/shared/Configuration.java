package org.sc.themis.shared;

import io.smallrye.config.ConfigMapping;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
@ConfigMapping( prefix = "themis" )
public interface Configuration {

    Application application();
    Engine engine();
    Window window();

    interface Application {
        String name();
        String version();
    }

    interface Engine {
        String name();
        String version();
    }

    interface Window {
        int width();
        int height();
        boolean resizable();
        boolean maximized();
    }


}

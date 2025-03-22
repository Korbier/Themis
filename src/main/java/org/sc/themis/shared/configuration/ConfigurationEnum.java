package org.sc.themis.shared.configuration;

public enum ConfigurationEnum {

    applicationName("application.name"),
    applicationVersion("application.version"),

    engineName("engine.name"),
    engineVersion("engine.version"),

    windowWidth("window.width"),
    windowHeight("window.height"),
    windowResizable("window.resizable"),
    windowMaximized("window.maximized"),

    rendererDebug("renderer.debug"),
    rendererVSyncEnabled("renderer.vsync.enabled"),
    rendererImageCount("renderer.image-count"),
    rendererFeatureSamplerAnisotropy("renderer.feature.sampler-anisotropy"),
    rendererFeatureGeometryShader("renderer.feature.geometry-shader"),
    rendererFeatureFragmentStoresAndAtomics("renderer.feature.fragment-stores-and-atomics"),

    sceneProjectionFov("scene.projection.fov"),
    sceneProjectionZNear("scene.projection.znear"),
    sceneProjectionZFar("scene.projection.zfar"),

    ;

    private String key;

    private ConfigurationEnum(String key) {
        this.key = key;
    }

    public String key() {
        return this.key;
    }

}

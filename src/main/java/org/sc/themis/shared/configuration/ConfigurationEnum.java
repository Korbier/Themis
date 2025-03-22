package org.sc.themis.shared.configuration;

import java.util.function.Function;

public class ConfigurationEnum {

    private final static Function<String, String> toString = a -> a;
    private final static Function<String, Integer> toInteger = Integer::parseInt;
    private final static Function<String, Float> toFloat = Float::parseFloat;
    private final static Function<String, Boolean> toBoolean = Boolean::parseBoolean;

    public final static ConfigurationKey<String> applicationName = ConfigurationKey.of("application.name", String.class, "no-name", ConfigurationEnum.toString);
    public final static ConfigurationKey<Integer> applicationVersion = ConfigurationKey.of("application.version", Integer.class, 1, ConfigurationEnum.toInteger);

    public final static ConfigurationKey<String> engineName = ConfigurationKey.of("engine.name", String.class, "no-name", ConfigurationEnum.toString);
    public final static ConfigurationKey<Integer> engineVersion = ConfigurationKey.of("engine.version", Integer.class, 1, ConfigurationEnum.toInteger);

    public final static ConfigurationKey<Integer> windowWidth = ConfigurationKey.of("window.width", Integer.class, 800, ConfigurationEnum.toInteger);
    public final static ConfigurationKey<Integer> windowHeight = ConfigurationKey.of("window.height", Integer.class, 600, ConfigurationEnum.toInteger);
    public final static ConfigurationKey<Boolean> windowResizable = ConfigurationKey.of("window.resizable", Boolean.class, true, ConfigurationEnum.toBoolean);
    public final static ConfigurationKey<Boolean> windowMaximized = ConfigurationKey.of("window.maximized", Boolean.class, false, ConfigurationEnum.toBoolean);

    public final static ConfigurationKey<Boolean> rendererDebug = ConfigurationKey.of("renderer.debug", Boolean.class, false, ConfigurationEnum.toBoolean);
    public final static ConfigurationKey<Boolean> rendererVSyncEnabled = ConfigurationKey.of("renderer.vsync.enabled", Boolean.class, true, ConfigurationEnum.toBoolean);
    public final static ConfigurationKey<Integer> rendererImageCount = ConfigurationKey.of("renderer.image-count", Integer.class, 3, ConfigurationEnum.toInteger);
    public final static ConfigurationKey<Boolean> rendererFeatureSamplerAnisotropy = ConfigurationKey.of("renderer.feature.sampler-anisotropy", Boolean.class, true, ConfigurationEnum.toBoolean);
    public final static ConfigurationKey<Boolean> rendererFeatureGeometryShader = ConfigurationKey.of("renderer.feature.geometry-shader", Boolean.class, true, ConfigurationEnum.toBoolean);
    public final static ConfigurationKey<Boolean> rendererFeatureFragmentStoresAndAtomics = ConfigurationKey.of("renderer.feature.fragment-stores-and-atomics", Boolean.class, true, ConfigurationEnum.toBoolean);

    public final static ConfigurationKey<Float> sceneProjectionFov = ConfigurationKey.of("scene.projection.fov", Float.class, 60.0f, ConfigurationEnum.toFloat);
    public final static ConfigurationKey<Float> sceneProjectionZNear = ConfigurationKey.of("scene.projection.znear", Float.class, 0.1f, ConfigurationEnum.toFloat);
    public final static ConfigurationKey<Float> sceneProjectionZFar = ConfigurationKey.of("scene.projection.zfar", Float.class, 1400.0f, ConfigurationEnum.toFloat);


}

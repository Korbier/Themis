package org.sc.themis.engine;

import org.sc.themis.renderer.RendererDescriptor;
import org.sc.themis.scene.SceneDescriptor;
import org.sc.themis.shared.TObjectDescriptor;
import org.sc.themis.window.WindowDescriptor;

public record EngineDescriptor(
    WindowDescriptor window,
    RendererDescriptor renderer,
    SceneDescriptor scene
)
    implements TObjectDescriptor
{}

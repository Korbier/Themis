package org.sc.themis.renderer;

import org.sc.themis.renderer.activity.RendererActivity;
import org.sc.themis.shared.TObjectDescriptor;

public record RendererDescriptor(
    RendererActivity activity
)
 implements TObjectDescriptor
{
}

package org.sc.themis.input;

import org.sc.themis.shared.TObjectDescriptor;
import org.sc.themis.window.Window;

public record InputDescriptor(
    Window window
)
    implements TObjectDescriptor
{

}

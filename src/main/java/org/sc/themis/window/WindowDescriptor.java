package org.sc.themis.window;

import org.sc.themis.shared.TObjectDescriptor;

public record WindowDescriptor(
    int width,
    int height,
    String title,
    boolean resizable,
    boolean maximized
)
    implements TObjectDescriptor
{

    public WindowDescriptor(int width, int height, String title) {
        this( width, height, title, true, false );
    }

}

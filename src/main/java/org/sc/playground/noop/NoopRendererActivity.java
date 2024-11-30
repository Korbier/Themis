package org.sc.playground.noop;

import org.sc.themis.renderer.Renderer;
import org.sc.themis.renderer.activity.RendererActivity;
import org.sc.themis.shared.Configuration;
import org.sc.themis.shared.exception.ThemisException;

public class NoopRendererActivity extends RendererActivity {

    public NoopRendererActivity(Configuration configuration) {
        super(configuration);
    }

    @Override
    public void setup(Renderer renderer) throws ThemisException {

    }

    @Override
    public void cleanup() throws ThemisException {

    }

}

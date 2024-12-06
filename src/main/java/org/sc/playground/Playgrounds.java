package org.sc.playground;

import org.sc.playground.descriptorset.imagesampler.DescriptorsetImageSamplerRendererActivity;
import org.sc.playground.descriptorset.uniform.DescriptorsetUniformRendererActivity;
import org.sc.playground.noop.NoopGamestate;
import org.sc.playground.noop.NoopRendererActivity;
import org.sc.playground.pushconstant.PushConstantRendererActivity;
import org.sc.playground.triangle.TriangleRendererActivity;
import org.sc.themis.gamestate.Gamestate;
import org.sc.themis.renderer.activity.RendererActivity;
import org.sc.themis.shared.Configuration;

import java.util.function.Function;

public enum Playgrounds {

    NOOP( "noop", new NoopGamestate(), NoopRendererActivity::new),
    TRIANGLE( "triangle", new NoopGamestate(), TriangleRendererActivity::new ),
    PUSH_CONSTANT( "pushconstant", new NoopGamestate(), PushConstantRendererActivity::new ),
    DESC_UNIFORM( "desc-uniform", new NoopGamestate(), DescriptorsetUniformRendererActivity::new ),
    DESC_IMAGESAMPLER( "desc-imagesampler", new NoopGamestate(), DescriptorsetImageSamplerRendererActivity::new );

    final String name;
    final Gamestate gamestate;
    final Function<Configuration, RendererActivity> rendererActivityFactory;

    Playgrounds(String name, Gamestate gamestate, Function<Configuration, RendererActivity> rendererActivityFactory) {
        this.name = name;
        this.gamestate = gamestate;
        this.rendererActivityFactory = rendererActivityFactory;
    }

    public Function<Configuration, RendererActivity> getFactory() {
        return this.rendererActivityFactory;
    }

    public static Playgrounds fromName( String name ) {

        for (Playgrounds playground : values() ) {
            if ( playground.name.equals( name ) ) return playground;
        }

        return Playgrounds.NOOP;

    }

}

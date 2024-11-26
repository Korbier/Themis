package org.sc.playground;

import org.sc.playground.noop.NoopGamestate;
import org.sc.playground.noop.NoopRendererActivity;
import org.sc.themis.gamestate.Gamestate;
import org.sc.themis.renderer.activity.RendererActivity;

public enum Playgrounds {

    NOOP( "noop", new NoopGamestate(), new NoopRendererActivity() );

    final String name;
    final Gamestate gamestate;
    final RendererActivity rendererActivity;

    Playgrounds(String name, Gamestate gamestate, RendererActivity activity) {
        this.name = name;
        this.gamestate = gamestate;
        this.rendererActivity = activity;
    }

    public static Playgrounds fromName( String name ) {

        for (Playgrounds playground : values() ) {
            if ( playground.name.equals( name ) ) return playground;
        }

        return Playgrounds.NOOP;

    }



}

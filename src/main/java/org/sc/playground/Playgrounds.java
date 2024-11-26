package org.sc.playground;

import org.sc.playground.starter.StarterGamestate;
import org.sc.playground.starter.StarterRendererActivity;
import org.sc.themis.gamestate.Gamestate;
import org.sc.themis.renderer.activity.RendererActivity;

public enum Playgrounds {

    STARTER( "starter", new StarterGamestate(), new StarterRendererActivity() );

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

        return Playgrounds.STARTER;

    }



}

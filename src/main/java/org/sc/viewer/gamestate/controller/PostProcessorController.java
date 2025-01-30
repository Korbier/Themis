package org.sc.viewer.gamestate.controller;

import org.sc.themis.input.Input;
import org.sc.themis.scene.Controller;
import org.sc.viewer.gamestate.PostProcessorContext;

import java.util.HashMap;
import java.util.Map;

public class PostProcessorController implements Controller {

    private final Map<Integer, String> keymap = new HashMap<>();
    private final PostProcessorContext context;

    public PostProcessorController( PostProcessorContext context ) {
        this.context = context;
    }

    public void map( int key, String postprocessor ) {
        this.keymap.put( key, postprocessor );
    }

    @Override
    public void update(long tpf) {}

    @Override
    public void input( Input input, long tpf ) {
        for ( int key : keymap.keySet() ) {
            if ( input.isKeyPressedNoRepeat( key ) ) {
                this.context.switchPostProcessor( keymap.get( key ) );
            }
        }
    }

}

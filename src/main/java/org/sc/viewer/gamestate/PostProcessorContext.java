package org.sc.viewer.gamestate;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class PostProcessorContext {

    private final Map<String, Boolean> enabled = new HashMap<>();

    public PostProcessorContext() {}

    public void add( String identifier ) {
        this.enabled.put( identifier, false );
    }

    public void switchPostProcessor( String identifier ) {
        this.enabled.put( identifier, !this.enabled.get(identifier) );
    }

    public boolean isEnabled( String identifier ) {
        return this.enabled.get( identifier );
    }

    public Set<String> getPostProcessors() {
        return this.enabled.keySet();
    }

}

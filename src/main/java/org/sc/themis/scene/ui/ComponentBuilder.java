package org.sc.themis.scene.ui;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import org.sc.themis.scene.pencil.Pencil;

public abstract sealed class ComponentBuilder
        permits ButtonBuilder {

    private final UIBuilder uiBuilder;
    private final Map<String, Consumer<UIBuilder>> events = new HashMap<>();

    abstract protected void build();

    protected ComponentBuilder(UIBuilder uiBuilder) {
        this.uiBuilder = uiBuilder;
    }

    protected UiState state() {
        return this.uiBuilder.getState();
    }

    protected Pencil pencil() {
        return this.uiBuilder.getPencil();
    }

    protected boolean regionHit(int x, int y, int w, int h) {
        return !(
                (state().getMouseX() < x)
                        || (state().getMouseY() < y)
                        || (state().getMouseX() >= (x + w))
                        || (state().getMouseY() >= (y + h))
        );
    }

    protected void addEvent(String event, Consumer<UIBuilder> eventConsumer) {
        this.events.put(event, eventConsumer);
    }

    protected void fireEvent(String event) {
        if (isEventDefined(event)) {
            this.events.get(event).accept(this.uiBuilder);
        }
    }

    protected boolean isEventDefined(String event) {
        return this.events.containsKey(event);
    }

}

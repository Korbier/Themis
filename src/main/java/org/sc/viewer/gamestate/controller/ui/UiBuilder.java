package org.sc.viewer.gamestate.controller.ui;

import org.sc.themis.scene.pencil.Pencil;

public class UiBuilder {

    private final UiState state;
    private final Pencil pencil;

    private UiBuilder(UiState state, Pencil pencil) {
        this.state = state;
        this.pencil = pencil;
    }

    public static UiBuilder of(UiState state, Pencil pencil) {
        return new UiBuilder(state, pencil);
    }

    private UiState state() {
        return this.state;
    }

    public Pencil pencil() {
        return pencil;
    }

    private boolean regionHit(int x, int y, int w, int h) {
        return !(
            (state().getMouseX() < x)
            || (state().getMouseY() < y)
            || (state().getMouseX() >= (x + w))
            || (state().getMouseY() >= (y + h))
        );
    }

    public boolean button(String id, int x, int y) {

        if (regionHit(x, y, 64, 48)) {
            state().setHotItem(id);
            if (state().getActiveItem() == null && state().isMouseDown()) {
                state().setActiveItem(id);
            }
        }

        pencil().drawRect(x, y, 64, 48, .9f, .9f, .9f);

        if (id.equals(state().getHotItem())) {
            if (id.equals(state().getActiveItem())) {
                pencil().drawRect(x + 2, y + 2, 60, 44, 0.0f, 1.0f, 1.0f);
            } else {
                pencil().drawRect(x + 2, y + 2, 60, 44, 0.0f, 0.5f, 0.5f);
            }
        } else {
            pencil().drawRect(x, y, 64, 48, .5f, .5f, .5f);
        }

        return state().isMouseDown()
            && id.equals(state().getHotItem())
            && id.equals(state().getActiveItem());

    }

}

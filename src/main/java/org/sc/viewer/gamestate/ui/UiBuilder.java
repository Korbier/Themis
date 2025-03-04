package org.sc.viewer.gamestate.ui;

import org.sc.themis.scene.pencil.Pencil;

public class UiBuilder {

    public static final int DEFAULT_BORDER_SIZE = 1;
    public static final int DEFAULT_BUTTON_WIDTH = 64;
    public static final int DEFAULT_BUTTON_HEIGHT = 24;

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

        if (regionHit(x, y, DEFAULT_BUTTON_WIDTH, DEFAULT_BUTTON_HEIGHT)) {
            state().setHotItem(id);
            if (state().getActiveItem() == null && state().isMouseDown()) {
                state().setActiveItem(id);
            }
        }

        pencil().drawRect(x, y, DEFAULT_BUTTON_WIDTH, DEFAULT_BUTTON_HEIGHT, .3f, .3f, .3f);

        if (id.equals(state().getHotItem())) {
            if (id.equals(state().getActiveItem())) {
                pencil().drawRect(
                    x + DEFAULT_BORDER_SIZE,
                    y + DEFAULT_BORDER_SIZE,
                    DEFAULT_BUTTON_WIDTH - 2 * DEFAULT_BORDER_SIZE,
                    DEFAULT_BUTTON_HEIGHT - 2 * DEFAULT_BORDER_SIZE,
                    .5f, .5f, .5f
                );
            } else {
                pencil().drawRect(
                    x + DEFAULT_BORDER_SIZE,
                    y + DEFAULT_BORDER_SIZE,
                    DEFAULT_BUTTON_WIDTH - 2 * DEFAULT_BORDER_SIZE,
                    DEFAULT_BUTTON_HEIGHT - 2 * DEFAULT_BORDER_SIZE,
                    1.f, 1.f, 1.f
                );
            }
        } else {
            pencil().drawRect(
                x + DEFAULT_BORDER_SIZE,
                y + DEFAULT_BORDER_SIZE,
                DEFAULT_BUTTON_WIDTH - 2 * DEFAULT_BORDER_SIZE,
                DEFAULT_BUTTON_HEIGHT - 2 * DEFAULT_BORDER_SIZE,
                .3f, .3f, .3f
            );
        }

        return state().isMouseDown()
            && id.equals(state().getHotItem())
            && id.equals(state().getActiveItem());

    }

}

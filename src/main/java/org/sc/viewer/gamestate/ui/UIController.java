package org.sc.viewer.gamestate.ui;

import org.sc.themis.input.Input;
import org.sc.themis.scene.Controller;
import org.sc.themis.scene.pencil.Pencil;

import java.util.UUID;

public class UIController implements Controller {

    private final Pencil pencil;
    private final UiState uiState = new UiState();

    public UIController(Pencil pencil) {
        this.pencil = pencil;
    }

    @Override
    public void update(long tpf) {

        prepare();

        UiBuilder builder = UiBuilder.of(uiState, this.pencil);

        if (builder.button(UUID.randomUUID().toString(), 10, 10)) {
            System.out.println("plop");
        }

        if (builder.button(UUID.randomUUID().toString(), 76, 10)) {
            System.out.println("plip");
        }

        finish();

    }

    @Override
    public void input(Input input, long tpf) {
        this.uiState.setMouseX((int) input.getMousePosition().x);
        this.uiState.setMouseY((int) input.getMousePosition().y);
        this.uiState.setMouseDown(input.isLeftButtonPressed());
    }

    private void prepare() {
        uiState.setHotItem(null);
        this.pencil.clear();
    }

    private void finish() {
        if (!uiState.isMouseDown()) {
            uiState.setActiveItem(null);
        } else {
            if (uiState.getActiveItem() == null) {
                uiState.setActiveItem("NOT_AVAILABLE");
            }
        }
    }

}

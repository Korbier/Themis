package org.sc.viewer.gamestate.controller;

import org.sc.themis.input.Input;
import org.sc.themis.scene.Controller;
import org.sc.themis.scene.Scene;
import org.sc.viewer.gamestate.controller.ui.UiBuilder;
import org.sc.viewer.gamestate.controller.ui.UiState;

import java.util.UUID;

public class UIController implements Controller {

    private final Scene scene;
    private final UiState uiState = new UiState();

    public UIController(Scene scene) {
        this.scene = scene;
    }

    @Override
    public void update(long tpf) {

        prepare();

        UiBuilder builder = UiBuilder.of(uiState, scene.getPencil());

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
        scene.getPencil().clear();
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

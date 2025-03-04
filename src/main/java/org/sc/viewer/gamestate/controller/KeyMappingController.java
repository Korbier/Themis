package org.sc.viewer.gamestate.controller;

import org.sc.themis.input.Input;
import org.sc.themis.scene.Controller;
import org.sc.viewer.ViewerKeyMapping;

public class KeyMappingController implements Controller {

    private final ViewerKeyMapping keyMapping;

    public KeyMappingController(ViewerKeyMapping keyMapping) {
        this.keyMapping = keyMapping;
    }

    @Override
    public void update(long tpf) {}

    @Override
    public void input(Input input, long tpf) {
        this.keyMapping.input(input);
    }

}

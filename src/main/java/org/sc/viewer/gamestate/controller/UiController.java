package org.sc.viewer.gamestate.controller;

import org.sc.themis.input.Input;
import org.sc.themis.scene.Controller;
import org.sc.themis.scene.pencil.Pencil;
import org.sc.themis.scene.ui.UIBuilder;
import org.sc.viewer.ViewerContext;

import java.util.UUID;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_F1;

public class UiController implements Controller {

    private final ViewerContext context;
    private final UIBuilder builder;

    public UiController(Pencil pencil, ViewerContext context) {
        this.context = context;
        this.builder = new UIBuilder(pencil);
    }

    @Override
    public void update(long tpf) {

        this.builder.begin();

        this.builder
            .button(UUID.randomUUID().toString(), "Normals")
            .left(10).top(10)
            .width(120).height(60)
            .onClick(builder -> context.getKeyMapping().execute(GLFW_KEY_F1))
            .build();

        this.builder.end();

    }

    @Override
    public void input(Input input, long tpf) {
        this.builder.input(input);
    }

}

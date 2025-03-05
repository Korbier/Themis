package org.sc.themis.scene.ui;

import java.util.function.Consumer;

public final class ButtonBuilder extends ComponentBuilder {

    public static final int DEFAULT_BORDER_SIZE = 1;
    public static final int DEFAULT_BUTTON_WIDTH = 64;
    public static final int DEFAULT_BUTTON_HEIGHT = 24;

    public static final String EVENT_ON_CLICK = "button.event.onclick";
    public static final String EVENT_ON_HOVER = "button.event.onHover";

    private String identifier;
    private String text = null;
    private int left = 0;
    private int top = 0;
    private int width = 0;
    private int height = 0;

    ButtonBuilder(UIBuilder builder) {
        super(builder);
    }

    ButtonBuilder identifier(String identifier) {
        this.identifier = identifier;
        return this;
    }

    ButtonBuilder text(String text) {
        this.text = text;
        return this;
    }

    public ButtonBuilder left(int left) {
        this.left = left;
        return this;
    }

    public ButtonBuilder top(int top) {
        this.top = top;
        return this;
    }

    public ButtonBuilder width(int width) {
        this.width = width;
        return this;
    }

    public ButtonBuilder height(int height) {
        this.height = height;
        return this;
    }

    public ButtonBuilder onClick(Consumer<UIBuilder> eventListener) {
        addEvent(EVENT_ON_CLICK, eventListener);
        return this;
    }

    public ButtonBuilder onHover(Consumer<UIBuilder> eventListener) {
        addEvent(EVENT_ON_HOVER, eventListener);
        return this;
    }

    public void build() {

        if (regionHit(this.left, this.top, this.width, this.height)) {
            state().setHotItem(this.identifier);
            if (state().getActiveItem() == null && state().isMouseDown()) {
                state().setActiveItem(this.identifier);
            }
        }

        pencil().drawRect(this.left, this.top, this.width, this.height, .3f, .3f, .3f);

        if (this.identifier.equals(state().getHotItem())) {
            if (this.identifier.equals(state().getActiveItem())) {
                pencil().drawRect(
                    this.left + DEFAULT_BORDER_SIZE,
                    this.top + DEFAULT_BORDER_SIZE,
                    this.width - 2 * DEFAULT_BORDER_SIZE,
                    this.height - 2 * DEFAULT_BORDER_SIZE,
                    .5f, .5f, .5f
                );
            } else {
                pencil().drawRect(
                    this.left + DEFAULT_BORDER_SIZE,
                    this.top + DEFAULT_BORDER_SIZE,
                    this.width - 2 * DEFAULT_BORDER_SIZE,
                    this.height - 2 * DEFAULT_BORDER_SIZE,
                    1.f, 1.f, 1.f
                );
            }
        } else {
            pencil().drawRect(
                this.left + DEFAULT_BORDER_SIZE,
                this.top + DEFAULT_BORDER_SIZE,
                this.width - 2 * DEFAULT_BORDER_SIZE,
                this.height - 2 * DEFAULT_BORDER_SIZE,
                .3f, .3f, .3f
            );
        }

        if (this.text != null) {
            pencil().drawText(this.left + DEFAULT_BORDER_SIZE, this.top + DEFAULT_BORDER_SIZE, this.text);
        }

        if (shouldTriggerOnHoverEvent()) {
            this.fireEvent(EVENT_ON_HOVER);
        }

        if (shouldTriggerOnClickEvent()) {
            this.fireEvent(EVENT_ON_CLICK);
        }

    }

    private boolean shouldTriggerOnClickEvent() {
        return isEventDefined(EVENT_ON_CLICK)
                && state().isMouseDown()
                && this.identifier.equals(state().getHotItem())
                && this.identifier.equals(state().getActiveItem());
    }

    private boolean shouldTriggerOnHoverEvent() {
        return isEventDefined(EVENT_ON_HOVER)
                && this.identifier.equals(state().getHotItem());
    }

}

package org.sc.themis.scene.ui;

public class UiState {

    private int mouseX = -1;
    private int mouseY = -1;
    private boolean mouseDown = false;
    private String hotItem = null;
    private String activeItem = null;

    public int getMouseX() {
        return mouseX;
    }

    public void setMouseX(int mouseX) {
        this.mouseX = mouseX;
    }

    public int getMouseY() {
        return mouseY;
    }

    public void setMouseY(int mouseY) {
        this.mouseY = mouseY;
    }

    public boolean isMouseDown() {
        return mouseDown;
    }

    public void setMouseDown(boolean mouseDown) {
        this.mouseDown = mouseDown;
    }

    public String getHotItem() {
        return hotItem;
    }

    public void setHotItem(String hotItem) {
        this.hotItem = hotItem;
    }

    public String getActiveItem() {
        return activeItem;
    }

    public void setActiveItem(String activeItem) {
        this.activeItem = activeItem;
    }

}

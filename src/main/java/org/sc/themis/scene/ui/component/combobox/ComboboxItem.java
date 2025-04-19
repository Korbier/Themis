package org.sc.themis.scene.ui.component.combobox;

public class ComboboxItem<C> {

  private String label;
  private C content;

  public ComboboxItem(String label, C content) {
    this.label = label;
    this.content = content;
  }

  public String label() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public C content() {
    return content;
  }

  public void setContent(C content) {
    this.content = content;
  }

}

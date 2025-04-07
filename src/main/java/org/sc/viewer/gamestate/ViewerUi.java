package org.sc.viewer.gamestate;

import org.sc.themis.renderer.pencil2d.Pencil2D;
import org.sc.themis.scene.Scene;
import org.sc.themis.scene.pencil.Pencil;
import org.sc.themis.scene.ui.UiSceneController;
import org.sc.themis.scene.ui.component.*;
import org.sc.viewer.ViewerContext;

import static org.lwjgl.glfw.GLFW.*;

public class ViewerUi extends UiSceneController {

  private final ViewerContext context;

  private final static int PANEL_WIDTH  = 240;
  private final static int PANEL_HEIGHT = 120;
  private final static int PANEL_SPACE  =  10;

  private final static int PANEL_MARGIN = 2;
  private final static int PANEL_COLUMN_A_LEFT  = PanelBuilder.DEFAULT_BORDER_SIZE + PANEL_MARGIN;
  private final static int PANEL_COLUMN_A_WIDTH = 190;
  private final static int PANEL_COLUMN_B_LEFT  = PANEL_COLUMN_A_LEFT + PANEL_COLUMN_A_WIDTH + PANEL_MARGIN ;
  private final static int PANEL_COLUMN_B_WIDTH  = PANEL_WIDTH - (PANEL_COLUMN_B_LEFT + PANEL_MARGIN + PanelBuilder.DEFAULT_BORDER_SIZE);

  private final static int PANEL_ROW_HEIGHT = 20;

  private final ContainerBuilder desktop;
  private LabelBuilder infoMaterialLblValue;

  public ViewerUi(Scene scene, Pencil2D pencil, ViewerContext context) {

    super(scene, pencil);

    this.context = context;

    PanelBuilder infoPnl = createInfoPanel(PANEL_SPACE, PANEL_SPACE);
    PanelBuilder lightPnl = createLightPanel(PANEL_SPACE, PANEL_SPACE * 2 + PANEL_HEIGHT);
    PanelBuilder meshPnl = createMeshPanel(PANEL_SPACE, PANEL_SPACE * 3 + PANEL_HEIGHT* 2);
    PanelBuilder materialPnl = createMaterialPanel(PANEL_SPACE, PANEL_SPACE * 4 + PANEL_HEIGHT * 3);

    this.desktop = builder().container();
    this.desktop.child(infoPnl, lightPnl, meshPnl, materialPnl);

  }

  private PanelBuilder createInfoPanel(int left, int top) {

    LabelBuilder infoFpsLbl = builder().label().debug(true)
        .text("Fps")
        .size(PANEL_COLUMN_A_WIDTH, PANEL_ROW_HEIGHT)
        .position(PANEL_COLUMN_A_LEFT, PanelBuilder.DEFAULT_HEADER_SIZE + PANEL_MARGIN);

    this.infoMaterialLblValue = builder().label()
        .text("-")
        .size(PANEL_COLUMN_B_WIDTH, PANEL_ROW_HEIGHT)
        .position(PANEL_COLUMN_B_LEFT, PanelBuilder.DEFAULT_HEADER_SIZE + PANEL_MARGIN);

    return builder().panel()
        .position(left, top)
        .size(PANEL_WIDTH, PANEL_HEIGHT)
        .text("Information")
        .child(
            infoFpsLbl, this.infoMaterialLblValue
        );

  }

  private PanelBuilder createLightPanel(int left, int top) {

    LabelBuilder lightDirLbl = builder().label()
        .text("Directional")
        .size(PANEL_COLUMN_A_WIDTH, PANEL_ROW_HEIGHT)
        .position(PANEL_COLUMN_A_LEFT, PanelBuilder.DEFAULT_HEADER_SIZE + PANEL_MARGIN);

    ToggleButtonBuilder lightDirTgl = builder().toggleButton()
        .position(PANEL_COLUMN_B_LEFT, PanelBuilder.DEFAULT_HEADER_SIZE + PANEL_MARGIN + PANEL_MARGIN)
        .size(PANEL_COLUMN_B_WIDTH, PANEL_ROW_HEIGHT - PANEL_MARGIN * 2)
        .isToggledSupplier(() -> context.getKeyMapping().getState(GLFW_KEY_1))
        .onClick(_ -> context.getKeyMapping().execute(GLFW_KEY_1));

    LabelBuilder lightPtLbl = builder().label()
        .text("Point")
        .size(PANEL_COLUMN_A_WIDTH, PANEL_ROW_HEIGHT)
        .position(PANEL_COLUMN_A_LEFT, PanelBuilder.DEFAULT_HEADER_SIZE + PANEL_MARGIN + PANEL_ROW_HEIGHT + PANEL_MARGIN );

    ToggleButtonBuilder lightPtTgl = builder().toggleButton()
        .position(PANEL_COLUMN_B_LEFT, PanelBuilder.DEFAULT_HEADER_SIZE + PANEL_MARGIN + PANEL_MARGIN + PANEL_ROW_HEIGHT + PANEL_MARGIN)
        .size(PANEL_COLUMN_B_WIDTH, PANEL_ROW_HEIGHT - PANEL_MARGIN * 2)
        .isToggledSupplier(() -> context.getKeyMapping().getState(GLFW_KEY_2))
        .onClick(_ -> context.getKeyMapping().execute(GLFW_KEY_2));

    LabelBuilder lightSptLbl = builder().label()
        .text("Spot")
        .size(PANEL_COLUMN_A_WIDTH, PANEL_ROW_HEIGHT)
        .position(PANEL_COLUMN_A_LEFT, PanelBuilder.DEFAULT_HEADER_SIZE + PANEL_MARGIN + (PANEL_ROW_HEIGHT + PANEL_MARGIN) * 2 );

    ToggleButtonBuilder lightSptTgl = builder().toggleButton()
        .position(PANEL_COLUMN_B_LEFT, PanelBuilder.DEFAULT_HEADER_SIZE + PANEL_MARGIN + PANEL_MARGIN + (PANEL_ROW_HEIGHT + PANEL_MARGIN) * 2 )
        .size(PANEL_COLUMN_B_WIDTH, PANEL_ROW_HEIGHT - PANEL_MARGIN * 2)
        .isToggledSupplier(() -> context.getKeyMapping().getState(GLFW_KEY_3))
        .onClick(_ -> context.getKeyMapping().execute(GLFW_KEY_3));

    return builder().panel()
        .position(left, top)
        .size(PANEL_WIDTH, PANEL_HEIGHT)
        .text("Lights")
        .child(
            lightDirLbl, lightDirTgl,
            lightPtLbl, lightPtTgl,
            lightSptLbl, lightSptTgl
        );
  }

  private PanelBuilder createMeshPanel(int left, int top) {
    return builder().panel()
        .position(left, top)
        .size(PANEL_WIDTH, PANEL_HEIGHT)
        .text("Mesh");
  }

  private PanelBuilder createMaterialPanel(int left, int top) {

    ComboboxBuilder matRendererCmb = builder().combobox()
        .size(PANEL_COLUMN_A_WIDTH + PANEL_MARGIN + PANEL_COLUMN_B_WIDTH , PANEL_ROW_HEIGHT - PANEL_MARGIN * 2)
        .position(PANEL_COLUMN_A_LEFT, PanelBuilder.DEFAULT_HEADER_SIZE + PANEL_MARGIN + PANEL_MARGIN);

    LabelBuilder matNormMappingLbl = builder().label()
        .text("Normal Mapping")
        .size(PANEL_COLUMN_A_WIDTH, PANEL_ROW_HEIGHT)
        .position(PANEL_COLUMN_A_LEFT, PanelBuilder.DEFAULT_HEADER_SIZE + PANEL_MARGIN + PANEL_ROW_HEIGHT + PANEL_MARGIN );

    ToggleButtonBuilder matNormMappingTgl = builder().toggleButton()
        .position(PANEL_COLUMN_B_LEFT, PanelBuilder.DEFAULT_HEADER_SIZE + PANEL_MARGIN + PANEL_MARGIN + PANEL_ROW_HEIGHT + PANEL_MARGIN)
        .size(PANEL_COLUMN_B_WIDTH, PANEL_ROW_HEIGHT - PANEL_MARGIN * 2)
        .isToggledSupplier(() -> context.getKeyMapping().getState(GLFW_KEY_4))
        .onClick(_ -> context.getKeyMapping().execute(GLFW_KEY_4));

    return builder().panel()
        .position(left, top)
        .size(PANEL_WIDTH, PANEL_HEIGHT)
        .text("Material")
        .child(
            matRendererCmb,
            matNormMappingLbl, 
            matNormMappingTgl
        );
  }

  @Override
  protected void build(long tpf) {
    this.updateFPS(tpf);
    this.desktop.build();
  }

  private long cumul = 0;
  private int nbPass = 0;
  private void updateFPS(long tpf) {

    cumul += tpf;
    nbPass++;

    if (cumul > 1000) {
      this.infoMaterialLblValue.text("%d".formatted(nbPass));
      cumul = 0;
      nbPass = 0;
    }

  }

}

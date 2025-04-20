package org.sc.viewer.gamestate;

import org.sc.themis.renderer.material.MaterialManager;
import org.sc.themis.renderer.material.MaterialRenderer;
import org.sc.themis.renderer.pencil2d.Pencil2D;
import org.sc.themis.scene.Scene;
import org.sc.themis.scene.ui.UiSceneController;
import org.sc.themis.scene.ui.component.*;
import org.sc.themis.scene.ui.component.combobox.ComboboxBuilder;
import org.sc.viewer.ViewerContext;

import static org.lwjgl.glfw.GLFW.*;

public class ViewerUi extends UiSceneController {

  private final ViewerContext context;
  private final MaterialManager materialManager;

  private final static int PANEL_WIDTH  = 240;
  private final static int PANEL_HEIGHT = 120;
  private final static int PANEL_SPACE  =  10;

  private final static int PANEL_MARGIN = 1;
  private final static int PANEL_COLUMN_A_LEFT  = PanelBuilder.DEFAULT_BORDER_SIZE + PANEL_MARGIN;
  private final static int PANEL_COLUMN_A_WIDTH = 190;
  private final static int PANEL_COLUMN_B_LEFT  = PANEL_COLUMN_A_LEFT + PANEL_COLUMN_A_WIDTH;
  private final static int PANEL_COLUMN_B_WIDTH = PANEL_WIDTH - PanelBuilder.DEFAULT_BORDER_SIZE - PANEL_COLUMN_B_LEFT - PANEL_MARGIN * 2;

  private final static int PANEL_ROW_HEIGHT = 20;

  private final ContainerBuilder desktop;
  private LabelBuilder infoMaterialLblValue;

  public ViewerUi(Scene scene, Pencil2D pencil, ViewerContext context, MaterialManager materialManager) {

    super(scene, pencil);

    this.context = context;
    this.materialManager = materialManager;

    PanelBuilder infoPnl = createInfoPanel(PANEL_SPACE, PANEL_SPACE);
    PanelBuilder lightPnl = createLightPanel(PANEL_SPACE, PANEL_SPACE * 2 + PANEL_HEIGHT);
    PanelBuilder meshPnl = createMeshPanel(PANEL_SPACE, PANEL_SPACE * 3 + PANEL_HEIGHT* 2);
    PanelBuilder materialPnl = createMaterialPanel(PANEL_SPACE, PANEL_SPACE * 4 + PANEL_HEIGHT * 3);

    this.desktop = builder().container();
    this.desktop.child(infoPnl, lightPnl, meshPnl, materialPnl);

  }

  private PanelBuilder createInfoPanel(int left, int top) {

    LabelBuilder infoFpsLbl = builder().label()
        .text("Fps")
        .size(PANEL_COLUMN_A_WIDTH, PANEL_ROW_HEIGHT)
        .position(PANEL_MARGIN, PANEL_MARGIN);

    this.infoMaterialLblValue = builder().label()
        .text("-")
        .size(PANEL_COLUMN_B_WIDTH, PANEL_ROW_HEIGHT)
        .position(PANEL_COLUMN_B_LEFT, PANEL_MARGIN);

    LabelBuilder tbnLbl = builder().label()
        .size(PANEL_COLUMN_A_WIDTH, PANEL_ROW_HEIGHT)
        .position(PANEL_MARGIN, (PANEL_ROW_HEIGHT + PANEL_MARGIN) + PANEL_MARGIN)
        .text("TBN Vectors");

    ToggleButtonBuilder tbnTgl = builder().toggleButton()
        .size(PANEL_COLUMN_B_WIDTH, PANEL_ROW_HEIGHT)
        .position(PANEL_COLUMN_B_LEFT, (PANEL_ROW_HEIGHT + PANEL_MARGIN) + PANEL_MARGIN)
        .isToggledSupplier(() -> context.getKeyMapping().getState(GLFW_KEY_F1))
        .onClick((_,_) -> context.getKeyMapping().execute(GLFW_KEY_F1));

    LabelBuilder gridLbl = builder().label()
        .size(PANEL_COLUMN_A_WIDTH, PANEL_ROW_HEIGHT)
        .position(PANEL_MARGIN, (PANEL_ROW_HEIGHT + PANEL_MARGIN) * 2 + PANEL_MARGIN )
        .text("Grid");

    ToggleButtonBuilder gridTgl = builder().toggleButton()
        .size(PANEL_COLUMN_B_WIDTH, PANEL_ROW_HEIGHT)
        .position(PANEL_COLUMN_B_LEFT, (PANEL_ROW_HEIGHT + PANEL_MARGIN) * 2 + PANEL_MARGIN )
        .isToggledSupplier(() -> context.getKeyMapping().getState(GLFW_KEY_F2))
        .onClick((_,_) -> context.getKeyMapping().execute(GLFW_KEY_F2));


    return builder().panel()
        .size(PANEL_WIDTH, PANEL_HEIGHT)
        .position(left, top)
        .text("Information")
        .child(
            infoFpsLbl, this.infoMaterialLblValue,
            tbnLbl, tbnTgl,
            gridLbl, gridTgl
        );

  }

  private PanelBuilder createLightPanel(int left, int top) {

    LabelBuilder lightDirLbl = builder().label()
        .size(PANEL_COLUMN_A_WIDTH, PANEL_ROW_HEIGHT)
        .position(PANEL_MARGIN, PANEL_MARGIN)
        .text("Directional");

    ToggleButtonBuilder lightDirTgl = builder().toggleButton()
        .size(PANEL_COLUMN_B_WIDTH, PANEL_ROW_HEIGHT)
        .position(PANEL_COLUMN_B_LEFT, PANEL_MARGIN)
        .isToggledSupplier(() -> context.getKeyMapping().getState(GLFW_KEY_1))
        .onClick((_,_) -> context.getKeyMapping().execute(GLFW_KEY_1));

    LabelBuilder lightPtLbl = builder().label()
        .size(PANEL_COLUMN_A_WIDTH, PANEL_ROW_HEIGHT)
        .position(PANEL_MARGIN, (PANEL_ROW_HEIGHT + PANEL_MARGIN) + PANEL_MARGIN)
        .text("Point");

    ToggleButtonBuilder lightPtTgl = builder().toggleButton()
        .size(PANEL_COLUMN_B_WIDTH, PANEL_ROW_HEIGHT)
        .position(PANEL_COLUMN_B_LEFT, (PANEL_ROW_HEIGHT + PANEL_MARGIN) + PANEL_MARGIN)
        .isToggledSupplier(() -> context.getKeyMapping().getState(GLFW_KEY_2))
        .onClick((_,_) -> context.getKeyMapping().execute(GLFW_KEY_2));

    LabelBuilder lightSptLbl = builder().label()
        .size(PANEL_COLUMN_A_WIDTH, PANEL_ROW_HEIGHT)
        .position(PANEL_MARGIN, (PANEL_ROW_HEIGHT + PANEL_MARGIN) * 2 + PANEL_MARGIN )
        .text("Spot");

    ToggleButtonBuilder lightSptTgl = builder().toggleButton()
        .size(PANEL_COLUMN_B_WIDTH, PANEL_ROW_HEIGHT)
        .position(PANEL_COLUMN_B_LEFT, (PANEL_ROW_HEIGHT + PANEL_MARGIN) * 2 + PANEL_MARGIN )
        .isToggledSupplier(() -> context.getKeyMapping().getState(GLFW_KEY_3))
        .onClick((_,_) -> context.getKeyMapping().execute(GLFW_KEY_3));

    return builder().panel()
        .size(PANEL_WIDTH, PANEL_HEIGHT)
        .position(left, top)
        .text("Lights")
        .child(
            lightDirLbl, lightDirTgl,
            lightPtLbl, lightPtTgl,
            lightSptLbl, lightSptTgl
        );
  }

  private PanelBuilder createMeshPanel(int left, int top) {
    return builder().panel()
        .size(PANEL_WIDTH, PANEL_HEIGHT)
        .position(left, top)
        .text("Mesh");
  }

  private PanelBuilder createMaterialPanel(int left, int top) {

    ComboboxBuilder<MaterialRenderer> matCbx = builder().<MaterialRenderer>combobox()
        .size( PANEL_COLUMN_A_WIDTH + PANEL_MARGIN + PANEL_COLUMN_B_WIDTH, PANEL_ROW_HEIGHT)
        .position(PANEL_MARGIN, PANEL_MARGIN);

    this.materialManager.getMaterialRenderers().forEach(r -> matCbx.content(r));
    matCbx
        .onSelect( (c) -> {
          this.context.setActiveRenderer(c);
          context.getKeyMapping().execute("TRIGGER.CHANGE.RENDERER");
        })
        .selectionSupplier(this.context::activeRenderer);

    LabelBuilder matNormMappingLbl = builder().label()
        .size(PANEL_COLUMN_A_WIDTH, PANEL_ROW_HEIGHT)
        .position(PANEL_MARGIN, (PANEL_ROW_HEIGHT + PANEL_MARGIN) + PANEL_MARGIN)
        .text("Normal Mapping");

    ToggleButtonBuilder matNormMappingTgl = builder().toggleButton()
        .size(PANEL_COLUMN_B_WIDTH, PANEL_ROW_HEIGHT)
        .position(PANEL_COLUMN_B_LEFT, (PANEL_ROW_HEIGHT + PANEL_MARGIN) + PANEL_MARGIN)
        .isToggledSupplier(() -> context.getKeyMapping().getState(GLFW_KEY_4))
        .onClick((_,_) -> context.getKeyMapping().execute(GLFW_KEY_4));

    LabelBuilder matEmissiveLbl = builder().label()
        .size(PANEL_COLUMN_A_WIDTH, PANEL_ROW_HEIGHT)
        .position(PANEL_MARGIN, (PANEL_ROW_HEIGHT + PANEL_MARGIN) * 2 + PANEL_MARGIN)
        .text("Emissive");

    ToggleButtonBuilder matEmissiveTgl = builder().toggleButton()
        .size(PANEL_COLUMN_B_WIDTH, PANEL_ROW_HEIGHT)
        .position(PANEL_COLUMN_B_LEFT, (PANEL_ROW_HEIGHT + PANEL_MARGIN) * 2 + PANEL_MARGIN)
        .isToggledSupplier(() -> context.getKeyMapping().getState(GLFW_KEY_5))
        .onClick((_,_) -> context.getKeyMapping().execute(GLFW_KEY_5));

    return builder().panel()
        .position(left, top)
        .size(PANEL_WIDTH, PANEL_HEIGHT)
        .text("Material")
        .child(
            matCbx,
            matNormMappingLbl, matNormMappingTgl,
            matEmissiveLbl, matEmissiveTgl
        );
        //.childVisibilityRule(matCbx.getComboboxContent(), matCbx::isOpen);
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

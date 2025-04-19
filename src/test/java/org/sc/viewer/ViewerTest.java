package org.sc.viewer;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.sc.TestWithConfiguration;
import org.sc.themis.engine.Engine;
import org.sc.themis.renderer.material.MaterialManager;
import org.sc.themis.renderer.resource.ResourceEnum;
import org.sc.themis.renderer.resource.ResourceLoader;
import org.sc.themis.renderer.resource.font.FontRepository;
import org.sc.themis.renderer.resource.font.FontResourceDescriptor;
import org.sc.themis.shared.exception.ThemisException;
import org.sc.viewer.gamestate.ViewerGamestate;
import org.sc.viewer.renderactivity.ViewerRendererActivity;
import org.sc.viewer.renderactivity.geometry.material.ColorMaterialRenderer;
import org.sc.viewer.renderactivity.geometry.material.NoLightColorMaterialRenderer;
import org.sc.viewer.renderactivity.geometry.material.TextureMaterialRenderer;

import java.nio.file.Path;

public class ViewerTest extends TestWithConfiguration {

  @Test
  @Disabled
  void runViewer() throws ThemisException {

    // Given
    MaterialManager materialManager = new MaterialManager();
    materialManager.setMaterialRenderers(
        new TextureMaterialRenderer(getConfiguration()),
        new ColorMaterialRenderer(getConfiguration()),
        new NoLightColorMaterialRenderer(getConfiguration())
    );
    ViewerContext context = ViewerContext.createDefault(materialManager.getDefaultMaterialRenderer());

    FontRepository fRepository = new FontRepository();
    try {
      fRepository.load(ResourceLoader.get().get(ResourceEnum.FONT, FontResourceDescriptor.sdf( Path.of("CenturyGothic.ttf"), 14, 0.47f, 0.060f )));
      fRepository.load(ResourceLoader.get().get(ResourceEnum.FONT, FontResourceDescriptor.sdf( Path.of("CenturyGothic.ttf"), 16, 0.46f, 0.09f )));
    } catch (ThemisException e) {
      e.printStackTrace(); //todo
    }

    ViewerGamestate gamestate = new ViewerGamestate(context, fRepository, materialManager);
    Engine engine = new Engine(getConfiguration(), new ViewerRendererActivity(getConfiguration(), context, gamestate, materialManager));

    // When
    engine.setup();
    engine.setGamestate(gamestate);
    engine.run();

    // Then

    // Cleanup
    engine.cleanup();

  }
}

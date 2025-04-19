package org.sc.viewer;

import org.sc.themis.engine.Engine;
import org.sc.themis.renderer.material.MaterialManager;
import org.sc.themis.renderer.resource.ResourceEnum;
import org.sc.themis.renderer.resource.font.FontRepository;
import org.sc.themis.renderer.resource.font.FontResourceDescriptor;
import org.sc.themis.shared.configuration.Configuration;
import org.sc.themis.shared.exception.ThemisException;
import org.sc.themis.renderer.resource.ResourceLoader;
import org.sc.viewer.gamestate.ViewerGamestate;
import org.sc.viewer.renderactivity.ViewerRendererActivity;
import org.sc.viewer.renderactivity.geometry.material.ColorMaterialRenderer;
import org.sc.viewer.renderactivity.geometry.material.NoLightColorMaterialRenderer;
import org.sc.viewer.renderactivity.geometry.material.TextureMaterialRenderer;

import java.nio.file.Path;

public class Bootstrap {

  public static void main(String[] args) throws ThemisException {
    new Bootstrap().run();
  }

  private void run() throws ThemisException {

    Configuration configuration = new Configuration("./application.properties");
    ResourceLoader.get().apply(configuration);

    MaterialManager mManager = new MaterialManager();
    mManager.setMaterialRenderers(
        new TextureMaterialRenderer(configuration),
        new ColorMaterialRenderer(configuration),
        new NoLightColorMaterialRenderer(configuration)
    );

    ViewerContext   context  = ViewerContext.createDefault(mManager.getDefaultMaterialRenderer());

    FontRepository fRepository = new FontRepository();
    try {
      fRepository.load(ResourceLoader.get().get(ResourceEnum.FONT, FontResourceDescriptor.sdf( Path.of("CenturyGothic.ttf"), 14, 0.47f, 0.060f )));
      fRepository.load(ResourceLoader.get().get(ResourceEnum.FONT, FontResourceDescriptor.sdf( Path.of("CenturyGothic.ttf"), 16, 0.46f, 0.09f )));
    } catch (ThemisException e) {
      e.printStackTrace(); //todo
    }

    ViewerGamestate gamestate = new ViewerGamestate(context, fRepository, mManager);
    ViewerRendererActivity activity = new ViewerRendererActivity(configuration, context, gamestate, mManager);

    Engine engine = new Engine(configuration, activity);
    engine.setup();

    engine.setGamestate(gamestate);
    engine.run();

  }

}

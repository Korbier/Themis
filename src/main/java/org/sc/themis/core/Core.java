package org.sc.themis.core;

import org.sc.themis.engine.Engine;
import org.sc.themis.gamestate.Gamestate;
import org.sc.themis.renderer.RendererActivity;
import org.sc.themis.shared.assertion.Assertions;
import org.sc.themis.shared.configuration.Configuration;
import org.sc.themis.shared.exception.ThemisException;
import org.sc.themis.shared.function.ConsumerWithException;

import java.util.ArrayList;
import java.util.List;

public class Core {

  public static CoreBuilder builder() {
    return new CoreBuilder();
  }

  private final String configuration;
  private final Gamestate gamestate;
  private final Class<? extends Gamestate> gamestateType;
  private final RendererActivity rendererActivity;
  private final Class<? extends RendererActivity> rendererActivityType;
  private final List<Configurer<?>> configurers;

  private Core(CoreBuilder builder) {
    this.configuration = builder.configuration;
    this.gamestate = builder.gamestate;
    this.gamestateType = builder.gamestateType;
    this.rendererActivity = builder.rendererActivity;
    this.rendererActivityType = builder.rendererActivityType;
    this.configurers = builder.configurers;
  }

  public void run() throws ThemisException {

    Assertions.notNull(this.configuration, new ThemisException());

    try (CoreDiContext diContext = new CoreDiContext().start()) {

      Configuration conf = diContext.select(Configuration.class);
      Gamestate gamestate = this.gamestate != null ? this.gamestate : diContext.select(this.gamestateType);
      RendererActivity activity = this.rendererActivity != null ? this.rendererActivity : diContext.select(this.rendererActivityType);

      //Chargement de la configuration
      if (this.configuration != null) {
        conf.load(this.configuration);
      }

      //Configure caller specifics beans
      if (this.configurers != null) {
        for (Configurer configurer : this.configurers) {
          configurer.consumer().accept(diContext.select(configurer.type()));
        }
      }

      //Engine instanciation
      Engine engine = new Engine(conf, activity);
      engine.setGamestate(gamestate);
      engine.setup();

      engine.run();

    }

  }

  public String configuration() {
    return configuration;
  }

  public Class<? extends Gamestate> gamestateType() {
    return gamestateType;
  }

  public Class<? extends RendererActivity> rendererActivity() {
    return rendererActivityType;
  }

  public static class CoreBuilder {

    private String configuration;
    private Gamestate gamestate;
    private Class<? extends Gamestate> gamestateType;
    private RendererActivity rendererActivity;
    private Class<? extends RendererActivity> rendererActivityType;
    private List<Configurer<?>> configurers;

    public Core build() {
      return new Core(this);
    }

    public CoreBuilder configuration(String configuration) {
      this.configuration = configuration;
      return this;
    }

    public CoreBuilder gamestate(Gamestate gamestate) {
      this.gamestate = gamestate;
      return this;
    }

    public CoreBuilder gamestate(Class<? extends Gamestate> gamestateType) {
      this.gamestateType = gamestateType;
      return this;
    }

    public CoreBuilder rendererActivity(RendererActivity activity) {
      this.rendererActivity = activity;
      return this;
    }

    public CoreBuilder rendererActivity(Class<? extends RendererActivity> rendererActivity) {
      this.rendererActivityType = rendererActivity;
      return this;
    }

    public <T> CoreBuilder configure(Class<T> type, ConsumerWithException<ThemisException, T> configurer) {

      if (this.configurers == null) {
        this.configurers = new ArrayList<>();
      }

      this.configurers.add(new Configurer<>(type, configurer));

      return this;

    }

  }

  private record Configurer<T>(Class<T> type, ConsumerWithException<ThemisException, T> consumer) {}

}

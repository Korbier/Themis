package org.sc.playground;

import org.sc.themis.core.Core;
import org.sc.themis.shared.exception.ThemisException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Bootstrap {

  private static final Logger logger = LoggerFactory.getLogger(Bootstrap.class);

  public static void main(String[] args) throws ThemisException {
    new Bootstrap().run();
  }

  public int run(String... args) throws ThemisException {

    Playgrounds playground = selectPlayground(args);
    logger.info("Running {} playground ...", playground);

    Core.builder()
        .configuration("./src/main/resources/application.properties")
        .gamestate(playground.gamestate)
        .rendererActivity(playground.rendererActivityFactory.get())
        .build()
        .run();

    return 0;

  }

  private Playgrounds selectPlayground(String[] args) {

    if (args.length > 0) {
      return Playgrounds.fromName(args[0]);
    }

    return Playgrounds.NOOP;
  }
}

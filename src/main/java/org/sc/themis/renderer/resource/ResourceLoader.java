package org.sc.themis.renderer.resource;

import org.sc.themis.shared.configuration.Configuration;
import org.sc.themis.shared.configuration.ConfigurationEnum;
import org.sc.themis.shared.exception.ThemisException;
import org.sc.themis.renderer.resource.base.exception.ResourceFileNotFoundException;
import org.sc.themis.renderer.resource.base.ResourceDescriptor;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public class ResourceLoader {

  private static final org.slf4j.Logger logger = LoggerFactory.getLogger(ResourceLoader.class);

  private static final ResourceLoader _instance = new ResourceLoader();
  private ResourceLoader() {}

  public static ResourceLoader get() {
    return _instance;
  }

  private Path root = Path.of("./src/main/resources");

  public void apply(Configuration configuration) {
    this.root = Path.of(configuration.get(ConfigurationEnum.pathResources));
  }

  public <T, D extends ResourceDescriptor> T get(ResourceType<T, D> type, D descriptor) throws ThemisException {
    Path fullpath = this.root.resolve( type.path() ).resolve( descriptor.name() );
    return getFromFullPath(type, descriptor, fullpath);
  }

  public <T, D extends ResourceDescriptor> T get(ResourceType<T, D> type, D descriptor, Path root) throws ThemisException {
    Path fullpath = root.resolve( descriptor.name() );
    return getFromFullPath(type, descriptor, fullpath);
  }

  private <T, D extends ResourceDescriptor> T getFromFullPath(ResourceType<T, D> type, D descriptor, Path fullpath) throws ThemisException {

    if (!fullpath.toFile().exists()) {
      throw new ResourceFileNotFoundException(fullpath);
    }

    logger.debug("Loading resource {} (type={})", fullpath, type);
    return type.get(fullpath, descriptor);
  }

}

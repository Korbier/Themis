package org.sc.themis.renderer.base;

import org.sc.themis.shared.exception.ThemisException;
import org.sc.themis.shared.function.BiFunctionWithException;
import org.sc.themis.renderer.resource.base.ResourceDescriptor;

import java.nio.file.Path;

public class ResourceType<T, D extends ResourceDescriptor> {

  public static <O, I extends ResourceDescriptor> ResourceType<O,I> of(Class<O> type, Path path, BiFunctionWithException<Path, I, O> loadFunction, String ... extensions ) {
    ResourceType<O, I> rType = new ResourceType<>();
    rType.type = type;
    rType.path = path;
    rType.extensions = extensions;
    rType.loadFunction = loadFunction;
    return rType;
  }

  private Class<T> type;
  private Path path;
  private String [] extensions;
  private BiFunctionWithException<Path, D, T> loadFunction;

  public Path path() {
    return path;
  }

  public T get(Path root, D descriptor) throws ThemisException {
    return this.loadFunction.apply(root, descriptor);
  }

  @Override
  public String toString() {
    return this.type.getCanonicalName();
  }
}

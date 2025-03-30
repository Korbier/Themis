package org.sc.themis.renderer.resource.base.exception;

import org.sc.themis.shared.exception.ThemisException;

import java.nio.file.Path;

public class ResourceFileNotFoundException extends ThemisException {

  private static final String MESSAGE = "File %s not found";

  public ResourceFileNotFoundException(Path path) {
    super(MESSAGE.formatted(path.toAbsolutePath().toString()));
  }
}

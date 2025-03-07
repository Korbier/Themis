package org.sc.themis.scene.exception;

import java.nio.file.Path;
import org.sc.themis.shared.exception.ThemisException;

public class ModelFileNotFoundException extends ThemisException {

  private static final String MESSAGE = "Model file %s not found";

  public ModelFileNotFoundException(Path path) {
    super(MESSAGE.formatted(path.toAbsolutePath().toString()));
  }
}

package org.sc.themis.shared.resource.exception;

import org.sc.themis.shared.exception.ThemisException;

public class ImageNotLoadedException extends ThemisException {

  private static final String MESSAGE = "File %s not loaded. reason : %s ";

  public ImageNotLoadedException(String path, String reason) {
    super(MESSAGE.formatted(path, reason));
  }
}

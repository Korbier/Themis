package org.sc.themis.renderer.base.exception;

import org.sc.themis.shared.exception.ThemisException;

public class NoPhysicalDeviceFoundException extends ThemisException {

  public NoPhysicalDeviceFoundException() {
    super("No physical device found.");
  }
}

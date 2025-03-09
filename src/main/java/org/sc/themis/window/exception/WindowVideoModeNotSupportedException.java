package org.sc.themis.window.exception;

import org.sc.themis.shared.exception.ThemisException;

public class WindowVideoModeNotSupportedException extends ThemisException {

  private static final String MESSAGE = "Video mode not supported";

  public WindowVideoModeNotSupportedException() {
    super(MESSAGE);
  }
}

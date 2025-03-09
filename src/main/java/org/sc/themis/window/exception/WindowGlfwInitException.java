package org.sc.themis.window.exception;

import org.sc.themis.shared.exception.ThemisException;

public class WindowGlfwInitException extends ThemisException {

  private static final String MESSAGE = "Cannot initialize GLFW";

  public WindowGlfwInitException() {
    super(MESSAGE);
  }
}

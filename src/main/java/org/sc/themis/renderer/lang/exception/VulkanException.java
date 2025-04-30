package org.sc.themis.renderer.lang.exception;

import org.sc.themis.shared.exception.ThemisException;

public class VulkanException extends ThemisException {

  private final int code;

  public VulkanException(int code, String message) {
    super(code + " = " + message);
    this.code = code;
  }

  public VulkanException(String message, Throwable cause) {
    super(message, cause);
    this.code = -1;
  }

  public int getCode() {
    return this.code;
  }

}

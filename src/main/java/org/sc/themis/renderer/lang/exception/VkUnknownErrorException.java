package org.sc.themis.renderer.lang.exception;

public class VkUnknownErrorException extends VulkanException {

  public VkUnknownErrorException(int errorCode) {
    super(errorCode, "Unknown error");
  }

}

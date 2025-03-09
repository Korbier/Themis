package org.sc.themis.renderer.base.device.layer;

public interface VkLayer {

  static VkLayer of(String name) {
    return () -> name;
  }

  String getName();
}

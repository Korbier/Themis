package org.sc.viewer.renderactivity.postprocess;

public interface PostProcessor {

  enum DrawFrequency {
    DRAW_ONCE,
    DRAW_EVERY_VERTEX
  }

  String getIdentifier();

  DrawFrequency getFrequency();

  byte[] getVertexShader();

  byte[] getGeometryShader();

  byte[] getFragmentShader();
}

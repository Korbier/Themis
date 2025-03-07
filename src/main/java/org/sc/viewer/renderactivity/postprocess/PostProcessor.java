package org.sc.viewer.renderactivity.postprocess;

public interface PostProcessor {

  enum Frequency {
    ONCE,
    PER_VERTEX
  }

  String getIdentifier();

  Frequency getFrequency();

  byte[] getVertexShader();

  byte[] getGeometryShader();

  byte[] getFragmentShader();
}

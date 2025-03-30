package org.sc.themis.renderer.resource.shader;

public class ShaderSource {

  private byte[] content;

  public ShaderSource(byte[] content) {
    this.content = content;
  }

  public byte[] getContent() {
    return this.content;
  }

}

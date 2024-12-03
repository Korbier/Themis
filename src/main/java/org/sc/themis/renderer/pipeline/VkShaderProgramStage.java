package org.sc.themis.renderer.pipeline;

public record VkShaderProgramStage(
    int shaderStage,
    byte [] source
) {
}

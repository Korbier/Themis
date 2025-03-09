package org.sc.themis.renderer.base.pipeline;

public record VkPushConstantRange(int stage, int offset, int size) {}

package org.sc.themis.renderer.base.command;

public record VkCommandInheritanceInfo(long vkRenderPass, long vkFrameBuffer, int subPass) {}

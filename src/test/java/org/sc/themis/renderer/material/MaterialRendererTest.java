package org.sc.themis.renderer.material;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sc.themis.renderer.base.pipeline.VkPipelineDescriptor;
import org.sc.themis.renderer.base.pipeline.VkVertexInputStateDescriptor;
import org.sc.themis.shared.utils.MemorySizeUtils;

import static org.lwjgl.vulkan.VK10.*;

public class MaterialRendererTest {


  @Test
  @DisplayName("CreateMaterialRenderer - Success")
  public void testCreateMaterialRenderer() {

    MaterialRenderer<MaterialRendererProperties> renderer = MaterialRenderer.builder()
        .shaderStage(VK_SHADER_STAGE_VERTEX_BIT,new byte[0])
        .shaderStage(VK_SHADER_STAGE_FRAGMENT_BIT,new byte[0])
        .pushConstantRange(VK_SHADER_STAGE_VERTEX_BIT, 0, MemorySizeUtils.MAT4x4F)
        .vertexInputStateDescriptor(new VkVertexInputStateDescriptor(VK_VERTEX_INPUT_RATE_VERTEX))
        .pipelineDescriptor(new VkPipelineDescriptor(null, 0, false, 1, true, 1, 1, 1))
        .mainLayout(
          MaterialVariantLayout.builder()
              .uniformDynamic(0, VK_SHADER_STAGE_FRAGMENT_BIT, null)
              .uniformDynamic(1, VK_SHADER_STAGE_FRAGMENT_BIT, null)
              .uniformDynamic(2, VK_SHADER_STAGE_FRAGMENT_BIT, null)
              .build()
        )
        .variantLayout(
            MaterialVariantLayout.builder()
                .uniformDynamic(0, VK_SHADER_STAGE_FRAGMENT_BIT, null)
                .uniformDynamic(1, VK_SHADER_STAGE_FRAGMENT_BIT, null)
                .uniformDynamic(2, VK_SHADER_STAGE_FRAGMENT_BIT, null)
                .build()
        )
        .mainConsumer((variant, properties) -> {})
        .varianConsumer((variant, material) -> {})
        .build();

  }

}


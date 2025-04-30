package org.sc.themis.renderer.lang;

import org.lwjgl.vulkan.VK10;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.function.Consumer;

public class VulkanTest {

  protected void mockVK10(
    Consumer<MockedStatic<VK10>> mockConsumer,
    Runnable assertRunnable
  ) {
    try (MockedStatic<VK10> mockedVK10 = Mockito.mockStatic(VK10.class)) {
      mockConsumer.accept(mockedVK10);
      assertRunnable.run();
    }
  }

}

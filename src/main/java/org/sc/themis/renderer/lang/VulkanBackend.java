package org.sc.themis.renderer.lang;

import java.util.function.Supplier;
import org.lwjgl.vulkan.VK10;
import org.sc.themis.renderer.lang.exception.VkUnknownErrorException;
import org.sc.themis.renderer.lang.exception.VulkanException;
import org.sc.themis.shared.function.ConsumerWithException;

public class VulkanBackend {

  /**
   * Executes a Vulkan operation using the provided supplier and handles errors with the given
   * consumer.
   *
   * @param supplier the supplier for obtaining the Vulkan operation result
   * @param consumerWithException the consumer for handling Vulkan errors
   * @throws E if an unknown error occurs during the Vulkan operation
   */
  protected <E extends VulkanException> void vk(Supplier<Integer> supplier, ConsumerWithException<E, Integer> consumerWithException) throws E {

    int errno = supplier.get();

    if (errno == VK10.VK_SUCCESS) {
      return;
    }

    consumerWithException.accept(errno);

    throw (E) new VkUnknownErrorException(errno);

  }

  /**
   * Executes a Vulkan operation using the provided callable and handles errors.
   *
   * @param executor the executor for executing the Vulkan operation
   * @throws VulkanException if an unknown error occurs during the Vulkan operation
   */
  protected void vk(VulkanExecutor executor) throws VulkanException {
    try {
      executor.execute();
    } catch (Exception exception) {
      throw new VulkanException("Unknown error", exception);
    }
  }

  protected interface VulkanExecutor {
    void execute();
  }

}

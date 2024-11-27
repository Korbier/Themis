package org.sc.themis.renderer.base;

import org.lwjgl.vulkan.VK10;
import org.sc.themis.renderer.exception.VulkanException;
import org.sc.themis.shared.exception.ThemisException;
import org.sc.themis.shared.function.ConsumerWithException;

import java.util.function.Supplier;

public class Vulkan {

    /**
     * Executes a Vulkan operation using the provided supplier and handles errors with the given consumer.
     *
     * @param supplier               the supplier for obtaining the Vulkan operation result
     * @param consumerWithException    the consumer for handling Vulkan errors
     * @throws ThemisException       if an unknown error occurs during the Vulkan operation
     */
    protected void vk(Supplier<Integer> supplier, ConsumerWithException<Integer> consumerWithException) throws ThemisException {

        int errno = supplier.get();

        if ( errno == VK10.VK_SUCCESS ) {
            return;
        }

        consumerWithException.accept( errno );

        throw new VulkanException(errno, "Unknown error");

    }

    /**
     * Executes a Vulkan operation using the provided callable and handles errors.
     *
     * @param executor         the executor for executing the Vulkan operation
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

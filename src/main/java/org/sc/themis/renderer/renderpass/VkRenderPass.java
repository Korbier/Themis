package org.sc.themis.renderer.renderpass;

import org.jboss.logging.Logger;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkAttachmentDescription;
import org.lwjgl.vulkan.VkAttachmentReference;
import org.lwjgl.vulkan.VkRenderPassCreateInfo;
import org.lwjgl.vulkan.VkSubpassDescription;
import org.sc.themis.renderer.base.VulkanObject;
import org.sc.themis.renderer.device.VkDevice;
import org.sc.themis.shared.Configuration;
import org.sc.themis.shared.exception.ThemisException;

import java.nio.LongBuffer;
import java.util.List;
import java.util.Map;

import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_RENDER_PASS_CREATE_INFO;

public class VkRenderPass extends VulkanObject {

    private static final org.jboss.logging.Logger LOG = Logger.getLogger(VkRenderPass.class);

    private final VkDevice device;
    private final VkRenderPassDescriptor descriptor;

    private long handle;

    public VkRenderPass(Configuration configuration, VkDevice device, VkRenderPassDescriptor descriptor ) {
        super(configuration);
        this.device = device;
        this.descriptor = descriptor;
    }

    @Override
    public void setup() throws ThemisException {

        try (MemoryStack stack = MemoryStack.stackPush() ) {

            VkAttachmentDescription.Buffer attachmentDescription = createAttachmentDescription(stack);
            VkSubpassDescription.Buffer subpassDescription = createSubpassDescription(stack);
            org.lwjgl.vulkan.VkSubpassDependency.Buffer dependencies = createSubpassDependencies(stack);

            VkRenderPassCreateInfo renderPassCreateInfo = createRenderPassCreateInfo(stack, attachmentDescription, subpassDescription, dependencies);

            this.handle = vkCreateRenderPass(stack, renderPassCreateInfo);

        }

        LOG.trace("Renderpass initialised");

    }

    @Override
    public void cleanup() throws ThemisException {
        vkRenderPass().destroyRenderPass( this.device.getHandle(), this.handle );
    }

    public long getHandle() {
        return this.handle;
    }

    public VkRenderPassDescriptor getDescriptor() {
        return this.descriptor;
    }

    private long vkCreateRenderPass( MemoryStack stack, VkRenderPassCreateInfo renderPassCreateInfo ) throws ThemisException {
        LongBuffer pRenderPass = stack.mallocLong(1);
        vkRenderPass().createRenderPass( this.device.getHandle(), renderPassCreateInfo, pRenderPass );
        return pRenderPass.get(0);
    }

    private VkRenderPassCreateInfo createRenderPassCreateInfo(
            MemoryStack stack,
            VkAttachmentDescription.Buffer attachmentDescription,
            VkSubpassDescription.Buffer subpassDescription,
            org.lwjgl.vulkan.VkSubpassDependency.Buffer dependencies
    ) {
        return VkRenderPassCreateInfo.calloc(stack)
                .sType(VK_STRUCTURE_TYPE_RENDER_PASS_CREATE_INFO)
                .pAttachments(attachmentDescription)
                .pSubpasses(subpassDescription)
                .pDependencies(dependencies);
    }

    private org.lwjgl.vulkan.VkSubpassDependency.Buffer createSubpassDependencies( MemoryStack stack ) {

        List<VkSubpassDependency> dependencies = this.descriptor.getDependencies();
        org.lwjgl.vulkan.VkSubpassDependency.Buffer subpassDependencies = org.lwjgl.vulkan.VkSubpassDependency.calloc(dependencies.size(), stack);

        int i = 0;

        for ( VkSubpassDependency dependency : dependencies ) {

            subpassDependencies.get(i)
                    .srcSubpass( dependency.srcSubPass() )
                    .dstSubpass( dependency.dstSubPass() )
                    .srcStageMask(dependency.srcStageMask()) //https://registry.khronos.org/vulkan/specs/1.3-extensions/man/html/VkPipelineStageFlagBits.html
                    .dstStageMask(dependency.dstStageMask())
                    .srcAccessMask(dependency.srcAccessMask())
                    .dstAccessMask(dependency.dstAccessMask());

            if ( dependency.flag() > 0 ) {
                subpassDependencies.get(i).dependencyFlags(dependency.flag());
            }

            i++;

        }

        return subpassDependencies;

    }

    private VkAttachmentDescription.Buffer createAttachmentDescription( MemoryStack stack ) {

        VkRenderPassLayout layout = this.descriptor.getLayout();
        VkAttachmentDescription.Buffer attachmentsDesc = VkAttachmentDescription.calloc( layout.size(), stack );

        int i = 0;

        for ( Integer idx : layout.keys() ) {

            VkRenderPassAttachment attachment = layout.get( idx );

            attachmentsDesc.get(i++)
                    .format(attachment.format())
                    .initialLayout(attachment.initialLayout()).finalLayout(attachment.finalLayout())
                    .loadOp(attachment.loadOp()).storeOp(attachment.storeOp())
                    .stencilLoadOp(attachment.stencilLoadOp()).stencilStoreOp(attachment.stencilStoreOp())
                    .samples( attachment.sampleCount() );

        }

        return attachmentsDesc;

    }

    private VkSubpassDescription.Buffer createSubpassDescription(MemoryStack stack) {

        VkSubpassDescription.Buffer subpassDesc = VkSubpassDescription.calloc( this.descriptor.getSubpasses().size(), stack);

        int i = 0;

        for ( VkSubpass subpass : this.descriptor.getSubpasses() ) {

            VkAttachmentReference.Buffer inputReferences = createInputReferences( stack, subpass );
            VkAttachmentReference.Buffer colorReferences = createColorReferences( stack, subpass );
            VkAttachmentReference.Buffer resolveReferences = createResolveReferences( stack, subpass );
            VkAttachmentReference depthReference = createDepthReferences( stack, subpass );

            VkSubpassDescription description = subpassDesc.get( i++ );

            description.pipelineBindPoint( subpass.getPipelineBindPoint() );

            if ( inputReferences != null ) {
                description.pInputAttachments( inputReferences );
            }

            if ( colorReferences != null ) {
                description.pColorAttachments( colorReferences );
                description.colorAttachmentCount( colorReferences.capacity() );
            }

            if ( resolveReferences != null ) {
                description.pResolveAttachments( resolveReferences );
            }

            if ( depthReference != null ) {
                description.pDepthStencilAttachment( depthReference );
            }

        }

        return subpassDesc;

    }

    private VkAttachmentReference.Buffer createInputReferences(MemoryStack stack, VkSubpass subpass) {
        return createReferences( stack, subpass.inputs() );
    }

    private VkAttachmentReference.Buffer createColorReferences(MemoryStack stack, VkSubpass subpass) {
        return createReferences( stack, subpass.colors() );
    }

    private VkAttachmentReference.Buffer createResolveReferences(MemoryStack stack, VkSubpass subpass) {
        return createReferences( stack, subpass.resolves() );
    }

    private VkAttachmentReference createDepthReferences(MemoryStack stack, VkSubpass subpass) {

        if ( subpass.depthIndex() == -1 ) {
            return null;
        }

        VkAttachmentReference inputReference = VkAttachmentReference.calloc(stack);
        inputReference.attachment( subpass.depthIndex() ).layout( subpass.depthLayout() );

        return inputReference;

    }

    private VkAttachmentReference.Buffer createReferences( MemoryStack stack, Map<Integer, Integer> layouts ) {

        if ( layouts.isEmpty() ) {
            return null;
        }

        VkAttachmentReference.Buffer inputReferences = VkAttachmentReference.calloc(layouts.size(), stack);

        int i = 0;
        for ( Map.Entry<Integer, Integer> layout : layouts.entrySet() ) {
            inputReferences.get( i++ ).attachment( layout.getKey() ).layout( layout.getValue() );
        }

        return inputReferences;

    }

}

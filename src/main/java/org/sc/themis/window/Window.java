package org.sc.themis.window;

import org.jboss.logging.Logger;
import org.joml.Vector2i;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.glfw.GLFWVulkan;
import org.lwjgl.system.MemoryUtil;
import org.sc.themis.shared.TObject;
import org.sc.themis.shared.assertion.Assertions;
import org.sc.themis.shared.exception.ThemisException;
import org.sc.themis.window.exception.WindowGlfwInitException;
import org.sc.themis.window.exception.WindowVideoModeNotSupportedException;
import org.sc.themis.window.exception.WindowVukanNotSupportedException;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;

public class Window extends TObject<WindowDescriptor> {

    private static final Logger LOG = Logger.getLogger(Window.class);

    private final Vector2i size = new Vector2i();
    private final Vector2i resolution = new Vector2i();
    private final List<Runnable> pollListeners = new ArrayList<>();

    private Long handle;
    private boolean resized = false;

    public Window( WindowDescriptor descriptor ) {
        super( descriptor );
    }

    @Override
    public void setup() throws ThemisException {

        LOG.tracef( "Window setup" );

        Assertions.isTrue( org.lwjgl.glfw.GLFW::glfwInit, new WindowGlfwInitException() );
        Assertions.isTrue( GLFWVulkan::glfwVulkanSupported, new WindowVukanNotSupportedException() );

        GLFWVidMode vidMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        Assertions.notNull( vidMode, new WindowVideoModeNotSupportedException() );

        setupAttributes( vidMode, getDescriptor().width(), getDescriptor().height() );
        setupWindow( getDescriptor().title(), getDescriptor().resizable(), getDescriptor().maximized() );
        setupCallback();

        LOG.tracef( "Window initialized (Size=%dx%d, Resolution=%d,%d)", this.size.x, this.size.y, this.resolution.x, this.resolution.y );

    }

    @Override
    public void cleanup() {
        LOG.tracef( "Window setup" );
        glfwSetWindowShouldClose( this.getHandle(), true);
    }

    private void setupAttributes(GLFWVidMode vidMode, int width, int height) {
        this.resolution.x = vidMode.width();
        this.resolution.y = vidMode.height();
        this.size.x = width > 0 ? width : vidMode.width();
        this.size.y = height > 0 ? height : vidMode.height();
    }

    private void setupWindow( String title, boolean isResizable, boolean isMaximized ) {

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_CLIENT_API, GLFW_NO_API);//Don't create OpenGl context
        glfwWindowHint(GLFW_RESIZABLE, isResizable ? GLFW_TRUE : GLFW_FALSE);
        glfwWindowHint(GLFW_MAXIMIZED, isMaximized ? GLFW_TRUE : GLFW_FALSE);

        this.handle = glfwCreateWindow(this.size.x, this.size.y, title, MemoryUtil.NULL, MemoryUtil.NULL);

        if ( handle == MemoryUtil.NULL ) {
            throw new RuntimeException("Cannot create window");
        }

    }

    private void setupCallback() {
        glfwSetFramebufferSizeCallback( handle, (window, w, h) -> this.resize(w, h) );
    }

    public Long getHandle() {
        return this.handle;
    }

    public Vector2i getResolution() {
        return resolution;
    }

    public Vector2i getSize() {
        return size;
    }

    public boolean shouldClose() {
        return glfwWindowShouldClose( this.getHandle() );
    }

    public void poll() {
        glfwPollEvents();
        firePollListeners();
    }

    public void addPollListener( Runnable runnable ) {
        this.pollListeners.add( runnable );
    }

    public void removePollListener( Runnable runnable ) {
        this.pollListeners.remove( runnable );
    }

    private void firePollListeners() {
        this.pollListeners.forEach( Runnable::run );
    }

    /** Window Resizing Management **/
    public void setResized(boolean resized) {
        this.resized = resized;
    }

    public void resetResized() {
        this.resized = false;
    }

    public void resize(int width, int height) {
        this.resized = true;
        this.size.set( width, height );
    }

    public boolean isResized() {
        return this.resized;
    }

}

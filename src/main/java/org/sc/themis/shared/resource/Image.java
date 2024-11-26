package org.sc.themis.shared.resource;

import org.lwjgl.system.MemoryStack;
import org.sc.themis.shared.assertion.Assertions;
import org.sc.themis.shared.resource.exception.ImageNotLoadedException;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.stb.STBImage.stbi_failure_reason;
import static org.lwjgl.stb.STBImage.stbi_load;

public class Image {

    private final String path;
    private final ByteBuffer buffer;
    private final int width;
    private final int height;

    public static Image of( String path ) throws ImageNotLoadedException {

        try (MemoryStack stack = MemoryStack.stackPush() ){

            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            IntBuffer channels = stack.mallocInt(1);

            ByteBuffer data = stbi_load(path, w, h, channels, 4);
            Assertions.notNull( data, new ImageNotLoadedException( path, stbi_failure_reason() ));

            return new Image(path, data, w.get(), h.get());

        }

    }

    public static Image of( float r, float g, float b, float a ) {

        int red   = (int) r * 255;
        int green = (int) g * 255;
        int blue  = (int) b * 255;
        int alpha = (int) a * 255;

        ByteBuffer data = ByteBuffer.allocate( 4 );
        data.put( (byte) red );
        data.put( (byte) green );
        data.put( (byte) blue );
        data.put( (byte)  alpha );
        data.rewind();

        return new Image( "generated_" + r + "_" + g + "_" + b + "_" + a , data, 1, 1 );

    }

    private Image(String path, ByteBuffer buffer, int width, int height ) {
        this.path = path;
        this.buffer = buffer;
        this.width = width;
        this.height = height;
    }

    public String getPath() {
        return path;
    }

    public ByteBuffer getBuffer() {
        return buffer;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

}

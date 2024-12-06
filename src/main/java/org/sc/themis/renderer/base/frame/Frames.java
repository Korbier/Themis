package org.sc.themis.renderer.base.frame;

import org.sc.themis.renderer.base.VulkanObject;
import org.sc.themis.shared.exception.ThemisException;
import org.sc.themis.shared.function.BiConsumerWithException;
import org.sc.themis.shared.function.ConsumerWithException;
import org.sc.themis.shared.function.FunctionWithException;
import org.sc.themis.shared.function.SupplierWithException;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class Frames {

    private final int size;
    private final Frame [] frames;

    public Frames( int size ) {
        this( size, false, false );
    }

    public Frames( int size, boolean autoSetup, boolean autoCleanup ) {
        this.size = size;
        this.frames = new Frame[ this.size ];
        for ( int i = 0; i<this.size; i++) {
            this.frames[i] = new Frame(autoSetup, autoCleanup);
        }
    }

    public void cleanup() throws ThemisException {
        for ( Frame frame : this.frames ) {
            frame.cleanup();
        }
    }

    public Frame get( int idx ) {
        return this.frames[idx];
    }

    public <T extends VulkanObject> T get( int idx, FrameKey<T> key ) {
        return this.frames[idx].get( key );
    }

    public <T extends VulkanObject> void create( FrameKey<T> key, SupplierWithException<T> supplier ) throws ThemisException {
        for ( Frame frame : this.frames ) {
            frame.create( key, supplier );
        }
    }

    public <T extends VulkanObject> void create( FrameKey<T> key, FunctionWithException<Integer, T> function ) throws ThemisException {
        for ( int i = 0; i<this.size; i++) {
            frames[i].create( key, i, function );
        }
    }

    public <T extends VulkanObject> void update( FrameKey<T> key, ConsumerWithException<T> consumer ) throws ThemisException {
        for ( Frame frame : this.frames ) {
            frame.update( key, consumer );
        }
    }

    public <T extends VulkanObject> void update( FrameKey<T> key, BiConsumerWithException<Integer, T> consumer ) throws ThemisException {
        for ( int i = 0; i<this.size; i++) {
            frames[i].update( key, i, consumer );
        }
    }

    public <T extends VulkanObject> T update( int idx, FrameKey<T> key, ConsumerWithException<T> consumer ) throws ThemisException {
        return this.frames[idx].update( key, consumer );
    }

    public <T extends VulkanObject> void remove( FrameKey<T> key ) throws ThemisException {
        for ( Frame frame : this.frames ) {
            frame.remove( key );
        }
    }

}

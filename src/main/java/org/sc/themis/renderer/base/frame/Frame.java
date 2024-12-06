package org.sc.themis.renderer.base.frame;

import org.sc.themis.renderer.base.VulkanObject;
import org.sc.themis.shared.exception.ThemisException;
import org.sc.themis.shared.function.BiConsumerWithException;
import org.sc.themis.shared.function.ConsumerWithException;
import org.sc.themis.shared.function.FunctionWithException;
import org.sc.themis.shared.function.SupplierWithException;

import java.util.HashMap;
import java.util.Map;

public class Frame {

    private final Map<FrameKey<? extends VulkanObject>, VulkanObject> content = new HashMap<>();

    public <T extends VulkanObject> T get( FrameKey<T> key ) {
        return (T) this.content.get( key );
    }

    public void cleanup() throws ThemisException {
        for ( VulkanObject o : this.content.values() ) {
            o.cleanup();
        }
    }

    <T extends VulkanObject> T create( FrameKey<T> key, SupplierWithException<T> supplier ) throws ThemisException {
        T data = supplier.get();
        this.content.put( key, data );
        return data;
    }

    <T extends VulkanObject> T create( FrameKey<T> key, int frame, FunctionWithException<Integer, T> function ) throws ThemisException {
        T data = function.apply( frame );
        this.content.put( key, data );
        return data;
    }

    <T extends VulkanObject> T update( FrameKey<T> key, ConsumerWithException<T> consumer ) throws ThemisException {
        T data = (T) this.content.get( key );
        consumer.accept( data );
        return data;
    }

    <T extends VulkanObject> T update( FrameKey<T> key, int frame, BiConsumerWithException<Integer, T> consumer ) throws ThemisException {
        T data = (T) this.content.get( key );
        consumer.accept( frame, data );
        return data;
    }

    <T extends VulkanObject> T remove( FrameKey<T> key, boolean cleanup ) throws ThemisException {
        T data = (T) this.content.remove( key );
        if ( cleanup ) data.cleanup();
        return data;
    }

}

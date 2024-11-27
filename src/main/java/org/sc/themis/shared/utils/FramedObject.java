package org.sc.themis.shared.utils;

import org.sc.themis.shared.exception.ThemisException;
import org.sc.themis.shared.function.BiConsumerWithException;
import org.sc.themis.shared.function.ConsumerWithException;
import org.sc.themis.shared.function.FunctionWithException;
import org.sc.themis.shared.function.SupplierWithException;

import java.util.HashMap;
import java.util.Map;

public class FramedObject<FRAMED> {

    private Map<Integer, FRAMED> frames;

    public static <O> FramedObject<O> of(int count, SupplierWithException<O> initializer ) throws ThemisException {
        FramedObject<O> frame = new FramedObject<>();
        frame.init( count, initializer );
        return frame;
    }

    public static <O> FramedObject<O> of(int count, FunctionWithException<Integer, O> initializer ) throws ThemisException {
        FramedObject<O> frame = new FramedObject<>();
        frame.init( count, initializer );
        return frame;
    }

    private FramedObject() {}

    public void accept(BiConsumerWithException<Integer, FRAMED> consumer ) throws ThemisException {
        for ( Map.Entry<Integer, FRAMED> entry : this.frames.entrySet() ) {
            consumer.accept( entry.getKey(), entry.getValue() );
        }
    }

    public void accept(ConsumerWithException<FRAMED> consumer ) throws ThemisException {
        for ( FRAMED framed : this.frames.values() ) {
            consumer.accept( framed );
        }
    }

    public FRAMED get( int frame ) {
        return this.frames.get( frame );
    }

    private void init(int count, FunctionWithException<Integer, FRAMED> initializer ) throws ThemisException {

        this.frames = new HashMap<>(count);

        for ( int i=0; i<count; i++) {
            this.frames.put( i, initializer.apply( i ) );
        }

    }

    private void init( int count, SupplierWithException<FRAMED> initializer ) throws ThemisException {

        this.frames = new HashMap<>(count);

        for ( int i=0; i<count; i++) {
            this.frames.put( i, initializer.get() );
        }

    }

}

package org.sc.themis.scene.pen;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

public class DrawCommand {

    private final Queue<DrawVertex> vertices = new ArrayBlockingQueue<DrawVertex>(1024);

    public void put(DrawVertex a, DrawVertex b, DrawVertex c) {
        this.vertices.add(a);
        this.vertices.add(b);
        this.vertices.add(c);
    }

    public float[] toArray() {

        float[] array = new float[this.vertices.size() * DrawVertex.SIZE];
        int i = 0;

        for (DrawVertex vertex : this.vertices) {
            array[i++] = vertex.position().x();
            array[i++] = vertex.position().y();
            array[i++] = vertex.texture().x();
            array[i++] = vertex.texture().y;
        }

        return array;

    }

}

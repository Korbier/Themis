package org.sc.themis.scene.pencil;

import org.joml.Vector3f;

public class Pencil {

    private static final int COMPONENT_COUNT = 7;

    private float [] data = new float[0];
    private int [] indices = new int[0];

    public int getDataSize() {
        return this.data.length;
    }

    public float[] getData() {
        return this.data;
    }

    public int getIndiceSize() {
        return this.indices.length;
    }

    public int [] getIndices() {
        return this.indices;
    }

    public boolean isRenderable() {
        return this.data.length > 0 && this.indices.length > 0;
    }

    public Pencil clear() {
        this.data = new float[0];
        this.indices = new int[0];
        return this;
    }

    public Pencil drawTriangle(float x1, float y1, float x2, float y2, float x3, float y3, float r, float g, float b) {

        int startIndiceOffset = getDataSize() / COMPONENT_COUNT;

        float [] data = extendData(3);
        setData(data, getDataSize(), x1, y1, 0.0f, 0.0f, r, g, b);
        setData(data, getDataSize() + COMPONENT_COUNT, x2, y2, 0.0f, 0.0f, r, g, b);
        setData(data, getDataSize() + COMPONENT_COUNT * 2, x3, y3, 0.0f, 0.0f, r, g, b);
        this.data = data;

        int[] indices = extendIndices(3);
        setIndice(indices, getIndiceSize(), startIndiceOffset);
        setIndice(indices, getIndiceSize() + 1, startIndiceOffset + 1);
        setIndice(indices, getIndiceSize() + 2, startIndiceOffset + 2);
        this.indices = indices;

        return this;
    }

    public Pencil drawRect(float x, float y, float width, float height, float r, float g, float b) {

        int startIndiceOffset = getDataSize() / COMPONENT_COUNT;

        float [] data = extendData(4);
        setData(data, getDataSize(), x, y, 0.0f, 0.0f, r, g, b);
        setData(data, getDataSize() + COMPONENT_COUNT, x + width, y, 1.0f, 0.0f, r, g, b);
        setData(data, getDataSize() + COMPONENT_COUNT * 2, x + width, y + height, 1.0f, 1.0f, r, g, b);
        setData(data, getDataSize() + COMPONENT_COUNT * 3, x, y + height, 0.0f, 1.0f, r, g, b);
        this.data = data;

        int[] indices = extendIndices(6);
        setIndice(indices, getIndiceSize(), startIndiceOffset);
        setIndice(indices, getIndiceSize() + 1, startIndiceOffset + 1);
        setIndice(indices, getIndiceSize() + 2, startIndiceOffset + 3);
        setIndice(indices, getIndiceSize() + 3, startIndiceOffset + 1);
        setIndice(indices, getIndiceSize() + 4, startIndiceOffset + 2);
        setIndice(indices, getIndiceSize() + 5, startIndiceOffset + 3);
        this.indices = indices;

        return this;
    }

    private float [] extendData(int additionalSize) {
        float [] data = new float[this.data.length + additionalSize * COMPONENT_COUNT];
        System.arraycopy(this.data, 0, data, 0, this.data.length);
        return data;
    }

    private int [] extendIndices(int additionalSize) {
        int [] indices = new int[this.indices.length + additionalSize];
        System.arraycopy(this.indices, 0, indices, 0, this.indices.length);
        return indices;
    }

    private void setData(float[] data, int idx, float x, float y, float u, float v, float r, float g, float b) {
        data[idx] = x;
        data[idx + 1] = y;
        data[idx + 2] = u;
        data[idx + 3] = v;
        data[idx + 4] = r;
        data[idx + 5] = g;
        data[idx + 6] = b;
    }

    private void setIndice(int[] indices, int idx, int value) {
        indices[idx] = value;
    }

}

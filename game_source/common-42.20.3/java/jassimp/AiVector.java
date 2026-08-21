/*
 * Decompiled with CFR 0.152.
 */
package jassimp;

import java.nio.ByteBuffer;

public final class AiVector {
    private final ByteBuffer m_buffer;
    private final int m_offset;
    private final int m_numComponents;

    public AiVector(ByteBuffer byteBuffer, int n, int n2) {
        if (null == byteBuffer) {
            throw new IllegalArgumentException("buffer may not be null");
        }
        this.m_buffer = byteBuffer;
        this.m_offset = n;
        this.m_numComponents = n2;
    }

    public float getX() {
        return this.m_buffer.getFloat(this.m_offset);
    }

    public float getY() {
        if (this.m_numComponents <= 1) {
            throw new UnsupportedOperationException("vector has only 1 component");
        }
        return this.m_buffer.getFloat(this.m_offset + 4);
    }

    public float getZ() {
        if (this.m_numComponents <= 2) {
            throw new UnsupportedOperationException("vector has only 2 components");
        }
        return this.m_buffer.getFloat(this.m_offset + 8);
    }

    public void setX(float f) {
        this.m_buffer.putFloat(this.m_offset, f);
    }

    public void setY(float f) {
        if (this.m_numComponents <= 1) {
            throw new UnsupportedOperationException("vector has only 1 component");
        }
        this.m_buffer.putFloat(this.m_offset + 4, f);
    }

    public void setZ(float f) {
        if (this.m_numComponents <= 2) {
            throw new UnsupportedOperationException("vector has only 2 components");
        }
        this.m_buffer.putFloat(this.m_offset + 8, f);
    }

    public int getNumComponents() {
        return this.m_numComponents;
    }

    public String toString() {
        return "[" + this.getX() + ", " + this.getY() + ", " + this.getZ() + "]";
    }
}


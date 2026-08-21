/*
 * Decompiled with CFR 0.152.
 */
package jassimp;

import java.nio.ByteBuffer;

public final class AiQuaternion {
    private final ByteBuffer m_buffer;
    private final int m_offset;

    public AiQuaternion(ByteBuffer byteBuffer, int n) {
        if (null == byteBuffer) {
            throw new IllegalArgumentException("buffer may not be null");
        }
        this.m_buffer = byteBuffer;
        this.m_offset = n;
    }

    public float getX() {
        return this.m_buffer.getFloat(this.m_offset + 4);
    }

    public float getY() {
        return this.m_buffer.getFloat(this.m_offset + 8);
    }

    public float getZ() {
        return this.m_buffer.getFloat(this.m_offset + 12);
    }

    public float getW() {
        return this.m_buffer.getFloat(this.m_offset);
    }

    public void setX(float f) {
        this.m_buffer.putFloat(this.m_offset + 4, f);
    }

    public void setY(float f) {
        this.m_buffer.putFloat(this.m_offset + 8, f);
    }

    public void setZ(float f) {
        this.m_buffer.putFloat(this.m_offset + 12, f);
    }

    public void setW(float f) {
        this.m_buffer.putFloat(this.m_offset, f);
    }

    public String toString() {
        return "[" + this.getX() + ", " + this.getY() + ", " + this.getZ() + ", " + this.getW() + "]";
    }
}


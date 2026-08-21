/*
 * Decompiled with CFR 0.152.
 */
package jassimp;

import java.nio.ByteBuffer;

public final class AiColor {
    private final ByteBuffer m_buffer;
    private final int m_offset;

    public AiColor(ByteBuffer byteBuffer, int n) {
        this.m_buffer = byteBuffer;
        this.m_offset = n;
    }

    public float getRed() {
        return this.m_buffer.getFloat(this.m_offset);
    }

    public float getGreen() {
        return this.m_buffer.getFloat(this.m_offset + 4);
    }

    public float getBlue() {
        return this.m_buffer.getFloat(this.m_offset + 8);
    }

    public float getAlpha() {
        return this.m_buffer.getFloat(this.m_offset + 12);
    }

    public void setRed(float f) {
        this.m_buffer.putFloat(this.m_offset, f);
    }

    public void setGreen(float f) {
        this.m_buffer.putFloat(this.m_offset + 4, f);
    }

    public void setBlue(float f) {
        this.m_buffer.putFloat(this.m_offset + 8, f);
    }

    public void setAlpha(float f) {
        this.m_buffer.putFloat(this.m_offset + 12, f);
    }

    public String toString() {
        return "[" + this.getRed() + ", " + this.getGreen() + ", " + this.getBlue() + ", " + this.getAlpha() + "]";
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package jassimp;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public final class AiMatrix4f {
    private final float[] m_data;

    public AiMatrix4f(float[] fArray) {
        if (fArray == null) {
            throw new IllegalArgumentException("data may not be null");
        }
        if (fArray.length != 16) {
            throw new IllegalArgumentException("array length is not 16");
        }
        this.m_data = fArray;
    }

    public float get(int n, int n2) {
        if (n < 0 || n > 3) {
            throw new IndexOutOfBoundsException("Index: " + n + ", Size: 4");
        }
        if (n2 < 0 || n2 > 3) {
            throw new IndexOutOfBoundsException("Index: " + n2 + ", Size: 4");
        }
        return this.m_data[n * 4 + n2];
    }

    public FloatBuffer toByteBuffer() {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(64);
        byteBuffer.order(ByteOrder.nativeOrder());
        FloatBuffer floatBuffer = byteBuffer.asFloatBuffer();
        floatBuffer.put(this.m_data);
        floatBuffer.flip();
        return floatBuffer;
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < 4; ++i) {
            for (int j = 0; j < 4; ++j) {
                stringBuilder.append(this.m_data[i * 4 + j]).append(" ");
            }
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }
}


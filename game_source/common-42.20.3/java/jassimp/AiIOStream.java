/*
 * Decompiled with CFR 0.152.
 */
package jassimp;

import java.nio.ByteBuffer;

public interface AiIOStream {
    public boolean read(ByteBuffer var1);

    public int getFileSize();
}


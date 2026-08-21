/*
 * Decompiled with CFR 0.152.
 */
package jassimp;

import jassimp.AiIOStream;

public interface AiIOSystem<T extends AiIOStream> {
    public T open(String var1, String var2);

    public boolean exists(String var1);

    public char getOsSeparator();

    public void close(T var1);
}


/*
 * Decompiled with CFR 0.152.
 */
package jassimp;

import java.nio.ByteBuffer;

public interface AiWrapperProvider<V3, M4, C, N, Q> {
    public V3 wrapVector3f(ByteBuffer var1, int var2, int var3);

    public M4 wrapMatrix4f(float[] var1);

    public C wrapColor(ByteBuffer var1, int var2);

    public N wrapSceneNode(Object var1, Object var2, int[] var3, String var4);

    public Q wrapQuaternion(ByteBuffer var1, int var2);
}


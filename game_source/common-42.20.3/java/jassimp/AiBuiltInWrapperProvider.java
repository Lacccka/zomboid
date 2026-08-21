/*
 * Decompiled with CFR 0.152.
 */
package jassimp;

import jassimp.AiColor;
import jassimp.AiMatrix4f;
import jassimp.AiNode;
import jassimp.AiQuaternion;
import jassimp.AiVector;
import jassimp.AiWrapperProvider;
import java.nio.ByteBuffer;

public final class AiBuiltInWrapperProvider
implements AiWrapperProvider<AiVector, AiMatrix4f, AiColor, AiNode, AiQuaternion> {
    @Override
    public AiVector wrapVector3f(ByteBuffer byteBuffer, int n, int n2) {
        return new AiVector(byteBuffer, n, n2);
    }

    @Override
    public AiMatrix4f wrapMatrix4f(float[] fArray) {
        return new AiMatrix4f(fArray);
    }

    @Override
    public AiColor wrapColor(ByteBuffer byteBuffer, int n) {
        return new AiColor(byteBuffer, n);
    }

    @Override
    public AiNode wrapSceneNode(Object object, Object object2, int[] nArray, String string) {
        return new AiNode((AiNode)object, object2, nArray, string);
    }

    @Override
    public AiQuaternion wrapQuaternion(ByteBuffer byteBuffer, int n) {
        return new AiQuaternion(byteBuffer, n);
    }
}


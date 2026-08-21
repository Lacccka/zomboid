/*
 * Decompiled with CFR 0.152.
 */
package jassimp;

import jassimp.AiAnimBehavior;
import jassimp.AiWrapperProvider;
import jassimp.Jassimp;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class AiNodeAnim {
    private final int POS_KEY_SIZE = Jassimp.NATIVE_AIVEKTORKEY_SIZE;
    private final int ROT_KEY_SIZE = Jassimp.NATIVE_AIQUATKEY_SIZE;
    private final int SCALE_KEY_SIZE = Jassimp.NATIVE_AIVEKTORKEY_SIZE;
    private final String m_nodeName;
    private final int m_numPosKeys;
    private ByteBuffer m_posKeys;
    private final int m_numRotKeys;
    private ByteBuffer m_rotKeys;
    private final int m_numScaleKeys;
    private ByteBuffer m_scaleKeys;
    private final AiAnimBehavior m_preState;
    private final AiAnimBehavior m_postState;

    AiNodeAnim(String string, int n, int n2, int n3, int n4, int n5) {
        this.m_nodeName = string;
        this.m_numPosKeys = n;
        this.m_numRotKeys = n2;
        this.m_numScaleKeys = n3;
        this.m_preState = AiAnimBehavior.fromRawValue(n4);
        this.m_postState = AiAnimBehavior.fromRawValue(n5);
        this.m_posKeys = ByteBuffer.allocateDirect(n * this.POS_KEY_SIZE);
        this.m_posKeys.order(ByteOrder.nativeOrder());
        this.m_rotKeys = ByteBuffer.allocateDirect(n2 * this.ROT_KEY_SIZE);
        this.m_rotKeys.order(ByteOrder.nativeOrder());
        this.m_scaleKeys = ByteBuffer.allocateDirect(n3 * this.SCALE_KEY_SIZE);
        this.m_scaleKeys.order(ByteOrder.nativeOrder());
    }

    public String getNodeName() {
        return this.m_nodeName;
    }

    public int getNumPosKeys() {
        return this.m_numPosKeys;
    }

    public ByteBuffer getPosKeyBuffer() {
        ByteBuffer byteBuffer = this.m_posKeys.duplicate();
        byteBuffer.order(ByteOrder.nativeOrder());
        return byteBuffer;
    }

    public double getPosKeyTime(int n) {
        return this.m_posKeys.getDouble(this.POS_KEY_SIZE * n);
    }

    public float getPosKeyX(int n) {
        return this.m_posKeys.getFloat(this.POS_KEY_SIZE * n + 8);
    }

    public float getPosKeyY(int n) {
        return this.m_posKeys.getFloat(this.POS_KEY_SIZE * n + 12);
    }

    public float getPosKeyZ(int n) {
        return this.m_posKeys.getFloat(this.POS_KEY_SIZE * n + 16);
    }

    public <V3, M4, C, N, Q> V3 getPosKeyVector(int n, AiWrapperProvider<V3, M4, C, N, Q> aiWrapperProvider) {
        return aiWrapperProvider.wrapVector3f(this.m_posKeys, this.POS_KEY_SIZE * n + 8, 3);
    }

    public int getNumRotKeys() {
        return this.m_numRotKeys;
    }

    public ByteBuffer getRotKeyBuffer() {
        ByteBuffer byteBuffer = this.m_rotKeys.duplicate();
        byteBuffer.order(ByteOrder.nativeOrder());
        return byteBuffer;
    }

    public double getRotKeyTime(int n) {
        return this.m_rotKeys.getDouble(this.ROT_KEY_SIZE * n);
    }

    public float getRotKeyW(int n) {
        return this.m_rotKeys.getFloat(this.ROT_KEY_SIZE * n + 8);
    }

    public float getRotKeyX(int n) {
        return this.m_rotKeys.getFloat(this.ROT_KEY_SIZE * n + 12);
    }

    public float getRotKeyY(int n) {
        return this.m_rotKeys.getFloat(this.ROT_KEY_SIZE * n + 16);
    }

    public float getRotKeyZ(int n) {
        return this.m_rotKeys.getFloat(this.ROT_KEY_SIZE * n + 20);
    }

    public <V3, M4, C, N, Q> Q getRotKeyQuaternion(int n, AiWrapperProvider<V3, M4, C, N, Q> aiWrapperProvider) {
        return aiWrapperProvider.wrapQuaternion(this.m_rotKeys, this.ROT_KEY_SIZE * n + 8);
    }

    public int getNumScaleKeys() {
        return this.m_numScaleKeys;
    }

    public ByteBuffer getScaleKeyBuffer() {
        ByteBuffer byteBuffer = this.m_scaleKeys.duplicate();
        byteBuffer.order(ByteOrder.nativeOrder());
        return byteBuffer;
    }

    public double getScaleKeyTime(int n) {
        return this.m_scaleKeys.getDouble(this.SCALE_KEY_SIZE * n);
    }

    public float getScaleKeyX(int n) {
        return this.m_scaleKeys.getFloat(this.SCALE_KEY_SIZE * n + 8);
    }

    public float getScaleKeyY(int n) {
        return this.m_scaleKeys.getFloat(this.SCALE_KEY_SIZE * n + 12);
    }

    public float getScaleKeyZ(int n) {
        return this.m_scaleKeys.getFloat(this.SCALE_KEY_SIZE * n + 16);
    }

    public <V3, M4, C, N, Q> V3 getScaleKeyVector(int n, AiWrapperProvider<V3, M4, C, N, Q> aiWrapperProvider) {
        return aiWrapperProvider.wrapVector3f(this.m_scaleKeys, this.SCALE_KEY_SIZE * n + 8, 3);
    }

    public AiAnimBehavior getPreState() {
        return this.m_preState;
    }

    public AiAnimBehavior getPostState() {
        return this.m_postState;
    }
}


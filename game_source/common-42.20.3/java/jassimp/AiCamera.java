/*
 * Decompiled with CFR 0.152.
 */
package jassimp;

import jassimp.AiWrapperProvider;

public final class AiCamera {
    private final String m_name;
    private final Object m_position;
    private final Object m_up;
    private final Object m_lookAt;
    private final float m_horizontalFOV;
    private final float m_clipNear;
    private final float m_clipFar;
    private final float m_aspect;

    AiCamera(String string, Object object, Object object2, Object object3, float f, float f2, float f3, float f4) {
        this.m_name = string;
        this.m_position = object;
        this.m_up = object2;
        this.m_lookAt = object3;
        this.m_horizontalFOV = f;
        this.m_clipNear = f2;
        this.m_clipFar = f3;
        this.m_aspect = f4;
    }

    public String getName() {
        return this.m_name;
    }

    public <V3, M4, C, N, Q> V3 getPosition(AiWrapperProvider<V3, M4, C, N, Q> aiWrapperProvider) {
        return (V3)this.m_position;
    }

    public <V3, M4, C, N, Q> V3 getUp(AiWrapperProvider<V3, M4, C, N, Q> aiWrapperProvider) {
        return (V3)this.m_up;
    }

    public <V3, M4, C, N, Q> V3 getLookAt(AiWrapperProvider<V3, M4, C, N, Q> aiWrapperProvider) {
        return (V3)this.m_lookAt;
    }

    public float getHorizontalFOV() {
        return this.m_horizontalFOV;
    }

    public float getClipPlaneNear() {
        return this.m_clipNear;
    }

    public float getClipPlaneFar() {
        return this.m_clipFar;
    }

    public float getAspect() {
        return this.m_aspect;
    }
}


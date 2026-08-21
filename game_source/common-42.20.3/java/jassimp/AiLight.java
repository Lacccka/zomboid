/*
 * Decompiled with CFR 0.152.
 */
package jassimp;

import jassimp.AiLightType;
import jassimp.AiWrapperProvider;

public final class AiLight {
    private final String m_name;
    private final AiLightType m_type;
    private final Object m_position;
    private final Object m_direction;
    private final float m_attenuationConstant;
    private final float m_attenuationLinear;
    private final float m_attenuationQuadratic;
    private final Object m_diffuse;
    private final Object m_specular;
    private final Object m_ambient;
    private final float m_innerCone;
    private final float m_outerCone;

    AiLight(String string, int n, Object object, Object object2, float f, float f2, float f3, Object object3, Object object4, Object object5, float f4, float f5) {
        this.m_name = string;
        this.m_type = AiLightType.fromRawValue(n);
        this.m_position = object;
        this.m_direction = object2;
        this.m_attenuationConstant = f;
        this.m_attenuationLinear = f2;
        this.m_attenuationQuadratic = f3;
        this.m_diffuse = object3;
        this.m_specular = object4;
        this.m_ambient = object5;
        this.m_innerCone = f4;
        this.m_outerCone = f5;
    }

    public String getName() {
        return this.m_name;
    }

    public AiLightType getType() {
        return this.m_type;
    }

    public <V3, M4, C, N, Q> V3 getPosition(AiWrapperProvider<V3, M4, C, N, Q> aiWrapperProvider) {
        return (V3)this.m_position;
    }

    public <V3, M4, C, N, Q> V3 getDirection(AiWrapperProvider<V3, M4, C, N, Q> aiWrapperProvider) {
        return (V3)this.m_direction;
    }

    public float getAttenuationConstant() {
        return this.m_attenuationConstant;
    }

    public float getAttenuationLinear() {
        return this.m_attenuationLinear;
    }

    public float getAttenuationQuadratic() {
        return this.m_attenuationQuadratic;
    }

    public <V3, M4, C, N, Q> C getColorDiffuse(AiWrapperProvider<V3, M4, C, N, Q> aiWrapperProvider) {
        return (C)this.m_diffuse;
    }

    public <V3, M4, C, N, Q> C getColorSpecular(AiWrapperProvider<V3, M4, C, N, Q> aiWrapperProvider) {
        return (C)this.m_specular;
    }

    public <V3, M4, C, N, Q> C getColorAmbient(AiWrapperProvider<V3, M4, C, N, Q> aiWrapperProvider) {
        return (C)this.m_ambient;
    }

    public float getAngleInnerCone() {
        return this.m_innerCone;
    }

    public float getAngleOuterCone() {
        return this.m_outerCone;
    }
}


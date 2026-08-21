/*
 * Decompiled with CFR 0.152.
 */
package jassimp;

import jassimp.AiTextureMapMode;
import jassimp.AiTextureOp;
import jassimp.AiTextureType;

public final class AiTextureInfo {
    private final AiTextureType m_type;
    private final int m_index;
    private final String m_file;
    private final int m_uvIndex;
    private final float m_blend;
    private final AiTextureOp m_textureOp;
    private final AiTextureMapMode m_textureMapModeU;
    private final AiTextureMapMode m_textureMapModeV;
    private final AiTextureMapMode m_textureMapModeW;

    AiTextureInfo(AiTextureType aiTextureType, int n, String string, int n2, float f, AiTextureOp aiTextureOp, AiTextureMapMode aiTextureMapMode, AiTextureMapMode aiTextureMapMode2, AiTextureMapMode aiTextureMapMode3) {
        this.m_type = aiTextureType;
        this.m_index = n;
        this.m_file = string;
        this.m_uvIndex = n2;
        this.m_blend = f;
        this.m_textureOp = aiTextureOp;
        this.m_textureMapModeU = aiTextureMapMode;
        this.m_textureMapModeV = aiTextureMapMode2;
        this.m_textureMapModeW = aiTextureMapMode3;
    }

    public AiTextureType getType() {
        return this.m_type;
    }

    public int getIndex() {
        return this.m_index;
    }

    public String getFile() {
        return this.m_file;
    }

    public int getUVIndex() {
        return this.m_uvIndex;
    }

    public float getBlend() {
        return this.m_blend;
    }

    public AiTextureOp getTextureOp() {
        return this.m_textureOp;
    }

    public AiTextureMapMode getTextureMapModeU() {
        return this.m_textureMapModeU;
    }

    public AiTextureMapMode getTextureMapModeV() {
        return this.m_textureMapModeV;
    }

    public AiTextureMapMode getTextureMapModeW() {
        return this.m_textureMapModeW;
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package jassimp;

public enum AiTextureOp {
    MULTIPLY(0),
    ADD(1),
    SUBTRACT(2),
    DIVIDE(3),
    SMOOTH_ADD(4),
    SIGNED_ADD(5);

    private final int m_rawValue;

    static AiTextureOp fromRawValue(int n) {
        for (AiTextureOp aiTextureOp : AiTextureOp.values()) {
            if (aiTextureOp.m_rawValue != n) continue;
            return aiTextureOp;
        }
        throw new IllegalArgumentException("unexptected raw value: " + n);
    }

    private AiTextureOp(int n2) {
        this.m_rawValue = n2;
    }
}


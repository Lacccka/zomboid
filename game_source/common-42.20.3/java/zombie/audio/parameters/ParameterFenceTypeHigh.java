/*
 * Decompiled with CFR 0.152.
 */
package zombie.audio.parameters;

import zombie.audio.FMODLocalParameter;
import zombie.util.StringUtils;

public final class ParameterFenceTypeHigh
extends FMODLocalParameter {
    public ParameterFenceTypeHigh() {
        super("FenceTypeHigh");
    }

    public static enum FenceType {
        WOOD("Wood", 0),
        METAL("Metal", 1),
        METAL_GATE("MetalGate", 2);

        private final String name;
        private final int value;

        private FenceType(String name, int value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return this.name;
        }

        public int getValue() {
            return this.value;
        }

        public static FenceType fromString(String name, FenceType defaultValue) {
            return StringUtils.tryParseEnum(FenceType.class, name, (e, s) -> e.getName().equals(s), defaultValue);
        }
    }
}


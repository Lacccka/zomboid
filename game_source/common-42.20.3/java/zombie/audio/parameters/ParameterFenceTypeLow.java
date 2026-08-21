/*
 * Decompiled with CFR 0.152.
 */
package zombie.audio.parameters;

import zombie.audio.FMODLocalParameter;
import zombie.util.StringUtils;

public final class ParameterFenceTypeLow
extends FMODLocalParameter {
    public ParameterFenceTypeLow() {
        super("FenceTypeLow");
    }

    public static enum FenceType {
        WOOD("Wood", 0),
        METAL("Metal", 1),
        SANDBAG("Sandbag", 2),
        GRAVELBAG("Gravelbag", 3),
        BARBWIRE("Barbwire", 4),
        ROADBLOCK("RoadBlock", 5),
        METAL_GATE("MetalGate", 6);

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


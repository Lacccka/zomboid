/*
 * Decompiled with CFR 0.152.
 */
package zombie.audio.parameters;

import zombie.audio.FMODLocalParameter;
import zombie.util.StringUtils;

public final class ParameterSinkType
extends FMODLocalParameter {
    public ParameterSinkType() {
        super("SinkType");
    }

    public static enum SinkType {
        GENERIC("Generic", 0),
        CERAMIC("Ceramic", 1),
        METAL("Metal", 2);

        private final String name;
        private final int value;

        private SinkType(String name, int value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return this.name;
        }

        public int getValue() {
            return this.value;
        }

        public static SinkType fromString(String name, SinkType defaultValue) {
            return StringUtils.tryParseEnum(SinkType.class, name, (e, s) -> e.getName().equals(s), defaultValue);
        }
    }
}


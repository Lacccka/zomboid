/*
 * Decompiled with CFR 0.152.
 */
package generation.builders;

import generation.builders.Writeable;
import java.io.IOException;
import java.io.Writer;
import zombie.scripting.objects.FluidKey;

public class FluidContainerFluidBuilder
implements Writeable {
    private final FluidKey type;
    private float percentage;
    private float[] color;

    public FluidContainerFluidBuilder(FluidKey type) {
        this.type = type;
    }

    public FluidContainerFluidBuilder percentage(float percentage) {
        this.percentage = percentage;
        return this;
    }

    public FluidContainerFluidBuilder color(float r, float g, float b) {
        this.color = new float[]{r, g, b};
        return this;
    }

    @Override
    public void write(Writer writer, int indent, String key) throws IOException {
        this.writeKeyValue(writer, indent, "fluid", "%s:%s%s".formatted(this.type, this.formatFloat(this.percentage / 100.0f), this.color == null ? "" : ":%s:%s:%s".formatted(this.formatFloat(this.color[0]), this.formatFloat(this.color[1]), this.formatFloat(this.color[2]))));
    }
}


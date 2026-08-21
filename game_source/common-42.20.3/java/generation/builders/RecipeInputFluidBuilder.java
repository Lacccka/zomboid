/*
 * Decompiled with CFR 0.152.
 */
package generation.builders;

import generation.builders.RecipeElement;
import generation.builders.Writeable;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import zombie.entity.components.crafting.FluidMatchMode;
import zombie.entity.components.crafting.InputFlag;
import zombie.entity.components.fluids.FluidCategory;
import zombie.scripting.objects.FluidKey;

public class RecipeInputFluidBuilder
implements RecipeElement,
Writeable {
    private InputType type;
    private final List<String> inputs = new ArrayList<String>();
    private final double amount;
    private final List<String> flags = new ArrayList<String>();
    private FluidMatchMode mode;

    public static RecipeInputFluidBuilder amount(double amount) {
        return new RecipeInputFluidBuilder(amount);
    }

    public RecipeInputFluidBuilder(double amount) {
        this.amount = amount;
    }

    public RecipeInputFluidBuilder type(FluidKey ... types) {
        this.addInputs(InputType.FLUID, (String[])Arrays.stream(types).map(FluidKey::toString).toArray(String[]::new));
        return this;
    }

    public RecipeInputFluidBuilder category(FluidCategory ... categories) {
        this.addInputs(InputType.CATEGORY, (String[])Arrays.stream(categories).map(Enum::toString).toArray(String[]::new));
        return this;
    }

    private void addInputs(InputType fluid, String[] types) {
        this.setType(fluid);
        Collections.addAll(this.inputs, types);
    }

    private void setType(InputType type) {
        if (type != this.type && this.type != null) {
            throw new IllegalStateException("Illegal recipe created, attempted to add %s after already adding %s".formatted(new Object[]{type, this.type}));
        }
        this.type = type;
    }

    public RecipeInputFluidBuilder flags(InputFlag ... flags) {
        Arrays.stream(flags).map(Enum::toString).forEach(this.flags::add);
        return this;
    }

    public RecipeInputFluidBuilder mode(FluidMatchMode mode) {
        this.mode = mode;
        return this;
    }

    @Override
    public void write(Writer writer, int indent, String key) throws IOException {
        writer.write("%s-fluid %s %s[%s]%s%s,\n".formatted(this.indent(indent), this.formatFloat((float)this.amount), this.type == InputType.CATEGORY ? "categories" : "", String.join((CharSequence)";", this.inputs), this.mode == null ? "" : " mode:%s".formatted(this.mode.name().substring(0, 1).toLowerCase() + this.mode.name().substring(1)), this.flags.isEmpty() ? "" : " flags[%s]".formatted(String.join((CharSequence)";", this.flags))));
    }

    public static enum InputType {
        FLUID,
        CATEGORY;

    }
}


/*
 * Decompiled with CFR 0.152.
 */
package generation.builders;

import generation.builders.RecipeElement;
import java.io.IOException;
import java.io.Writer;

public class RecipeEnergyBuilder
implements RecipeElement {
    private String input;
    private final int amount;
    private String apply;

    public RecipeEnergyBuilder(int amount) {
        this.amount = amount;
    }

    public static RecipeEnergyBuilder amount(int amount) {
        return new RecipeEnergyBuilder(amount);
    }

    public RecipeEnergyBuilder type(String type) {
        this.input = type;
        return this;
    }

    public RecipeEnergyBuilder apply(String apply2) {
        this.apply = apply2;
        return this;
    }

    @Override
    public void write(Writer writer, int indent, String key) throws IOException {
        this.writeValue(writer, indent, "energy %s %s%s".formatted(this.amount, this.input, this.apply == null ? "" : " apply:%s".formatted(this.apply)));
    }
}


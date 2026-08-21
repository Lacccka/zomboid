/*
 * Decompiled with CFR 0.152.
 */
package generation.builders;

import zombie.scripting.objects.EvolvedRecipeTemplateKey;

public record EvolvedRecipeHelper(EvolvedRecipeTemplateKey template, int number, boolean cooked) {
    @Override
    public String toString() {
        return "%s:%d%s".formatted(this.template(), this.number(), this.cooked ? "|Cooked" : "");
    }
}


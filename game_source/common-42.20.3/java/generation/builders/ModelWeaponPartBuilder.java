/*
 * Decompiled with CFR 0.152.
 */
package generation.builders;

import zombie.scripting.objects.ItemKey;
import zombie.scripting.objects.ModelKey;

public record ModelWeaponPartBuilder(ItemKey item, ModelKey model, String attachSelf, String attachParent) {
    public static ModelWeaponPartBuilder of(ItemKey item, ModelKey model) {
        return ModelWeaponPartBuilder.of(item, model, "none", "none");
    }

    public static ModelWeaponPartBuilder of(ItemKey item, ModelKey model, String attachSelf, String attachParent) {
        return new ModelWeaponPartBuilder(item, model, attachSelf, attachParent);
    }

    @Override
    public String toString() {
        return String.join((CharSequence)" ", this.item.id(), this.model.id(), this.attachSelf, this.attachParent);
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package generation;

import generation.ItemScriptGenerator;
import generation.builders.AnimalItemBuilder;
import generation.builders.ItemBuilder;
import zombie.scripting.objects.ItemDisplayCategory;
import zombie.scripting.objects.ItemKey;
import zombie.scripting.objects.ItemType;

public class ItemAnimalScriptGenerator
extends ItemScriptGenerator {
    public static void main(String ... args2) {
        ItemAnimalScriptGenerator.animal();
    }

    public static void animal() {
        ItemAnimalScriptGenerator.scriptFile("items/animal").add((ItemBuilder<?>)((AnimalItemBuilder)((AnimalItemBuilder)((AnimalItemBuilder)ItemBuilder.animal(ItemKey.Animal.ANIMAL).weight(0.1f)).itemType(ItemType.ANIMAL)).displayCategory(ItemDisplayCategory.GENERIC)).icon("WoolRaw")).write();
    }
}


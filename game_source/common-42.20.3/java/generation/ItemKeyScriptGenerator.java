/*
 * Decompiled with CFR 0.152.
 */
package generation;

import generation.ItemScriptGenerator;
import generation.builders.ItemBuilder;
import generation.builders.KeyItemBuilder;
import zombie.scripting.objects.ItemDisplayCategory;
import zombie.scripting.objects.ItemKey;
import zombie.scripting.objects.ItemTag;
import zombie.scripting.objects.ItemType;
import zombie.scripting.objects.ModelKey;

public class ItemKeyScriptGenerator
extends ItemScriptGenerator {
    public static void main(String ... args2) {
        ItemKeyScriptGenerator.key();
    }

    public static void key() {
        ItemKeyScriptGenerator.scriptFile("items/key").add((ItemBuilder<?>)((KeyItemBuilder)((KeyItemBuilder)((KeyItemBuilder)((KeyItemBuilder)((KeyItemBuilder)((KeyItemBuilder)((KeyItemBuilder)((KeyItemBuilder)((KeyItemBuilder)ItemBuilder.key(ItemKey.Key.KEY_1).displayCategory(ItemDisplayCategory.SECURITY)).itemType(ItemType.KEY)).weight(0.05f)).iconsForTexture("Key1", "Key2", "Key3", "Key4", "Key5")).metalValue(5.0f)).worldStaticModelsByIndex(ModelKey.KEY, ModelKey.KEY_2, ModelKey.KEY_3, ModelKey.KEY_3, ModelKey.KEY_2)).tags(ItemTag.BUILDING_KEY, ItemTag.MORE_WHEN_NO_ZOMBIES, ItemTag.FITS_WALLET, ItemTag.HAS_METAL)).originX(0)).originY(0)).originZ(0)).add((ItemBuilder<?>)((KeyItemBuilder)((KeyItemBuilder)((KeyItemBuilder)((KeyItemBuilder)((KeyItemBuilder)((KeyItemBuilder)ItemBuilder.key(ItemKey.Key.KEY_BLANK).displayCategory(ItemDisplayCategory.SECURITY)).itemType(ItemType.KEY)).weight(0.05f)).icon("Key_Blank")).metalValue(5.0f)).worldStaticModel(ModelKey.KEY_BLANK)).tags(ItemTag.FITS_WALLET, ItemTag.HAS_METAL)).add((ItemBuilder<?>)((KeyItemBuilder)((KeyItemBuilder)((KeyItemBuilder)((KeyItemBuilder)((KeyItemBuilder)((KeyItemBuilder)((KeyItemBuilder)ItemBuilder.key(ItemKey.Key.COMBINATION_PADLOCK).displayCategory(ItemDisplayCategory.SECURITY)).itemType(ItemType.KEY)).weight(0.3f)).icon("PadlockDigital")).digitalPadlock(true).metalValue(15.0f)).tooltip("Tooltip_Padlock")).worldStaticModel(ModelKey.COMBINATION_PADLOCK)).tags(ItemTag.LOCK)).add((ItemBuilder<?>)((KeyItemBuilder)((KeyItemBuilder)((KeyItemBuilder)((KeyItemBuilder)((KeyItemBuilder)((KeyItemBuilder)ItemBuilder.key(ItemKey.Key.KEY_PADLOCK).displayCategory(ItemDisplayCategory.SECURITY)).itemType(ItemType.KEY)).weight(0.05f)).icon("KeyPadlock1")).metalValue(5.0f)).worldStaticModel(ModelKey.KEY_PADLOCK)).tags(ItemTag.HAS_METAL, ItemTag.FITS_WALLET)).add((ItemBuilder<?>)((KeyItemBuilder)((KeyItemBuilder)((KeyItemBuilder)((KeyItemBuilder)((KeyItemBuilder)((KeyItemBuilder)((KeyItemBuilder)ItemBuilder.key(ItemKey.Key.PADLOCK).displayCategory(ItemDisplayCategory.SECURITY)).itemType(ItemType.KEY)).weight(0.3f)).icon("Padlock")).metalValue(15.0f)).padlock(true).tooltip("Tooltip_Padlock")).worldStaticModel(ModelKey.PADLOCK)).tags(ItemTag.LOCK)).add((ItemBuilder<?>)((KeyItemBuilder)((KeyItemBuilder)((KeyItemBuilder)((KeyItemBuilder)((KeyItemBuilder)((KeyItemBuilder)((KeyItemBuilder)((KeyItemBuilder)((KeyItemBuilder)ItemBuilder.key(ItemKey.Key.CAR_KEY).displayCategory(ItemDisplayCategory.SECURITY)).weight(0.05f)).itemType(ItemType.KEY)).icon("CarKey")).metalValue(7.0f)).worldStaticModel(ModelKey.CAR_KEYS)).tags(ItemTag.CAR_KEY, ItemTag.MORE_WHEN_NO_ZOMBIES, ItemTag.FITS_WALLET)).originX(0)).originY(0)).originZ(0)).write();
    }
}


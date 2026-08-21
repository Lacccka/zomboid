/*
 * Decompiled with CFR 0.152.
 */
package generation;

import generation.ItemScriptGenerator;
import generation.builders.AlarmClockItemBuilder;
import generation.builders.ItemBuilder;
import zombie.scripting.objects.ItemDisplayCategory;
import zombie.scripting.objects.ItemKey;
import zombie.scripting.objects.ItemTag;
import zombie.scripting.objects.ItemType;
import zombie.scripting.objects.ModelKey;
import zombie.scripting.objects.SoundKey;

public class ItemAlarmClockClothingScriptGenerator
extends ItemScriptGenerator {
    public static void main(String ... args2) {
        ItemAlarmClockClothingScriptGenerator.alarmclock();
    }

    public static void alarmclock() {
        ItemAlarmClockClothingScriptGenerator.scriptFile("items/alarmclock").add(((AlarmClockItemBuilder)((AlarmClockItemBuilder)((AlarmClockItemBuilder)((AlarmClockItemBuilder)((AlarmClockItemBuilder)((AlarmClockItemBuilder)((AlarmClockItemBuilder)ItemBuilder.alarmClock(ItemKey.AlarmClock.POCKETWATCH).displayCategory(ItemDisplayCategory.MEMENTO)).itemType(ItemType.ALARM_CLOCK)).weight(0.2f)).icon("Pocketwatch")).metalValue(1.0f)).worldStaticModel(ModelKey.POCKETWATCH)).tags(ItemTag.HAS_METAL, ItemTag.IGNORE_ZOMBIE_DENSITY, ItemTag.IS_MEMENTO, ItemTag.FITS_KEY_RING)).alarmSound(SoundKey.POCKET_WATCH_RINGING).soundRadius(7)).add((AlarmClockItemBuilder)((AlarmClockItemBuilder)((AlarmClockItemBuilder)((AlarmClockItemBuilder)((AlarmClockItemBuilder)((AlarmClockItemBuilder)ItemBuilder.alarmClock(ItemKey.AlarmClock.ALARM_CLOCK_2).displayCategory(ItemDisplayCategory.HOUSEHOLD)).itemType(ItemType.ALARM_CLOCK)).weight(1.0f)).icon("AlarmClock")).alarmSound(SoundKey.ALARM_CLOCK_RINGING_LOOP).metalValue(25.0f)).soundRadius(15).worldStaticModel(ModelKey.ALARM_CLOCK)).write();
    }
}


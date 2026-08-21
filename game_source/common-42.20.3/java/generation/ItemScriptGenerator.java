/*
 * Decompiled with CFR 0.152.
 */
package generation;

import generation.ItemAlarmClockClothingScriptGenerator;
import generation.ItemAlarmClockScriptGenerator;
import generation.ItemAnimalScriptGenerator;
import generation.ItemClothingScriptGenerator;
import generation.ItemContainerScriptGenerator;
import generation.ItemDrainableScriptGenerator;
import generation.ItemFoodScriptGenerator;
import generation.ItemKeyScriptGenerator;
import generation.ItemLiteratureScriptGenerator;
import generation.ItemMapScriptGenerator;
import generation.ItemMoveableScriptGenerator;
import generation.ItemNormalScriptGenerator;
import generation.ItemRadioScriptGenerator;
import generation.ItemWeaponPartScriptGenerator;
import generation.ItemWeaponScriptGenerator;
import generation.ScriptGenerator;
import generation.builders.ItemBuilder;
import generation.builders.validation.TranslationKeyValidator;
import java.util.Optional;

public class ItemScriptGenerator {
    protected static ScriptGenerator<ItemBuilder<?>> scriptFile(String name) {
        return ItemScriptGenerator.scriptFile(name, false);
    }

    protected static ScriptGenerator<ItemBuilder<?>> scriptFile(String name, boolean append) {
        return new ScriptGenerator<ItemBuilder<?>>(name, append){

            @Override
            public ScriptGenerator<ItemBuilder<?>> add(ItemBuilder<?> element) {
                if (element.get("hidden").filter(v -> v == false).isPresent()) {
                    TranslationKeyValidator.of("ItemName_%s", "%s.%s".formatted(this.module, element.getName()));
                }
                Optional<Integer> optDaysFresh = element.get("DaysFresh");
                int daysFresh = optDaysFresh.orElse(0);
                Optional optDaysTotallyRotten = element.get("DaysTotallyRotten");
                if (optDaysFresh.isPresent() || optDaysTotallyRotten.isPresent()) {
                    String name = "%s.%s".formatted(this.module, element.getName());
                    if (daysFresh != 0 && optDaysTotallyRotten.isPresent() && daysFresh >= (Integer)optDaysTotallyRotten.get()) {
                        System.out.println("%s Issue: daysFresh >= daysTotallyRotten.".formatted(name));
                    }
                    if (optDaysFresh.isEmpty() || optDaysTotallyRotten.isEmpty()) {
                        System.out.println("%s Issue: missing a %s value.".formatted(name, optDaysFresh.isEmpty() ? "daysFresh" : "daysTotallyRotten"));
                    }
                }
                return super.add(element);
            }
        };
    }

    public static void main(String ... args2) {
        ItemAlarmClockClothingScriptGenerator.main(new String[0]);
        ItemAlarmClockScriptGenerator.main(new String[0]);
        ItemAnimalScriptGenerator.main(new String[0]);
        ItemClothingScriptGenerator.main(new String[0]);
        ItemContainerScriptGenerator.main(new String[0]);
        ItemDrainableScriptGenerator.main(new String[0]);
        ItemFoodScriptGenerator.main(new String[0]);
        ItemKeyScriptGenerator.main(new String[0]);
        ItemLiteratureScriptGenerator.main(new String[0]);
        ItemMapScriptGenerator.main(new String[0]);
        ItemMoveableScriptGenerator.main(new String[0]);
        ItemNormalScriptGenerator.main(new String[0]);
        ItemRadioScriptGenerator.main(new String[0]);
        ItemWeaponPartScriptGenerator.main(new String[0]);
        ItemWeaponScriptGenerator.main(new String[0]);
    }
}


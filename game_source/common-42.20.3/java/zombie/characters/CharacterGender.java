/*
 * Decompiled with CFR 0.152.
 */
package zombie.characters;

import zombie.core.Translator;
import zombie.util.StringUtils;
import zombie.util.list.PZArrayUtil;

public enum CharacterGender {
    MALE("Male", "IGUI_char_Male"),
    FEMALE("Female", "IGUI_char_Female");

    private final String genderStr;
    private final String displayStringKey;

    private CharacterGender(String genderStr, String displayStringKey) {
        this.genderStr = genderStr;
        this.displayStringKey = displayStringKey;
    }

    public String getDisplayString() {
        return Translator.getText(this.getDisplayStringKey(), new Object[0]);
    }

    public String getDisplayStringKey() {
        return this.displayStringKey;
    }

    public String getGenderStr() {
        return this.genderStr;
    }

    public String toString() {
        return this.getGenderStr();
    }

    public static CharacterGender fromString(String genderStr) {
        return PZArrayUtil.findOrDefault(CharacterGender.values(), genderStr, (val, str) -> StringUtils.equalsIgnoreCase(val.genderStr, str), null);
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package zombie.characters.BodyDamage;

import zombie.UsedFromLua;
import zombie.characters.CharacterGender;
import zombie.core.Translator;
import zombie.core.skinnedmodel.population.OutfitRNG;
import zombie.debug.DebugType;
import zombie.util.StringUtils;
import zombie.util.list.PZArrayUtil;

@UsedFromLua
public enum BodyPartType {
    Hand_L("Hand_L", "IGUI_health_Left_Hand", "Base.Bandage_LeftHand", "Base.Wound_LHand_Bite_", "Base.Wound_LHand_Scratch_", "Base.Wound_LHand_Laceration_", 0.5f, 0.1f, 0.2f, 0.01f, 0.8f, 0.35f, 1.0f, 0.05f),
    Hand_R("Hand_R", "IGUI_health_Right_Hand", "Base.Bandage_RightHand", "Base.Wound_RHand_Bite_", "Base.Wound_RHand_Scratch_", "Base.Wound_RHand_Laceration_", 0.5f, 0.1f, 0.2f, 0.01f, 0.8f, 0.35f, 1.0f, 0.05f),
    ForeArm_L("ForeArm_L", "IGUI_health_Left_Forearm", "Base.Bandage_LeftLowerArm", "Base.Wound_LForearm_Bite_", "Base.Wound_LForearm_Scratch_", "Base.Wound_LForearm_Laceration_", 0.6f, 0.2f, 0.3f, 0.035f, 0.6f, 0.3f, 0.6f, 0.1f),
    ForeArm_R("ForeArm_R", "IGUI_health_Right_Forearm", "Base.Bandage_RightLowerArm", "Base.Wound_RForearm_Bite_", "Base.Wound_RForearm_Scratch_", "Base.Wound_RForearm_Laceration_", 0.6f, 0.2f, 0.3f, 0.035f, 0.6f, 0.3f, 0.6f, 0.1f),
    UpperArm_L("UpperArm_L", "IGUI_health_Left_Upper_Arm", "Base.Bandage_LeftUpperArm", "Base.Wound_LUArm_Bite_", "Base.Wound_LUArm_Scratch_", "Base.Wound_LUArm_Laceration_", 0.6f, 0.3f, 0.4f, 0.045f, 0.3f, 0.25f, 0.4f, 0.1f),
    UpperArm_R("UpperArm_R", "IGUI_health_Right_Upper_Arm", "Base.Bandage_RightUpperArm", "Base.Wound_RUArm_Bite_", "Base.Wound_RUArm_Scratch_", "Base.Wound_RUArm_Laceration_", 0.6f, 0.3f, 0.4f, 0.045f, 0.3f, 0.25f, 0.4f, 0.1f),
    Torso_Upper("Torso_Upper", "IGUI_health_Upper_Torso", "Base.Bandage_Chest", "Base.Wound_Chest_Bite_", "Base.Wound_Chest_Scratch_", "Base.Wound_Chest_Laceration_", 0.7f, 0.35f, 0.5f, 0.18f, 0.0f, 0.2f, 0.2f, 0.05f),
    Torso_Lower("Torso_Lower", "IGUI_health_Lower_Torso", "Base.Bandage_Abdomen", "Base.Wound_Abdomen_Bite_", "Base.Wound_Abdomen_Scratch_", "Base.Wound_Abdomen_Laceration_", 0.78f, 0.4f, 0.9f, 0.12f, 0.05f, 0.25f, 0.1f, 0.05f),
    Head("Head", "IGUI_health_Head", "Base.Bandage_Head", "Base.Wound_Neck_Bite_", "Base.Wound_Neck_Scratch_", "Base.Wound_Neck_Laceration_", 0.8f, 0.6f, 1.0f, 0.08f, 0.05f, 0.05f, 0.4f, 0.25f),
    Neck("Neck", "IGUI_health_Neck", "Base.Bandage_Neck", "Base.Wound_Neck_Bite_", "Base.Wound_Neck_Scratch_", "Base.Wound_Neck_Laceration_", 0.8f, 0.7f, 1.5f, 0.02f, 0.02f, 0.1f, 0.05f, 0.05f),
    Groin("Groin", "IGUI_health_Groin", "Base.Bandage_Groin", "Base.Wound_Groin_Bite_", "Base.Wound_Groin_Scratch_", "Base.Wound_Groin_Laceration_", 0.7f, 0.4f, 0.5f, 0.06f, 0.3f, 0.3f, 0.05f, 0.15f),
    UpperLeg_L("UpperLeg_L", "IGUI_health_Left_Thigh", "Base.Bandage_LeftUpperLeg", null, null, null, 0.7f, 0.3f, 0.4f, 0.09f, 0.45f, 0.55f, 0.1f, 0.4f),
    UpperLeg_R("UpperLeg_R", "IGUI_health_Right_Thigh", "Base.Bandage_RightUpperLeg", null, null, null, 0.7f, 0.3f, 0.4f, 0.09f, 0.45f, 0.55f, 0.1f, 0.4f),
    LowerLeg_L("LowerLeg_L", "IGUI_health_Left_Shin", "Base.Bandage_LeftLowerLeg", null, null, null, 0.6f, 0.2f, 0.3f, 0.07f, 0.75f, 0.75f, 0.1f, 0.6f),
    LowerLeg_R("LowerLeg_R", "IGUI_health_Right_Shin", "Base.Bandage_RightLowerLeg", null, null, null, 0.6f, 0.2f, 0.3f, 0.07f, 0.75f, 0.75f, 0.1f, 0.6f),
    Foot_L("Foot_L", "IGUI_health_Left_Foot", "Base.Bandage_LeftFoot", null, null, null, 0.5f, 0.2f, 0.2f, 0.02f, 1.0f, 1.0f, 0.1f, 1.0f),
    Foot_R("Foot_R", "IGUI_health_Right_Foot", "Base.Bandage_RightFoot", null, null, null, 0.5f, 0.2f, 0.2f, 0.02f, 1.0f, 1.0f, 0.1f, 1.0f),
    MAX("Unknown Body Part", "IGUI_health_Unknown_Body_Part", null, null, null, null, 1.0f, 1.0f, 1.0f, 0.001f, 0.0f, 1.0f, 0.0f, 0.0f);

    private static final BodyPartType[] values;
    private final String name;
    private final float painModifier;
    private final String displayNameKey;
    private final float damageModifier;
    private final float bleedingTimeModifier;
    private final float skinSurface;
    private final float distToCore;
    private final float umbrellaMod;
    private final float maxActionPenalty;
    private final float maxMovementPenalty;
    private final String bandageModel;
    private final String biteWoundModel;
    private final String scratchWoundModel;
    private final String cutWoundModel;

    private BodyPartType(String name, String displayNameKey, String bandageModel, String biteWoundModel, String scratchWoundModel, String cutWoundModel, float painModifier, float damageModifier, float bleedingTimeModifier, float skinSurface, float distToCore, float umbrellaMod, float maxActionPenalty, float maxMovementPenalty) {
        this.name = name;
        this.painModifier = painModifier;
        this.displayNameKey = displayNameKey;
        this.damageModifier = damageModifier;
        this.bleedingTimeModifier = bleedingTimeModifier;
        this.skinSurface = skinSurface;
        this.distToCore = distToCore;
        this.umbrellaMod = umbrellaMod;
        this.maxActionPenalty = maxActionPenalty;
        this.maxMovementPenalty = maxMovementPenalty;
        this.bandageModel = bandageModel;
        this.biteWoundModel = biteWoundModel;
        this.scratchWoundModel = scratchWoundModel;
        this.cutWoundModel = cutWoundModel;
    }

    public static BodyPartType FromIndex(int index) {
        return index >= 0 && index < values.length ? values[index] : MAX;
    }

    public int index() {
        return this.ordinal();
    }

    public static BodyPartType FromString(String str) {
        return PZArrayUtil.findOrDefault(values, str, (bodyPart, _str) -> StringUtils.equalsIgnoreCase(bodyPart.name, _str), MAX);
    }

    public static float getPainModifyer(int index) {
        return BodyPartType.FromIndex((int)index).painModifier;
    }

    public static String getDisplayName(BodyPartType bpt) {
        return Translator.getText(bpt != null ? bpt.displayNameKey : BodyPartType.MAX.displayNameKey, new Object[0]);
    }

    public static int ToIndex(BodyPartType bpt) {
        return bpt != null ? bpt.ordinal() : 0;
    }

    public static String ToString(BodyPartType bpt) {
        return bpt != null ? bpt.name : BodyPartType.MAX.name;
    }

    public static float getDamageModifyer(int index) {
        return BodyPartType.FromIndex((int)index).damageModifier;
    }

    public static float getBleedingTimeModifyer(int index) {
        return BodyPartType.FromIndex((int)index).bleedingTimeModifier;
    }

    public static float GetSkinSurface(BodyPartType bodyPartType) {
        if (bodyPartType == null || bodyPartType == MAX) {
            DebugType.General.warn("Warning: couldn't get skinSurface for body part '%s'.", new Object[]{bodyPartType});
            return BodyPartType.MAX.skinSurface;
        }
        return bodyPartType.skinSurface;
    }

    public static float GetDistToCore(BodyPartType bodyPartType) {
        if (bodyPartType == null || bodyPartType == MAX) {
            DebugType.General.warn("Warning: couldn't get distToCore for body part '%s'.", new Object[]{bodyPartType});
            return BodyPartType.MAX.distToCore;
        }
        return bodyPartType.distToCore;
    }

    public static float GetUmbrellaMod(BodyPartType bodyPartType) {
        if (bodyPartType == null || bodyPartType == MAX) {
            DebugType.General.warn("Warning: couldn't get umbrellaMod for body part '%s'.", new Object[]{bodyPartType});
            return BodyPartType.MAX.umbrellaMod;
        }
        return bodyPartType.umbrellaMod;
    }

    public static float GetMaxActionPenalty(BodyPartType bodyPartType) {
        if (bodyPartType == null || bodyPartType == MAX) {
            DebugType.General.warn("Warning: couldn't get maxActionPenalty for body part '%s'.", new Object[]{bodyPartType});
            return BodyPartType.MAX.maxActionPenalty;
        }
        return bodyPartType.maxActionPenalty;
    }

    public static float GetMaxMovementPenalty(BodyPartType bodyPartType) {
        if (bodyPartType == null || bodyPartType == MAX) {
            DebugType.General.warn("Warning: couldn't get maxMovementPenalty for body part '%s'.", new Object[]{bodyPartType});
            return BodyPartType.MAX.maxMovementPenalty;
        }
        return bodyPartType.maxMovementPenalty;
    }

    public String getBandageModel() {
        return this.bandageModel;
    }

    public String getBiteWoundModel(CharacterGender gender) {
        return this.biteWoundModel != null ? this.biteWoundModel + String.valueOf((Object)gender) : null;
    }

    public String getScratchWoundModel(CharacterGender gender) {
        return this.scratchWoundModel != null ? this.scratchWoundModel + String.valueOf((Object)gender) : null;
    }

    public String getCutWoundModel(CharacterGender gender) {
        return this.cutWoundModel != null ? this.cutWoundModel + String.valueOf((Object)gender) : null;
    }

    public static BodyPartType getRandom() {
        return BodyPartType.FromIndex(OutfitRNG.Next(0, MAX.index()));
    }

    static {
        values = BodyPartType.values();
    }
}


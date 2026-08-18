-- MFS B42 patch: inspection-model aliases for corrected held firearm meshes.
--
-- WeaponSprite must reference the pre-mirrored mesh expected by the character
-- renderer. The neutral inspection scene must instead show the original
-- Blender-space model. Add future held-to-inspection aliases here rather than
-- adding firearm-specific conditions to risky_inspect_core.lua.
AWCWF_InspectionModelMap = AWCWF_InspectionModelMap or {}

AWCWF_InspectionModelMap.M240_cat_Held = "M240_cat"
AWCWF_InspectionModelMap.URG_S_cat_Held = "URG_S_cat"
AWCWF_InspectionModelMap.URG_S_cat_Drum_Held = "URG_S_cat_Drum"
AWCWF_InspectionModelMap.NoveskeN4_cat_Held = "NoveskeN4_cat"
AWCWF_InspectionModelMap.NoveskeN4_cat_Drum_Held = "NoveskeN4_cat_Drum"
AWCWF_InspectionModelMap.HK416_cat_Held = "HK416_cat"
AWCWF_InspectionModelMap.HK416_cat_Drum_Held = "HK416_cat_Drum"
AWCWF_InspectionModelMap.M4Mk18_cat_Held = "M4Mk18_cat"
AWCWF_InspectionModelMap.M4Mk18_cat_Drum_Held = "M4Mk18_cat_Drum"
AWCWF_InspectionModelMap.M4A1S_cat_Held = "M4A1S_cat"
AWCWF_InspectionModelMap.M4A1S_cat_Drum_Held = "M4A1S_cat_Drum"


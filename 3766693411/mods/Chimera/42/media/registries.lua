
local bodylocations = {
    "ChimeraBodyLocationDefault",
    "TacticalLeoTard",
    "MollePlacard",
    "HelmetGearSet",
    "HelmetArmorPlates",
    "HolsterDeltaLeft",
    "HolsterDeltaRight",
    "HelmetArmored",
    "HelmetBase",
    "Ghillie",
    "MolleBracket",
    "MollePouch",
    "MollePouchTwo",
    "MollePouchThree",
    "HolsterVest",
    "Shemagh",
    "CamoPackCover",
    "Shawl",
    "HelmetScrimCover",
    "HelmetArmoredCover",
    "HelmetArmoredCoverIFF",
    "HelmetArmoredCoverIFFT",
    "HelmetGearSetCounterWeight",
    "HelmetGearHeadSet",
    "HelmetGearSetBatteryBox",
    "BlackArrowPouch",
    "BlackArrowPouchTwo",
    "MolleHarness",
    "MollePouchFour",
    "MollePouchFive",
    "CleavageOperator",
    "MolleVisualStorage",
    "MolleTabletID",
    "MolleMisc",
    "HelmetNightVisionGear",
    "ArmorKneePads",
    "HelmetPatch",
    "MolleStorageKit",
    "LegStorageLeft",
    "LegStorageRight",
    "MolleMagazineSet",
    "HeavyRiotGearEyewear",
    "HeavyRiotGearFrontPlate",
    "HeavyRiotGearHelmetPatchA",
    "HeavyRiotGearHelmetPatchB",
    "MollePlacardRadio",
    "HeavyRiotGearVest",

}



ChimeraRegistries = {}
ChimeraRegistries.BodyLocations = {}

for i = 1, #bodylocations do
    ChimeraRegistries.BodyLocations[i] = ItemBodyLocation.register("CHIMERA:" .. bodylocations[i])
end





--[[
-- threw errors. Cant be fucked to find why.
ChimeraRegistries = {}
ChimeraRegistries.BodyLocations = {}

ChimeraRegistries.BodyLocations.HolsterDeltaLeft = ItemBodyLocation.register("CHIMERA:HolsterDeltaLeft")
ChimeraRegistries.BodyLocations.HolsterDeltaRight = ItemBodyLocation.register("CHIMERA:HolsterDeltaRight")






ChimeraRegistries.BodyLocations.TacticalLeoTard = ItemBodyLocation.register("CHIMERA:TacticalLeoTard")
ChimeraRegistries.BodyLocations.CleavageOperator = ItemBodyLocation.register("CHIMERA:CleavageOperator")
ChimeraRegistries.BodyLocations.MollePlacard = ItemBodyLocation.register("CHIMERA:MollePlacard")
ChimeraRegistries.BodyLocations.MolleMagazineSet = ItemBodyLocation.register("CHIMERA:MolleMagazineSet")
ChimeraRegistries.BodyLocations.HelmetArmorPlates = ItemBodyLocation.register("CHIMERA:HelmetArmorPlates")
ChimeraRegistries.BodyLocations.HelmetGearSet = ItemBodyLocation.register("CHIMERA:HelmetGearSet")
ChimeraRegistries.BodyLocations.HelmetArmored = ItemBodyLocation.register("CHIMERA:HelmetArmored")
ChimeraRegistries.BodyLocations.HelmetBase = ItemBodyLocation.register("CHIMERA:HelmetBase")
ChimeraRegistries.BodyLocations.MolleBracket = ItemBodyLocation.register("CHIMERA:MolleBracket")
ChimeraRegistries.BodyLocations.MollePouch = ItemBodyLocation.register("CHIMERA:MollePouch")
ChimeraRegistries.BodyLocations.MollePouchTwo = ItemBodyLocation.register("CHIMERA:MollePouchTwo")
ChimeraRegistries.BodyLocations.MollePouchThree = ItemBodyLocation.register("CHIMERA:MollePouchThree")
ChimeraRegistries.BodyLocations.MollePouchFour = ItemBodyLocation.register("CHIMERA:MollePouchFour")
ChimeraRegistries.BodyLocations.HolsterVest = ItemBodyLocation.register("CHIMERA:HolsterVest")
ChimeraRegistries.BodyLocations.Shemagh = ItemBodyLocation.register("CHIMERA:Shemagh")
ChimeraRegistries.BodyLocations.Shawl = ItemBodyLocation.register("CHIMERA:Shawl")
ChimeraRegistries.BodyLocations.HelmetArmoredCover = ItemBodyLocation.register("CHIMERA:HelmetArmoredCover")
ChimeraRegistries.BodyLocations.HelmetArmoredCoverIFF = ItemBodyLocation.register("CHIMERA:HelmetArmoredCoverIFF")
ChimeraRegistries.BodyLocations.HelmetArmoredCoverIFFT = ItemBodyLocation.register("CHIMERA:HelmetArmoredCoverIFFT")
ChimeraRegistries.BodyLocations.HelmetGearSetCounterWeight = ItemBodyLocation.register("CHIMERA:HelmetGearSetCounterWeight")
ChimeraRegistries.BodyLocations.HelmetGearHeadSet = ItemBodyLocation.register("CHIMERA:HelmetGearHeadSet")
ChimeraRegistries.BodyLocations.HelmetGearSetBatteryBox = ItemBodyLocation.register("CHIMERA:HelmetGearSetBatteryBox")
ChimeraRegistries.BodyLocations.BlackArrowPouchTwo = ItemBodyLocation.register("CHIMERA:BlackArrowPouchTwo")
ChimeraRegistries.BodyLocations.BlackArrowPouch = ItemBodyLocation.register("CHIMERA:BlackArrowPouch")
ChimeraRegistries.BodyLocations.MolleHarness = ItemBodyLocation.register("CHIMERA:MolleHarness")
ChimeraRegistries.BodyLocations.MollePouchFive = ItemBodyLocation.register("CHIMERA:MollePouchFive")
ChimeraRegistries.BodyLocations.MolleVisualStorage = ItemBodyLocation.register("CHIMERA:MolleVisualStorage")
ChimeraRegistries.BodyLocations.MolleTabletID = ItemBodyLocation.register("CHIMERA:MolleTabletID")
ChimeraRegistries.BodyLocations.MolleMisc = ItemBodyLocation.register("CHIMERA:MolleMisc")
ChimeraRegistries.BodyLocations.HelmetNightVisionGear = ItemBodyLocation.register("CHIMERA:HelmetNightVisionGear")
ChimeraRegistries.BodyLocations.ArmorKneePads = ItemBodyLocation.register("CHIMERA:ArmorKneePads")
ChimeraRegistries.BodyLocations.HelmetPatch = ItemBodyLocation.register("CHIMERA:HelmetPatch")
ChimeraRegistries.BodyLocations.MolleStorageKit = ItemBodyLocation.register("CHIMERA:MolleStorageKit")
ChimeraRegistries.BodyLocations.LegStorageLeft = ItemBodyLocation.register("CHIMERA:LegStorageLeft")
ChimeraRegistries.BodyLocations.LegStorageRight = ItemBodyLocation.register("CHIMERA:LegStorageRight")
--]]
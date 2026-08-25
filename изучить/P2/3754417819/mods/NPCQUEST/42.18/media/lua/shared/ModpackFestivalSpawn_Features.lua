-- Bare baseline feature flags.
-- Keep quest UI/state, sister customization data, and isolated start Party NPCs.

ModpackFestivalFeatures = ModpackFestivalFeatures or {}

ModpackFestivalFeatures.MALL_LIGHTS_ENABLED = false
ModpackFestivalFeatures.SISTER_QUEST_ENABLED = true
ModpackFestivalFeatures.SISTER_SPAWN_ENABLED = true
ModpackFestivalFeatures.MALL_SPAWN_ENABLED = false  -- disabled until ready
ModpackFestivalFeatures.MALL_STORY_SPAWN_ENABLED = false
ModpackFestivalFeatures.START_FESTIVAL_BANDITS_ENABLED = true
ModpackFestivalFeatures.CIVILIAN_CONVERSION_ENABLED = true
ModpackFestivalFeatures.STARTER_VEHICLE_ENABLED = true

local SISTER_QUEST_IDS = {
    find_sister = true,
    meet_sister = true,
    get_home = true,
}

function ModpackFestivalFeatures.isSisterQuest(questId)
    return questId ~= nil and SISTER_QUEST_IDS[questId] == true
end

function ModpackFestivalFeatures.isSisterQuestEnabled()
    return ModpackFestivalFeatures.SISTER_QUEST_ENABLED == true
end

function ModpackFestivalFeatures.isSisterSpawnEnabled()
    return ModpackFestivalFeatures.SISTER_SPAWN_ENABLED == true
end

function ModpackFestivalFeatures.isMallLightsEnabled()
    return false
end

function ModpackFestivalFeatures.isMallStorySpawnEnabled()
    return false
end

function ModpackFestivalFeatures.isMallSpawnEnabled()
    return ModpackFestivalFeatures.MALL_SPAWN_ENABLED == true
end

function ModpackFestivalFeatures.isStartFestivalBanditsEnabled()
    return ModpackFestivalFeatures.START_FESTIVAL_BANDITS_ENABLED == true
end

function ModpackFestivalFeatures.isCivilianConversionEnabled()
    return ModpackFestivalFeatures.CIVILIAN_CONVERSION_ENABLED == true
end

function ModpackFestivalFeatures.isStarterVehicleEnabled()
    return ModpackFestivalFeatures.STARTER_VEHICLE_ENABLED == true
end

if not ModpackFestivalFeatures._announced then
    ModpackFestivalFeatures._announced = true
    print("[ModpackFestivalSpawn] feature flags: quests ON, sister spawn ON, mall spawn OFF, start Party NPCs ON, civilian conversion ON, starter vehicle ON")
end

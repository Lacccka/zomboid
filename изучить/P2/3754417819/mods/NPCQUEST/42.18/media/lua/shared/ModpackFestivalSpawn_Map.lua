-- Louisville world-map bounds (vanilla ISMapDefinitions 3x3 grid).

ModpackFestivalMap = ModpackFestivalMap or {}

ModpackFestivalMap.MOD_ID = "ModpackFestivalSpawn"

-- Full Louisville metro (all nine map sheets) — reference only.
ModpackFestivalMap.LOUISVILLE_FULL_X1 = 11700
ModpackFestivalMap.LOUISVILLE_FULL_Y1 = 750
ModpackFestivalMap.LOUISVILLE_FULL_X2 = 14699
ModpackFestivalMap.LOUISVILLE_FULL_Y2 = 3899

-- Top-right Louisville cell only (LootMaps.Init.LouisvilleMap3 — col 2, row 0).
-- Covers festival spawn, parking, and mall quest area.
ModpackFestivalMap.LOUISVILLE_KNOWN_X1 = 13500
ModpackFestivalMap.LOUISVILLE_KNOWN_Y1 = 750
ModpackFestivalMap.LOUISVILLE_KNOWN_X2 = 14699
ModpackFestivalMap.LOUISVILLE_KNOWN_Y2 = 2099

-- Per-quest world-map markers (unique symbol + tint; only the active navigable quest is shown).
ModpackFestivalMap.QUEST_MARKERS = {
    get_to_car = {
        symbolId = "SteeringWheel",
        r = 0.15,
        g = 0.45,
        b = 0.95,
        rotateWithVehicle = true,
    },
    find_sister = {
        symbolId = "Skyscraper",
        r = 0.55,
        g = 0.85,
        b = 1.0,
    },
    meet_sister = {
        symbolId = "Heart",
        r = 0.95,
        g = 0.25,
        b = 0.45,
    },
    get_home = {
        symbolId = "House",
        r = 0.95,
        g = 0.72,
        b = 0.2,
    },
}

function ModpackFestivalMap.getQuestMarkerConfig(questId)
    if not questId then return nil end
    return ModpackFestivalMap.QUEST_MARKERS[questId]
end

function ModpackFestivalMap.getActiveQuestMarker()
    if not ModpackFestivalQuests or not ModpackFestivalQuests.getActiveQuestId then
        return nil, nil
    end
    local questId = ModpackFestivalQuests.getActiveQuestId()
    if not questId then return nil, nil end
    local quest = ModpackFestivalQuests.getDefinition(questId)
    if not quest or not ModpackFestivalQuests.questHasNavigationTarget(quest) then
        return nil, nil
    end
    local cfg = ModpackFestivalMap.getQuestMarkerConfig(questId)
    if not cfg then return nil, nil end
    return cfg, questId
end

function ModpackFestivalMap.getPlayerMapData(player)
    if not player then return nil end
    local md = player:getModData()
    md.ModpackFestivalSpawn = md.ModpackFestivalSpawn or {}
    return md.ModpackFestivalSpawn
end

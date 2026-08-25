-- One-time zombie conversion for newly seen zombies.
-- Start-area zombies become Party NPCs; first mall-approach zombies near
-- Alyssa get a one-time 60% removal roll before remaining zombies get civilian rolls.
-- Global conversion rate drops from 10% to 1% once the find_sister quest is complete.

if isClient() and not isServer() then
    return
end

ModpackFestivalCivilianConvert = ModpackFestivalCivilianConvert or {}

local MOD_ID = "ModpackFestivalSpawn"
local LOG_PREFIX = "[" .. MOD_ID .. "][CivilianConvert] "

local PARTY_CLAN_UUID = "42364b66-ab03-4c38-b374-5575a0c24868"
local CIVILIAN_CLAN_UUID = "c167d1e0-c077-4ee5-b353-88b374de193d"

-- After 12 hours the spawn pool opens up to all clans without heavy weapons.
-- Excluded: 01_Rednecks, 00_Hunters, 12_Militia, 15_Legion, ArmyDesert, Officers.
local LATE_GAME_CLANS = {
    "f06e063f-551c-4fd5-aa19-bc15185c2371", -- 02_Sport
    "8db5a57f-b0a9-4b04-9228-beeadd2db2fa", -- 03_Butchers
    "1eb2f74a-8d09-4346-8ba4-3a02665647e5", -- 04_Criminals
    "94d0c40a-539f-480a-883e-f3fdf15c703f", -- 05_Spike
    "f8aa0e8c-92ee-4dce-99e8-e4cc3a5a8fbe", -- 06_Robbers
    "2681ecf5-d0d9-481d-b769-7c4fb626eb81", -- 07_Heretics
    "6e319aac-4480-4367-aa9a-5d4bf2ced9d1", -- 08_Hikers
    "48b1d4b0-ca4e-4b02-ab56-e1fff39afe48", -- 09_Leather
    "cf115969-1f01-425e-9f42-bc4aec99555a", -- 10_Mafia
    "b7d3a430-e966-48e8-97fa-9078c8d848a4", -- 11_Cannibals
    "bf5985eb-7cb0-44ba-8392-f656ffe421f4", -- 12_Medieval
    "fe1a8e07-7c73-466d-9ade-2e3e565dcb21", -- 13_Trash
    "a5def10b-bab9-46b1-8e8c-d5152c86457e", -- 14_Hermits
    "c5c7f769-e5e8-44bb-ada6-cdf00ee2c234", -- Baseball
    "8365593d-f3b2-4e93-a96f-29315f83c51f", -- Prison
    "0dfc13d3-4ce6-4af8-aac6-326eb7514c36", -- Karate
    "c167d1e0-c077-4ee5-b353-88b374de193d", -- Civilians
    "e42fc351-dd10-4a0c-a154-b383cef3b987", -- Wedding
    "bd53300c-f715-4cf7-a91f-1836a2282944", -- Runners
    "76e0eb48-ee72-45ac-9b1b-56a66f597235", -- Gardeners
    "e216b4ea-e57f-4b15-8cd8-140b82e7b5ea", -- Postal
    "e195497c-9a14-4c1f-b15a-b8227d15a682", -- Janitors
}
local FESTIVAL_SPAWN_X = 13737
local FESTIVAL_SPAWN_Y = 1962
local START_PARTY_RADIUS = 30
local CONVERSION_CHANCE_EARLY = 10
local CONVERSION_CHANCE_EARLY_MALL = 30
local CONVERSION_CHANCE_LATE  = 1
local SISTER_SPAWN_X = 13595
local SISTER_SPAWN_Y = 1292
local SISTER_MALL_THIN_RADIUS = 500
local SISTER_MALL_THIN_REMOVE_CHANCE = 60
local PROCESSED_FLAG = "modpackFestivalCivilianRollDone"
local MALL_THIN_FLAG = "modpackFestivalMallSpawnThinRollDone"
local FEATURE_VERSION = 4
local MALL_THIN_VERSION = 1

local banditsLoaded = false
local convertedCount = 0
local startPartyCount = 0
local mallThinRemovedCount = 0
-- Save-level flag: once the mall thin has run once it must not run again on reload.
-- Zombie ModData (MALL_THIN_FLAG) cannot protect against this because PZ regenerates
-- zombie instances when cells reload, giving them fresh ModData with no flag set.
local mallThinSaveComplete = false

-- Conversion is blocked for the first N ticks after a session loads.
-- On every load, nearby zombies fire OnZombieUpdate in a burst. Without this guard
-- they all get the 33% roll and spawn a wave of NPCs. The delay lets those zombies
-- get marked PROCESSED (skipping conversion) before the roll is enabled.
local CONVERSION_STARTUP_DELAY_TICKS = 300   -- ~15 seconds at ~20 ticks/sec
local conversionSessionTick = 0
local conversionReady = false

local function getMallThinState()
    local md = ModData.getOrCreate(MOD_ID)
    md.mallThin = md.mallThin or {}
    return md.mallThin
end

local function isMallThinComplete()
    if mallThinSaveComplete then return true end
    local st = getMallThinState()
    if st.done then
        mallThinSaveComplete = true
        return true
    end
    return false
end

local function markMallThinComplete()
    mallThinSaveComplete = true
    getMallThinState().done = true
end

local function banditsReady()
    if not BanditServer or not BanditServer.Spawner or not BanditServer.Spawner.Clan then
        return false
    end
    if not banditsLoaded and BanditCustom and BanditCustom.Load then
        pcall(BanditCustom.Load)
        banditsLoaded = true
    end
    return true
end

local function isInSisterMallThinRadius(zombie)
    local dx = zombie:getX() - SISTER_SPAWN_X
    local dy = zombie:getY() - SISTER_SPAWN_Y
    return (dx * dx + dy * dy) <= (SISTER_MALL_THIN_RADIUS * SISTER_MALL_THIN_RADIUS)
end

local function isFirstMallApproachActive()
    if not ModpackFestivalQuests then
        return false
    end
    if ModpackFestivalQuests.isCompleted and ModpackFestivalQuests.isCompleted("meet_sister") then
        return false
    end
    local activeId = ModpackFestivalQuests.getActiveQuestId and ModpackFestivalQuests.getActiveQuestId()
    if activeId == "find_sister" or activeId == "meet_sister" then
        return true
    end
    return ModpackFestivalQuests.isCompleted
        and ModpackFestivalQuests.isCompleted("find_sister")
        and not ModpackFestivalQuests.isCompleted("meet_sister")
end

local function isAlyssaSister(zombie)
    if ModpackFestivalSister and ModpackFestivalSister.isSisterBandit
        and ModpackFestivalSister.isSisterBandit(zombie) then
        return true
    end
    local md = zombie and zombie.getModData and zombie:getModData() or nil
    if not md or not ModpackFestivalSister then
        return false
    end
    return md.modpackFestivalSister == true
        or md.modpackFestivalSisterProtected == true
        or md.modpackFestivalSisterClanId == ModpackFestivalSister.CLAN_ID
        or md.modpackFestivalSisterBanditId == ModpackFestivalSister.BANDIT_ID
end

local function hasFoundSister()
    return ModpackFestivalQuests and ModpackFestivalQuests.isCompleted
        and ModpackFestivalQuests.isCompleted("find_sister")
end

local function getConversionChance(zombie)
    if hasFoundSister() then
        return CONVERSION_CHANCE_LATE
    end
    if zombie and isInSisterMallThinRadius(zombie) then
        return CONVERSION_CHANCE_EARLY_MALL
    end
    return CONVERSION_CHANCE_EARLY
end

local function shouldConvert(zombie)
    if not conversionReady then return false end
    if ZombRand then
        return ZombRand(100) < getConversionChance(zombie)
    end
    return false
end

local function getSpawnClan()
    if hasFoundSister() then
        return LATE_GAME_CLANS[ZombRand(#LATE_GAME_CLANS) + 1]
    end
    return CIVILIAN_CLAN_UUID
end

local function markProcessed(zombie)
    local md = zombie:getModData()
    md[PROCESSED_FLAG] = FEATURE_VERSION
end

local function shouldThinMallSpawnZombie(zombie)
    if not zombie or not zombie.getModData then
        return false
    end
    -- Save-level guard: once the thin has been applied once this save, never again.
    -- Zombie-level MALL_THIN_FLAG alone is insufficient because PZ regenerates zombie
    -- instances on cell reload, giving them fresh ModData with no flag set.
    if isMallThinComplete() then
        return false
    end
    if isAlyssaSister(zombie)
        or not isFirstMallApproachActive()
        or not isInSisterMallThinRadius(zombie) then
        return false
    end
    local md = zombie:getModData()
    if md[MALL_THIN_FLAG] == MALL_THIN_VERSION then
        return false
    end
    md[MALL_THIN_FLAG] = MALL_THIN_VERSION
    if ZombRand then
        return ZombRand(100) < SISTER_MALL_THIN_REMOVE_CHANCE
    end
    return false
end

local function spawnBanditAt(player, zombie, cid)
    local args = {
        cid = cid,
        program = "Defend",
        size = 1,
        hostileP = false,
        spawnPoints = {
            {
                x = zombie:getX(),
                y = zombie:getY(),
                z = zombie:getZ() or 0,
            },
        },
    }
    BanditServer.Spawner.Clan(player, args)
end

local function isInStartPartyRadius(zombie)
    local dx = zombie:getX() - FESTIVAL_SPAWN_X
    local dy = zombie:getY() - FESTIVAL_SPAWN_Y
    return (dx * dx + dy * dy) <= (START_PARTY_RADIUS * START_PARTY_RADIUS)
end

local function removeZombie(zombie)
    pcall(function()
        if BanditBrain and BanditBrain.Remove and zombie.getVariableBoolean
            and zombie:getVariableBoolean("Bandit") then
            BanditBrain.Remove(zombie)
        end
    end)
    pcall(function()
        if zombie.removeFromWorld then
            zombie:removeFromWorld()
        end
    end)
    pcall(function()
        if zombie.removeFromSquare then
            zombie:removeFromSquare()
        end
    end)
end

local function canProcessZombie(zombie)
    if not zombie or not zombie.getModData then
        return false
    end
    if zombie.isDead and zombie:isDead() then
        return false
    end
    if zombie.getVariableBoolean and zombie:getVariableBoolean("Bandit") then
        return false
    end
    local md = zombie:getModData()
    if md[PROCESSED_FLAG] == FEATURE_VERSION then
        return false
    end
    return true
end

local function onZombieUpdate(zombie)
    if ModpackFestivalFeatures and ModpackFestivalFeatures.isCivilianConversionEnabled
        and not ModpackFestivalFeatures.isCivilianConversionEnabled() then
        return
    end
    if not zombie or not zombie.getModData then
        return
    end
    if zombie.isDead and zombie:isDead() then
        return
    end
    local player = getSpecificPlayer and getSpecificPlayer(0)
    if not player then
        return
    end

    if shouldThinMallSpawnZombie(zombie) then
        markProcessed(zombie)
        removeZombie(zombie)
        mallThinRemovedCount = mallThinRemovedCount + 1
        if mallThinRemovedCount <= 10 or mallThinRemovedCount % 25 == 0 then
            print(LOG_PREFIX .. "first mall approach removed zombie/NPC near Alyssa, total="
                .. tostring(mallThinRemovedCount))
        end
        return
    end

    if zombie.getVariableBoolean and zombie:getVariableBoolean("Bandit") then
        return
    end

    if not canProcessZombie(zombie) then
        return
    end

    if not banditsReady() then
        return
    end

    markProcessed(zombie)

    if ModpackFestivalFeatures and ModpackFestivalFeatures.isStartFestivalBanditsEnabled
        and ModpackFestivalFeatures.isStartFestivalBanditsEnabled()
        and isInStartPartyRadius(zombie) then
        local ok, err = pcall(function()
            spawnBanditAt(player, zombie, PARTY_CLAN_UUID)
        end)
        removeZombie(zombie)
        if ok then
            startPartyCount = startPartyCount + 1
            if startPartyCount <= 10 or startPartyCount % 25 == 0 then
                print(LOG_PREFIX .. "start-area zombie replaced with Party NPC, total="
                    .. tostring(startPartyCount))
            end
        else
            print(LOG_PREFIX .. "Party spawn failed; removed start-area zombie: " .. tostring(err))
        end
        return
    end

    if not shouldConvert(zombie) then
        return
    end

    local ok, err = pcall(function()
        spawnBanditAt(player, zombie, getSpawnClan())
    end)
    if ok then
        removeZombie(zombie)
        convertedCount = convertedCount + 1
        if convertedCount <= 10 or convertedCount % 25 == 0 then
            print(LOG_PREFIX .. "converted zombie to civilian, total=" .. tostring(convertedCount))
        end
    else
        print(LOG_PREFIX .. "spawn failed: " .. tostring(err))
    end
end

-- Mark the mall thin permanently done once the player meets sister (quest advanced past
-- the mall approach). This stops the thin from running again on future reloads while
-- still allowing it to run every tick while the approach is active.
local mallThinCheckTick = 0
local function onConversionReadyTick()
    conversionSessionTick = conversionSessionTick + 1
    if conversionSessionTick >= CONVERSION_STARTUP_DELAY_TICKS then
        conversionReady = true
        print(LOG_PREFIX .. "conversion unlocked after startup grace period")
        Events.OnTick.Remove(onConversionReadyTick)
    end
end
Events.OnTick.Add(onConversionReadyTick)

local function onMallThinTick()
    if isMallThinComplete() then
        Events.OnTick.Remove(onMallThinTick)
        return
    end
    mallThinCheckTick = mallThinCheckTick + 1
    if mallThinCheckTick % 120 ~= 0 then return end
    if ModpackFestivalQuests and ModpackFestivalQuests.isCompleted
        and ModpackFestivalQuests.isCompleted("meet_sister") then
        markMallThinComplete()
        print(LOG_PREFIX .. "mall thin complete — meet_sister done, total removed="
            .. tostring(mallThinRemovedCount))
        Events.OnTick.Remove(onMallThinTick)
    end
end

Events.OnZombieUpdate.Add(onZombieUpdate)
Events.OnTick.Add(onMallThinTick)
print(LOG_PREFIX .. "loaded: " .. tostring(CONVERSION_CHANCE_EARLY)
    .. "% civilian roll per zombie (drops to " .. tostring(CONVERSION_CHANCE_LATE)
    .. "% once find_sister is complete); first mall approach removes "
    .. tostring(SISTER_MALL_THIN_REMOVE_CHANCE) .. "% within "
    .. tostring(SISTER_MALL_THIN_RADIUS) .. " tiles")

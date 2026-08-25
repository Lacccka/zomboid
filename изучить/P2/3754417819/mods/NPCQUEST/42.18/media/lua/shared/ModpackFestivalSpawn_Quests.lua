-- Festival quest definitions and progress (ModData).

ModpackFestivalQuests = ModpackFestivalQuests or {}

local MOD_ID = "ModpackFestivalSpawn"
ModpackFestivalQuests.MOD_ID = MOD_ID

ModpackFestivalQuests.VEHICLE_SPAWN_X = 13855
ModpackFestivalQuests.VEHICLE_SPAWN_Y = 1907
ModpackFestivalQuests.VEHICLE_SPAWN_Z = 0

ModpackFestivalQuests.FIRST_QUEST_DURATION_MS = 35000

-- Mall / Alyssa meet (B42 map ground floor: concourse planter seating — 13595, 1292).
ModpackFestivalQuests.MALL_SISTER_X = 13595
ModpackFestivalQuests.MALL_SISTER_Y = 1292
ModpackFestivalQuests.MALL_SISTER_Z = 0
ModpackFestivalQuests.MALL_ARRIVAL_RADIUS = 90

ModpackFestivalQuests.SISTER_MEET_X = 13595
ModpackFestivalQuests.SISTER_MEET_Y = 1292
ModpackFestivalQuests.SISTER_MEET_Z = 0
-- Player must load this chunk before spawn is attempted.
ModpackFestivalQuests.SISTER_MEET_SPAWN_RADIUS = 72
-- meet_sister: spawn Alyssa when the player enters this bubble around the meet tile.
ModpackFestivalQuests.SISTER_MEET_TRIGGER_RADIUS = 50
-- Find/meet sister quest completion once live Alyssa is close to the player.
ModpackFestivalQuests.SISTER_PROXIMITY_COMPLETE_RADIUS = 6
ModpackFestivalQuests.SISTER_MEET_COMPLETE_RADIUS = ModpackFestivalQuests.SISTER_PROXIMITY_COMPLETE_RADIUS
-- Legacy alias (find_sister reach_point radius).
ModpackFestivalQuests.MALL_SISTER_RADIUS = ModpackFestivalQuests.MALL_ARRIVAL_RADIUS

-- Safe house for end of quest line (B42 map: fenced home, ground floor).
ModpackFestivalQuests.HOME_X = 13344
ModpackFestivalQuests.HOME_Y = 1480
ModpackFestivalQuests.HOME_Z = 0

-- Mall Spawn start: 30 tiles west of Alyssa with festival/car steps already done.
ModpackFestivalQuests.MALL_STORY_SPAWN_X = 13565
ModpackFestivalQuests.MALL_STORY_SPAWN_Y = 1292
ModpackFestivalQuests.MALL_STORY_SPAWN_Z = 0
ModpackFestivalQuests.MALL_STORY_SPAWN_MATCH_RADIUS = 18

ModpackFestivalQuests.QUEST_CHAIN = {
    "whats_going_on",
    "get_to_car",
    "find_sister",
    "meet_sister",
    "get_home",
}

ModpackFestivalQuests.DEFINITIONS = {
    whats_going_on = {
        id = "whats_going_on",
        title = "What's Going On?",
        description = "Why'd the band just bolt off stage?",
        type = "timed",
        durationMs = ModpackFestivalQuests.FIRST_QUEST_DURATION_MS,
        hideTimer = true,
        showCompleteMessage = false,
        showDistance = false,
    },
    get_to_car = {
        id = "get_to_car",
        title = "Find Your Ride",
        description = "People are losing their minds. Find your ride.",
        type = "reach_vehicle",
        radius = 4,
        target = {
            x = ModpackFestivalQuests.VEHICLE_SPAWN_X,
            y = ModpackFestivalQuests.VEHICLE_SPAWN_Y,
            z = ModpackFestivalQuests.VEHICLE_SPAWN_Z,
        },
        completeMessage = "I dropped {sister} off at the mall this morning... Oh no!",
    },
    find_sister = {
        id = "find_sister",
        title = "Find {sister}",
        description = "Hurry to the Mall",
        type = "reach_point",
        radius = ModpackFestivalQuests.MALL_ARRIVAL_RADIUS,
        target = {
            x = ModpackFestivalQuests.MALL_SISTER_X,
            y = ModpackFestivalQuests.MALL_SISTER_Y,
            z = ModpackFestivalQuests.MALL_SISTER_Z,
        },
        sisterCompleteRadius = ModpackFestivalQuests.SISTER_PROXIMITY_COMPLETE_RADIUS,
        showCompleteMessage = false,
        mallArrivalSay = "Please God, don't let me be late.",
    },
    meet_sister = {
        id = "meet_sister",
        title = "Find {sister}",
        description = "She's gotta be around here somewhere.",
        type = "reach_point",
        radius = ModpackFestivalQuests.SISTER_MEET_COMPLETE_RADIUS,
        target = {
            x = ModpackFestivalQuests.SISTER_MEET_X,
            y = ModpackFestivalQuests.SISTER_MEET_Y,
            z = ModpackFestivalQuests.SISTER_MEET_Z,
        },
        sisterCompleteRadius = ModpackFestivalQuests.SISTER_PROXIMITY_COMPLETE_RADIUS,
        showCompleteMessage = false,
    },
    get_home = {
        id = "get_home",
        title = "Get Home",
        description = "We need to get the hell out of here! Home isn't far.",
        type = "reach_point",
        radius = 4,
        target = {
            x = ModpackFestivalQuests.HOME_X,
            y = ModpackFestivalQuests.HOME_Y,
            z = ModpackFestivalQuests.HOME_Z,
        },
        completeMessage = "You're home. Barricade up and survive the night.",
        completePlayerSay = "Made it. Time to get ready for tonight.",
    },
}

function ModpackFestivalQuests.getState()
    return ModData.getOrCreate(MOD_ID)
end

function ModpackFestivalQuests.getUiState()
    local md = ModpackFestivalQuests.getState()
    if not md.questUi then
        md.questUi = {
            x = nil,
            y = nil,
            minimized = false,
        }
    end
    return md.questUi
end

function ModpackFestivalQuests.saveUiLayout(x, y, minimized)
    local ui = ModpackFestivalQuests.getUiState()
    if x then ui.x = x end
    if y then ui.y = y end
    if minimized ~= nil then ui.minimized = minimized end
end

function ModpackFestivalQuests.getQuestProgress()
    local md = ModpackFestivalQuests.getState()
    if not md.quests then
        md.quests = {
            completed = {},
            activeId = nil,
            timedQuestStartedAtMs = nil,
            vehicleX = nil,
            vehicleY = nil,
            vehicleZ = nil,
            spokenStartLines = {},
        }
    end
    if not md.quests.spokenStartLines then
        md.quests.spokenStartLines = {}
    end
    ModpackFestivalQuests.migrateQuestSave(md.quests)
    return md.quests
end

function ModpackFestivalQuests.migrateQuestSave(qp)
    if not qp then
        return
    end
    if qp.enjoyMusicStartedAtMs and not qp.timedQuestStartedAtMs then
        qp.timedQuestStartedAtMs = qp.enjoyMusicStartedAtMs
        qp.enjoyMusicStartedAtMs = nil
    end
    if qp.completed and qp.completed.enjoy_music then
        qp.completed.whats_going_on = true
        qp.completed.enjoy_music = nil
    end
    if qp.activeId == "enjoy_music" then
        qp.activeId = "whats_going_on"
    end
    if qp.spokenStartLines and qp.spokenStartLines.enjoy_music then
        qp.spokenStartLines.whats_going_on = qp.spokenStartLines.enjoy_music
        qp.spokenStartLines.enjoy_music = nil
    end
end

function ModpackFestivalQuests.hasSpokenStartLine(questId)
    local qp = ModpackFestivalQuests.getQuestProgress()
    return qp.spokenStartLines[questId] == true
end

function ModpackFestivalQuests.markStartLineSpoken(questId)
    if not questId then return end
    ModpackFestivalQuests.getQuestProgress().spokenStartLines[questId] = true
end

function ModpackFestivalQuests.getDefinition(questId)
    return ModpackFestivalQuests.DEFINITIONS[questId]
end

ModpackFestivalQuests.DEFAULT_SISTER_NAME = "Alyssa"
ModpackFestivalQuests.SISTER_NAME_TOKEN = "{sister}"

function ModpackFestivalQuests.parseSisterForenameFromBuildString(buildString)
    if ModpackFestivalSister and ModpackFestivalSister.parseForenameFromBuildString then
        return ModpackFestivalSister.parseForenameFromBuildString(buildString)
    end
    if not buildString or buildString == "" then return nil end
    local namePart = buildString:match("name=([^;]+)")
    if not namePart then return nil end
    local forename = namePart:match("^([^|]*)")
    if forename and forename ~= "" then
        return forename
    end
    return nil
end

function ModpackFestivalQuests.getSisterForename(player)
    player = player or (getSpecificPlayer and getSpecificPlayer(0))

    if player and player.getModData then
        local pmd = player:getModData()
        if pmd.modpackSisterForename and pmd.modpackSisterForename ~= "" then
            return pmd.modpackSisterForename
        end
    end

    local md = ModpackFestivalQuests.getState()
    if md.sisterForename and md.sisterForename ~= "" then
        return md.sisterForename
    end
    if md.sisterBuildString then
        local fromBuild = ModpackFestivalQuests.parseSisterForenameFromBuildString(md.sisterBuildString)
        if fromBuild then
            md.sisterForename = fromBuild
            return fromBuild
        end
    end
    local appearance = md.sisterAppearanceData
    if appearance and appearance.forename and appearance.forename ~= "" then
        md.sisterForename = appearance.forename
        return appearance.forename
    end

    return ModpackFestivalQuests.DEFAULT_SISTER_NAME
end

function ModpackFestivalQuests.rememberSisterForename(forename, player)
    if not forename or forename == "" then return end

    pcall(function()
        local md = ModpackFestivalQuests.getState()
        md.sisterForename = forename
    end)

    player = player or (getSpecificPlayer and getSpecificPlayer(0))
    if player and player.getModData then
        pcall(function()
            local pmd = player:getModData()
            pmd.modpackSisterForename = forename
            if player.transmitModData then
                player:transmitModData()
            end
        end)
    end
end

function ModpackFestivalQuests.questTextNeedsSisterName(text)
    if not text or text == "" then return false end
    local token = ModpackFestivalQuests.SISTER_NAME_TOKEN
    if token and string.find(text, token, 1, true) then
        return true
    end
    return string.find(text, "%%s", 1, true) ~= nil
end

function ModpackFestivalQuests.formatQuestText(text, player)
    if not text or text == "" then return text end
    if not ModpackFestivalQuests.questTextNeedsSisterName(text) then
        return text
    end

    local name = ModpackFestivalQuests.getSisterForename(player)
    if not name or name == "" then
        name = ModpackFestivalQuests.DEFAULT_SISTER_NAME
    end

    local out = text
    local token = ModpackFestivalQuests.SISTER_NAME_TOKEN
    if token then
        out = out:gsub(token, name)
    end
    if string.find(out, "%%s", 1, true) then
        out = out:gsub("%%s", name)
    end
    return out
end

local QUEST_DISPLAY_TEXT_KEYS = {
    title = true,
    description = true,
    completeMessage = true,
    startMessage = true,
    mallArrivalSay = true,
    playerMeetSay = true,
    sisterMeetSay = true,
    completePlayerSay = true,
}

function ModpackFestivalQuests.getDisplayQuest(questId, player)
    local def = ModpackFestivalQuests.getDefinition(questId)
    if not def then return nil end

    local display = {}
    for key, value in pairs(def) do
        display[key] = value
    end
    for key in pairs(QUEST_DISPLAY_TEXT_KEYS) do
        if type(display[key]) == "string" then
            display[key] = ModpackFestivalQuests.formatQuestText(display[key], player)
        end
    end
    return display
end

function ModpackFestivalQuests.isCompleted(questId)
    local qp = ModpackFestivalQuests.getQuestProgress()
    return qp.completed[questId] == true
end

function ModpackFestivalQuests.getActiveQuestId()
    local qp = ModpackFestivalQuests.getQuestProgress()
    local id = qp.activeId
    if id and ModpackFestivalFeatures and ModpackFestivalFeatures.isSisterQuest(id)
        and not ModpackFestivalFeatures.isSisterQuestEnabled() then
        return nil
    end
    return id
end

function ModpackFestivalQuests.getActiveQuest(player)
    local questId = ModpackFestivalQuests.getActiveQuestId()
    if not questId then return nil end
    player = player or (getSpecificPlayer and getSpecificPlayer(0))
    return ModpackFestivalQuests.getDisplayQuest(questId, player)
end

function ModpackFestivalQuests.nextQuestId(afterId)
    local found = afterId == nil
    for i = 1, #ModpackFestivalQuests.QUEST_CHAIN do
        local id = ModpackFestivalQuests.QUEST_CHAIN[i]
        if ModpackFestivalFeatures and ModpackFestivalFeatures.isSisterQuest(id)
            and not ModpackFestivalFeatures.isSisterQuestEnabled() then
            if id == afterId then
                found = true
            end
        elseif found and not ModpackFestivalQuests.isCompleted(id) then
            return id
        end
        if id == afterId then
            found = true
        end
    end
    return nil
end

function ModpackFestivalQuests.activateQuest(questId)
    if ModpackFestivalFeatures and ModpackFestivalFeatures.isSisterQuest(questId)
        and not ModpackFestivalFeatures.isSisterQuestEnabled() then
        return
    end
    local qp = ModpackFestivalQuests.getQuestProgress()
    qp.activeId = questId
    if questId == "whats_going_on" then
        qp.timedQuestStartedAtMs = getTimestampMs()
    end
end

function ModpackFestivalQuests.isAtMallStorySpawn(x, y, z)
    if x == nil or y == nil then return false end
    if z ~= nil and math.floor(z + 0.5) ~= ModpackFestivalQuests.MALL_STORY_SPAWN_Z then
        return false
    end
    local dx = x - ModpackFestivalQuests.MALL_STORY_SPAWN_X
    local dy = y - ModpackFestivalQuests.MALL_STORY_SPAWN_Y
    local r = ModpackFestivalQuests.MALL_STORY_SPAWN_MATCH_RADIUS
    return (dx * dx + dy * dy) <= (r * r)
end

function ModpackFestivalQuests.applyMallStoryStartFlags()
    local md = ModpackFestivalQuests.getState()
    md.festivalSpawnDone = true
    md.festivalRadiosPlaced = true
end

function ModpackFestivalQuests.tryBootstrapMallStoryStart(player)
    if ModpackFestivalFeatures and ModpackFestivalFeatures.isMallSpawnEnabled
        and not ModpackFestivalFeatures.isMallSpawnEnabled() then
        return false
    end
    if ModpackFestivalFeatures and not ModpackFestivalFeatures.isSisterQuestEnabled() then
        return false
    end
    if not player then return false end

    local qp = ModpackFestivalQuests.getQuestProgress()
    if qp.mallStoryStartApplied then return false end

    if not ModpackFestivalQuests.isAtMallStorySpawn(player:getX(), player:getY(), player:getZ()) then
        return false
    end

    if ModpackFestivalQuests.isCompleted("find_sister")
        or ModpackFestivalQuests.isCompleted("meet_sister")
        or ModpackFestivalQuests.isCompleted("get_home") then
        qp.mallStoryStartApplied = true
        return false
    end

    if qp.activeId == "meet_sister" or qp.activeId == "get_home" then
        qp.mallStoryStartApplied = true
        return false
    end

    if (qp.activeId == "find_sister" or qp.activeId == "meet_sister")
        and qp.completed.whats_going_on == true
        and qp.completed.get_to_car == true
        and qp.completed.find_sister == true then
        qp.mallStoryStartApplied = true
        ModpackFestivalQuests.applyMallStoryStartFlags()
        return true
    end

    if qp.activeId and qp.activeId ~= "whats_going_on" and qp.activeId ~= "get_to_car" then
        return false
    end

    for i = 1, #ModpackFestivalQuests.QUEST_CHAIN do
        local id = ModpackFestivalQuests.QUEST_CHAIN[i]
        if id == "meet_sister" then
            ModpackFestivalQuests.activateQuest(id)
            break
        end
        qp.completed[id] = true
        qp.spokenStartLines[id] = true
    end

    if not qp.timedQuestStartedAtMs then
        qp.timedQuestStartedAtMs = getTimestampMs()
    end

    ModpackFestivalQuests.setVehicleLocation(
        ModpackFestivalQuests.VEHICLE_SPAWN_X,
        ModpackFestivalQuests.VEHICLE_SPAWN_Y,
        ModpackFestivalQuests.VEHICLE_SPAWN_Z
    )

    qp.mallStoryStartApplied = true
    ModpackFestivalQuests.applyMallStoryStartFlags()

    print("[" .. MOD_ID .. "] mall spawn: quests bootstrapped to meet_sister")
    return true
end

function ModpackFestivalQuests.ensureQuestLineStarted(player)
    player = player or (getSpecificPlayer and getSpecificPlayer(0))
    if player and ModpackFestivalQuests.tryBootstrapMallStoryStart(player) then
        return ModpackFestivalQuests.getQuestProgress().activeId
    end

    local qp = ModpackFestivalQuests.getQuestProgress()
    if qp.activeId then
        if qp.activeId == "whats_going_on" and not qp.timedQuestStartedAtMs then
            qp.timedQuestStartedAtMs = getTimestampMs()
        end
        return qp.activeId
    end

    local nextId = ModpackFestivalQuests.nextQuestId(nil)
    if nextId then
        ModpackFestivalQuests.activateQuest(nextId)
    end
    return qp.activeId
end

function ModpackFestivalQuests.getTimedQuestRemainingSec(quest)
    if not quest or quest.type ~= "timed" then return nil end
    local qp = ModpackFestivalQuests.getQuestProgress()
    if not qp.timedQuestStartedAtMs or not quest.durationMs then return nil end
    local leftMs = quest.durationMs - (getTimestampMs() - qp.timedQuestStartedAtMs)
    return math.max(0, math.ceil(leftMs / 1000))
end

function ModpackFestivalQuests.setVehicleLocation(x, y, z)
    local qp = ModpackFestivalQuests.getQuestProgress()
    qp.vehicleX = x
    qp.vehicleY = y
    qp.vehicleZ = z or 0
end

function ModpackFestivalQuests.getVehicleTarget()
    local qp = ModpackFestivalQuests.getQuestProgress()
    if qp.vehicleX and qp.vehicleY then
        return qp.vehicleX, qp.vehicleY, qp.vehicleZ or 0
    end
    return ModpackFestivalQuests.VEHICLE_SPAWN_X, ModpackFestivalQuests.VEHICLE_SPAWN_Y, ModpackFestivalQuests.VEHICLE_SPAWN_Z
end

function ModpackFestivalQuests.findFestivalVehicle(cell)
    if not cell then return nil end

    local cx, cy, cz = ModpackFestivalQuests.getVehicleTarget()
    for dx = -3, 3 do
        for dy = -3, 3 do
            local sq = cell:getGridSquare(cx + dx, cy + dy, cz)
            if sq then
                local vehicle = sq:getVehicleContainer()
                if vehicle then
                    return vehicle
                end
            end
        end
    end
    return nil
end

function ModpackFestivalQuests.getQuestTargetXY(player, quest)
    if not player or not quest then return nil end

    local tx, ty

    if quest.type == "timed" then
        if quest.target then
            tx, ty = quest.target.x, quest.target.y
        else
            return nil
        end
    elseif quest.type == "reach_vehicle" then
        local cell = getCell()
        local vehicle = cell and ModpackFestivalQuests.findFestivalVehicle(cell) or nil
        if vehicle then
            tx, ty = vehicle:getX(), vehicle:getY()
        else
            tx, ty = ModpackFestivalQuests.getVehicleTarget()
        end
    elseif quest.target then
        tx, ty = quest.target.x, quest.target.y
    else
        return nil
    end

    if not tx or not ty then
        return nil
    end
    return tx, ty
end

-- World bearing in degrees (PZ: 0 = south, 90 = west, 180 = north, 270 = east).
function ModpackFestivalQuests.getWorldBearingDeg(x1, y1, x2, y2)
    local dx, dy = x2 - x1, y2 - y1
    if (dx * dx + dy * dy) < 0.0001 then
        return 0
    end
    -- Same atan2 convention as vanilla foraging icons: atan2(dx, -dy), shifted to PZ facing degrees.
    return (math.deg(math.atan2(dx, -dy)) + 180) % 360
end

-- Quest compass arrow: rotation in radians for the UI (0 = north / up on the widget).
-- Uses the same world target as the map marker (getQuestTargetXY).
function ModpackFestivalQuests.getQuestArrowRotationRad(player, quest)
    if not player or not quest then return nil end
    if not ModpackFestivalQuests.questHasNavigationTarget(quest) then return nil end

    local tx, ty = ModpackFestivalQuests.getQuestTargetXY(player, quest)
    if not tx then return nil end

    local px, py = player:getX(), player:getY()
    local dx, dy = tx - px, ty - py
    if (dx * dx + dy * dy) < 0.0001 then
        return 0
    end

    -- Map-aligned compass: up = north (matches the world map), not vehicle facing.
    return math.atan2(dx, -dy)
end

function ModpackFestivalQuests.questHasNavigationTarget(quest)
    if not quest or quest.showDistance == false then
        return false
    end
    if quest.type == "reach_vehicle" or quest.type == "reach_point" then
        return true
    end
    if quest.type == "timed" and quest.target then
        return true
    end
    return false
end

function ModpackFestivalQuests.distToQuestTarget(player, quest)
    if not player or not quest then return 9999 end

    local tx, ty = ModpackFestivalQuests.getQuestTargetXY(player, quest)
    if not tx then return 9999 end

    local dx = player:getX() - tx
    local dy = player:getY() - ty
    return math.sqrt(dx * dx + dy * dy)
end

function ModpackFestivalQuests.isPlayerAtFestivalCar(player, radius)
    local carQuest = ModpackFestivalQuests.DEFINITIONS.get_to_car
    if not carQuest or not player then
        return false
    end
    local r = radius or carQuest.radius or 4
    return ModpackFestivalQuests.distToQuestTarget(player, carQuest) <= r
end

function ModpackFestivalQuests.findLiveSister()
    if not ModpackFestivalSister or not ModpackFestivalSister.findSisterBandit then
        return nil
    end
    return ModpackFestivalSister.findSisterBandit()
end

function ModpackFestivalQuests.isSisterWithinCompletionRadius(player, radius)
    if not player then return false end
    local sister = ModpackFestivalQuests.findLiveSister()
    if not sister then return false end
    local pz = player.getZ and player:getZ() or 0
    local sz = sister.getZ and sister:getZ() or pz
    if math.abs((sz or 0) - (pz or 0)) > 0.75 then
        return false
    end
    local r = radius or ModpackFestivalQuests.SISTER_PROXIMITY_COMPLETE_RADIUS
    local dx = player:getX() - sister:getX()
    local dy = player:getY() - sister:getY()
    return (dx * dx + dy * dy) <= (r * r)
end

function ModpackFestivalQuests.isQuestComplete(player, quest)
    if not player or not quest then return false end

    if quest.type == "timed" then
        local qp = ModpackFestivalQuests.getQuestProgress()
        if not qp.timedQuestStartedAtMs or not quest.durationMs then return false end
        return (getTimestampMs() - qp.timedQuestStartedAtMs) >= quest.durationMs
    end

    if quest.type == "reach_vehicle" or quest.type == "reach_point" then
        if quest.id == "find_sister" or quest.id == "meet_sister" then
            local sister = ModpackFestivalQuests.findLiveSister()
            if sister then
                return ModpackFestivalQuests.isSisterWithinCompletionRadius(
                    player,
                    quest.sisterCompleteRadius or ModpackFestivalQuests.SISTER_PROXIMITY_COMPLETE_RADIUS
                )
            end
        end
        local radius = quest.radius or 4
        return ModpackFestivalQuests.distToQuestTarget(player, quest) <= radius
    end

    return false
end

function ModpackFestivalQuests.completeQuest(questId)
    if ModpackFestivalFeatures and ModpackFestivalFeatures.isSisterQuest(questId)
        and not ModpackFestivalFeatures.isSisterQuestEnabled() then
        return nil
    end
    local qp = ModpackFestivalQuests.getQuestProgress()
    if qp.completed[questId] then
        return ModpackFestivalQuests.getDefinition(questId)
    end
    qp.completed[questId] = true
    local nextId = ModpackFestivalQuests.nextQuestId(questId)
    if nextId then
        ModpackFestivalQuests.activateQuest(nextId)
    else
        qp.activeId = nil
    end

    -- Global ModData only hits disk on the normal autosave/quit cycle, so a crash
    -- shortly after finishing a quest can roll progress back to before it. Force
    -- an immediate save here so a completed quest can't be lost that way.
    if isServer() and saveGame then
        local ok, err = pcall(saveGame)
        if not ok then
            print("[" .. MOD_ID .. "] completeQuest: saveGame failed: " .. tostring(err))
        end
    end

    return ModpackFestivalQuests.getDefinition(questId)
end

print("[" .. MOD_ID .. "] quest definitions loaded")

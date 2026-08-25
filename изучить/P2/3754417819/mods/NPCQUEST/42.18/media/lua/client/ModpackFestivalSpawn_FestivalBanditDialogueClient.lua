-- Party NPC dialogue for the rebuilt festival start.

if isServer() and not isClient() then
    return
end

ModpackFestivalStartBanditDialogue = ModpackFestivalStartBanditDialogue or {}

local MOD_ID = "ModpackFestivalSpawn"
local PARTY_CLAN_UUID = "42364b66-ab03-4c38-b374-5575a0c24868"
local FESTIVAL_SPAWN_X = 13737
local FESTIVAL_SPAWN_Y = 1962

local DIALOGUE_RADIUS = 70
local DIALOGUE_INTERVAL_TICKS = 180
local MIN_LINE_DELAY_MS = 9000
local MAX_SPEAKERS_PER_TICK = 2

local tickCount = 0

local CONCERT_LINES = {
    "Why'd the band stop?",
    "Is this part of the show?",
    "They just cut the speakers.",
    "Did somebody pull the power?",
    "I can't hear the stage anymore.",
    "Why is everyone looking toward the road?",
    "The concert can't be over already.",
    "Something's wrong backstage.",
    "Are they evacuating us?",
    "This better not be some prank.",
}

local PANIC_LINES = {
    "Those people are eating him!",
    "The walking dead are here!",
    "Run! They're killing people!",
    "What the hell are those things?",
    "They're not drunk, they're dead!",
    "Somebody help them!",
    "They're biting everyone!",
    "Get away from the fences!",
    "No, no, no, this can't be real!",
    "Move! They're coming through!",
}

local function distSqXY(ax, ay, bx, by)
    local dx = ax - bx
    local dy = ay - by
    return dx * dx + dy * dy
end

local function getBrain(zombie)
    if not zombie or not zombie.getModData then
        return nil
    end
    local md = zombie:getModData()
    return md and md.brain
end

local function isPartyBandit(zombie)
    if not zombie or (zombie.isDead and zombie:isDead()) then
        return false
    end
    if not zombie.getVariableBoolean or not zombie:getVariableBoolean("Bandit") then
        return false
    end
    local brain = getBrain(zombie)
    if brain and brain.cid == PARTY_CLAN_UUID then
        return true
    end
    local md = zombie:getModData()
    return md and md.modpackFestivalStartBandit == true
end

local function isNearFestivalStart(zombie)
    return distSqXY(zombie:getX(), zombie:getY(), FESTIVAL_SPAWN_X, FESTIVAL_SPAWN_Y)
        <= (DIALOGUE_RADIUS * DIALOGUE_RADIUS)
end

local function getDialoguePhase()
    if ModpackFestivalQuests then
        local activeId = ModpackFestivalQuests.getActiveQuestId
            and ModpackFestivalQuests.getActiveQuestId()
        if activeId and activeId ~= "whats_going_on" then
            return "panic"
        end
        if ModpackFestivalQuests.isCompleted
            and ModpackFestivalQuests.isCompleted("whats_going_on") then
            return "panic"
        end
    end
    return "concert"
end

local function pickLine(lines)
    if not lines or #lines == 0 then
        return nil
    end
    if ZombRand then
        return lines[ZombRand(#lines) + 1]
    end
    return lines[1]
end

local function sayLine(zombie, line, phase)
    if not zombie or not line or line == "" then
        return false
    end
    local md = zombie:getModData()
    local now = getTimestampMs and getTimestampMs() or 0
    if md.modpackFestivalNextDialogueMs and now < md.modpackFestivalNextDialogueMs then
        return false
    end

    local r, g, b = 0.95, 0.85, 0.2
    if phase == "panic" then
        r, g, b = 1.0, 0.18, 0.12
    end

    if zombie.addLineChatElement then
        zombie:addLineChatElement(line, r, g, b)
    elseif zombie.Say then
        zombie:Say(line)
    else
        return false
    end

    md.modpackFestivalDialoguePhase = phase
    md.modpackFestivalNextDialogueMs = now + MIN_LINE_DELAY_MS + (ZombRand and ZombRand(6000) or 0)
    return true
end

local function tickDialogue()
    local player = getSpecificPlayer and getSpecificPlayer(0)
    if not player then
        return
    end
    local cell = getCell and getCell()
    if not cell or not cell.getZombieList then
        return
    end
    local zombies = cell:getZombieList()
    if not zombies or not zombies.size then
        return
    end

    local phase = getDialoguePhase()
    local lines = phase == "panic" and PANIC_LINES or CONCERT_LINES
    local spoken = 0

    for i = 0, zombies:size() - 1 do
        local zombie = zombies:get(i)
        if isPartyBandit(zombie)
            and isNearFestivalStart(zombie)
            and distSqXY(player:getX(), player:getY(), zombie:getX(), zombie:getY())
                <= (DIALOGUE_RADIUS * DIALOGUE_RADIUS) then
            if sayLine(zombie, pickLine(lines), phase) then
                spoken = spoken + 1
                if spoken >= MAX_SPEAKERS_PER_TICK then
                    return
                end
            end
        end
    end
end

local function onTick()
    tickCount = tickCount + 1
    if not ModpackFestivalTick.every(tickCount, DIALOGUE_INTERVAL_TICKS) then
        return
    end
    tickDialogue()
end

Events.OnTick.Add(onTick)
print("[" .. MOD_ID .. "] Party NPC dialogue client loaded")

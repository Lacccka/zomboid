MissionsEvents = MissionsEvents or {}
MissionsEvents.M2 = MissionsEvents.M2 or {}

local M2 = MissionsEvents.M2

-- =========================
-- PLAYER DATA
-- =========================
M2.players = M2.players or {}

local function getPlayerID(player)
    if not player then return "unknown" end
    return player:getOnlineID() or player:getUsername()
end

local function getPlayerData(player)
    local id = getPlayerID(player)

    if not M2.players[id] then
        M2.players[id] = {
            active = false,
            type = nil,
            lastStart = 0,
            endTime = 0
        }
    end

    return M2.players[id]
end

-- =========================
-- CONFIG
-- =========================
local function getConfig()
    local sv = SandboxVars and SandboxVars.MissionsEvents

    local cfg = {
        enabled = sv and sv.M2_Enable ~= false,
        carpentry = sv and sv.M2_Carpentry ~= false,
        electrician = sv and sv.M2_Electrician ~= false,
        cooldown = (sv and sv.M2_CooldownHours or 2) * 60,
        duration = (sv and sv.M2_DurationMinutes or 30),
        rewardsCarpentry = sv and sv.M2_RewardsCarpentry or "",
        rewardsElectrician = sv and sv.M2_RewardsElectrician or ""
    }

    return cfg
end

-- =========================
-- SELECT TYPE
-- =========================
local function getRandomType(cfg)

    if not cfg.carpentry and not cfg.electrician then
        return nil
    end

    if cfg.carpentry and not cfg.electrician then
        return "carpentry"
    end

    if not cfg.carpentry and cfg.electrician then
        return "electrician"
    end

    return ZombRand(2) == 0 and "carpentry" or "electrician"
end

-- =========================
-- PARSE REWARDS
-- =========================
local function parseRewards(str)
    local items = {}

    for item in string.gmatch(str, "([^,]+)") do
        item = string.gsub(item, "%s+", "")
        table.insert(items, item)
    end

    return items
end

local function giveReward(player, rewards)

    local square = player:getSquare()
    if not square then return end

    local bag = instanceItem("Base.Bag_ToolBag")
    if not bag then return end

    local container = bag:getItemContainer()
    if not container then return end

    for _, itemID in ipairs(rewards) do

        local item = instanceItem(itemID)

        if item then
            container:AddItem(item)
        end
    end

    square:AddWorldInventoryItem(bag, 0.5, 0.5, 0)
end

local function consumeItems(player, requiredItems)

    local inv = player:getInventory()
    if not inv then return false end

    local items = inv:getItems()

    local toRemove = {}

    for fullType, amount in pairs(requiredItems) do

        local found = 0

        for i = 0, items:size()-1 do
            local it = items:get(i)

            if it and it:getFullType() == fullType then
                table.insert(toRemove, it)
                found = found + 1

                if found >= amount then break end
            end
        end

        if found < amount then
            return false
        end
    end

    for _, it in ipairs(toRemove) do

        local id = it:getID()

        sendServerCommand(player, "MissionsEvents", "RemoveItem", {
            id = id
        })

        inv:Remove(it)
    end

    return true
end

-- =========================
-- START EVENT
-- =========================
function M2.start(player)

    if not player then return end

    local cfg = getConfig()
    if not cfg.enabled then return end

    local data = getPlayerData(player)
    local now = getGameTime():getWorldAgeHours() * 60

    if (now - data.lastStart) < cfg.cooldown then
        return
    end

    local m1Players = MissionsEvents.M1 and MissionsEvents.M1.players

    if m1Players then
        local id = player:getOnlineID() or player:getUsername()
        local m1 = m1Players[id]

        if m1 and type(m1.lastStart) == "number" then

            local cooldown = (SandboxVars.MissionsEvents.M1_CooldownHours or 2) * 60

            if (now - m1.lastStart) < cooldown then

                sendServerCommand(player, "MissionsEvents", "HaloMessage", {
                    text = "IGUI_M2_BlockedByM1"
                })

                return
            end
        end
    end

    local eventType = getRandomType(cfg)
    if not eventType then return end

    local duration = cfg.duration or 10

    data.active = true
    data.type = eventType
    data.lastStart = now
    data.endTime = now + duration

    sendServerCommand(player, "MissionsEvents", "SyncM2", {
        active = true,
        type = eventType,
        lastStart = data.lastStart,
        endTime = data.endTime
    })
end

-- =========================
-- CONFIRM EVENT
-- =========================
function M2.confirm(player)

    local data = getPlayerData(player)
    if not data.active then return end

    local now = getGameTime():getWorldAgeHours() * 60

    if now >= data.endTime then
        data.active = false
        data.lastStart = now
        return
    end

    local required = MissionsEvents.M2.getRequiredItems(data.type)
    if not required then return end

    local ok = consumeItems(player, required)
    if not ok then return end

    local cfg = getConfig()

    local rewardsStr = (data.type == "carpentry")
        and cfg.rewardsCarpentry
        or cfg.rewardsElectrician

    giveReward(player, parseRewards(rewardsStr))

    data.active = false
    data.lastStart = now

    sendServerCommand(player, "MissionsEvents", "SyncM2", {
        active = false
    })
end

-- =========================
-- FAIL BY TIME
-- =========================
Events.OnPlayerUpdate.Add(function(player)

    local data = getPlayerData(player)
    if not data.active then return end

    local now = getGameTime():getWorldAgeHours() * 60

    if now >= data.endTime then
        data.active = false
        data.lastStart = now

        sendServerCommand(player, "MissionsEvents", "SyncM2", {
            active = false
        })
    end
end)

-- =========================
-- CLIENT COMMANDS
-- =========================
Events.OnClientCommand.Add(function(module, command, player, args)

    if module ~= "MissionsEvents" then return end

    if command == "StartM2" then
        M2.start(player)
    end

    if command == "ConfirmM2" then
        M2.confirm(player)
    end
end)
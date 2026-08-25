MissionsEvents = MissionsEvents or {}
MissionsEvents.M1 = MissionsEvents.M1 or {}

local M1 = MissionsEvents.M1

-- =========================
-- DATA POR JUGADOR (CLIENT CACHE)
-- =========================
M1.players = M1.players or {}

local function getPlayerID(player)
    return player:getOnlineID() or player:getUsername()
end

local function getPlayerData(player)
    local id = getPlayerID(player)

    if not M1.players[id] then
        M1.players[id] = {
            active = false,
            current = 0,
            max = 0,
            lastStart = 0,
            endTime = 0
        }
    end

    return M1.players[id]
end

-- =========================
-- CONFIG (SANDBOX)
-- =========================
local function getConfig()
    local sv = SandboxVars and SandboxVars.MissionsEvents

    return {
        enabled = sv and sv.M1_Enable ~= false,
        maxZ = sv and sv.M1_Zombies or 20,
        cooldown = (sv and sv.M1_CooldownHours or 2) * 60,
    }
end

-- =========================
-- UPDATE DESDE SERVER
-- =========================
function M1.updateFromServer(player, newData)

    if not player or not newData then return end

    local data = getPlayerData(player)

    data.active = newData.active or false
    data.current = newData.kills or 0
    data.max = newData.maxKills or 0
    data.lastStart = newData.lastStart or 0
    data.endTime = newData.endTime or 0
end

-- =========================
-- UI DATA (LO QUE USA LA UI)
-- =========================
function MissionsEvents.M1.getUIData(player)
    local data = getPlayerData(player)

    return {
        enabled = getConfig().enabled,
        active = data.active,
        current = data.current,
        max = data.max,
        lastStart = data.lastStart,
        endTime = data.endTime
    }
end
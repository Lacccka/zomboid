MissionsEvents = MissionsEvents or {}
MissionsEvents.M2 = MissionsEvents.M2 or {}

local M2 = MissionsEvents.M2

-- =========================
-- DATA POR JUGADOR
-- =========================
M2.players = M2.players or {}

local function getPlayerID(player)
    return player:getOnlineID() or player:getUsername()
end

local function getPlayerData(player)
    local id = getPlayerID(player)

    if not M2.players[id] then
        M2.players[id] = {
            active = false,
            type = nil, -- "carpentry" o "electrician"
            lastStart = 0,
            endTime = 0
        }
    end

    return M2.players[id]
end

-- =========================
-- ITEMS REQUERIDOS
-- =========================
M2.Items = {

    carpentry = {
        ["Base.Log"] = 1,
        ["Base.Plank"] = 3,
        ["Base.DenimStrips"] = 15,
        ["Base.RippedSheets"] = 20
    },

    electrician = {
        ["Base.Amplifier"] = 4,
        ["Base.Headphones"] = 1,
        ["Base.ElectronicsScrap"] = 10,
        ["Base.ElectricWire"] = 4,
        ["Base.RadioTransmitter"] = 5
    }
}

-- =========================
-- CONFIG (SANDBOX)
-- =========================
local function getConfig()
    local sv = SandboxVars and SandboxVars.MissionsEvents

    return {
        enabled = sv and sv.M2_Enable ~= false,
        carpentry = sv and sv.M2_Carpentry ~= false,
        electrician = sv and sv.M2_Electrician ~= false,
        cooldown = (sv and sv.M2_CooldownHours or 2) * 60,
        duration = (sv and sv.M2_DurationMinutes or 30)
    }
end

-- =========================
-- SELECT EVENT TYPE
-- =========================
function M2.getRandomEventType()

    local cfg = getConfig()

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

function M2.getRequiredItems(eventType)
    return M2.Items[eventType]
end

-- =========================
-- UPDATE DESDE SERVER
-- =========================
function M2.updateFromServer(player, newData)

    if not player or not newData then return end

    local data = getPlayerData(player)

    data.active = newData.active or false
    data.type = newData.type or data.type
    data.lastStart = newData.lastStart or data.lastStart
    data.endTime = newData.endTime or data.endTime
end

-- =========================
-- UI DATA
-- =========================
function M2.getUIData(player)

    local data = getPlayerData(player)
    local cfg = getConfig()

    return {
        enabled = cfg.enabled,
        active = data.active,
        type = data.type,
        lastStart = data.lastStart,
        endTime = data.endTime,
        requiredItems = data.type and M2.getRequiredItems(data.type) or nil
    }
end
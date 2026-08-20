require "PPO_Config"
require "PPO_MultiplierMath"
require "PPO_ToneMath"

PPO = PPO or {}
PPO.ClientRuntime = PPO.ClientRuntime or {
    Loaded = true,
    CarryBases = {},
    LastState = {},
}

local ClientRuntime = PPO.ClientRuntime
ClientRuntime.onStateChanged = ClientRuntime.onStateChanged or function() end

local function finiteOr(value, fallback)
    if type(value) ~= "number" or value ~= value
            or value == math.huge or value == -math.huge then
        return fallback
    end
    return value
end

local function readBase(character)
    local ok, value = pcall(function()
        return character:getMaxWeightBase()
    end)
    if not ok then return nil end
    local resolved = finiteOr(value, nil)
    if resolved == nil then return nil end
    return math.floor(resolved)
end

local function writeBase(character, value)
    return pcall(function()
        character:setMaxWeightBase(value)
    end)
end

-- The Strength ladder vanilla multiplies the carry base by; see
-- PPO.ToneMath.carryBaseDelta for why the bonus is divided by it.
local function readWeightMod(character)
    local ok, value = pcall(function()
        return character:getWeightMod()
    end)
    if not ok then return 1 end
    return finiteOr(value, 1)
end

-- Single player runs the server production against the very character sitting
-- in front of the player, and the report is delivered to this file through a
-- local event rather than the network. Both seams would then write the same
-- field, and the second one seeds its record from a base the first had already
-- raised -- paying the bonus twice for anyone who loads with a live tone. The
-- server owns the seam wherever it can reach the character itself.
local function serverOwnsCarrySeam()
    if type(isClient) ~= "function" or type(isServer) ~= "function" then
        return false
    end
    local clientOk, client = pcall(isClient)
    local serverOk, server = pcall(isServer)
    return clientOk and serverOk and client == false and server == false
end

local function activePlayers()
    local ok, count = pcall(getNumActivePlayers)
    count = ok and finiteOr(count, 0) or 0
    count = math.max(0, math.floor(count))
    local players = {}
    for index = 0, count - 1 do
        local readOK, character = pcall(getSpecificPlayer, index)
        if readOK and character ~= nil then
            table.insert(players, character)
        end
    end
    return players
end

local function characterOnlineID(character)
    if character == nil or type(character.getOnlineID) ~= "function" then
        return nil
    end
    local ok, value = pcall(character.getOnlineID, character)
    if not ok then return nil end
    value = finiteOr(value, -1)
    if value < 0 then return nil end
    return value
end

local function characterUsername(character)
    if character == nil or type(character.getUsername) ~= "function" then
        return nil
    end
    local ok, value = pcall(character.getUsername, character)
    if not ok or type(value) ~= "string" or value == "" then return nil end
    return value
end

function ClientRuntime.resolveCharacter(payload)
    if type(payload) ~= "table" then return nil end
    local players = activePlayers()
    local ownerID = finiteOr(payload.ownerOnlineID, -1)
    local ownerName = payload.ownerUsername
    local matches = {}

    if ownerID >= 0 then
        for _, character in ipairs(players) do
            if characterOnlineID(character) == ownerID then
                table.insert(matches, character)
            end
        end
    elseif type(ownerName) == "string" and ownerName ~= "" then
        for _, character in ipairs(players) do
            if characterUsername(character) == ownerName then
                table.insert(matches, character)
            end
        end
    elseif #players == 1 then
        return players[1]
    end

    if #matches == 1 then return matches[1] end
    return nil
end

-- The client applies a number the server computed. It never derives a bonus,
-- a stage or a duration of its own.
function ClientRuntime.applyState(character, payload)
    if character == nil or type(payload) ~= "table" then return false end
    if type(payload.Strength) ~= "table" then return false end

    -- Cached before the carry seam, because a base another mod owns must not
    -- cost the player the skill-panel numbers as well.
    ClientRuntime.LastState[character] = payload
    ClientRuntime.onStateChanged(character, payload)

    if serverOwnsCarrySeam() then return true end

    local bonus = math.max(0,
        math.floor(finiteOr(payload.Strength.carryBonus, 0) + 0.5))

    local current = readBase(character)
    if current == nil then return false end

    local record = ClientRuntime.CarryBases[character]
    if record == nil then
        record = { original = current, applied = current }
        ClientRuntime.CarryBases[character] = record
    end
    if current ~= record.applied then return false end

    local target = record.original + PPO.ToneMath.carryBaseDelta(
        bonus, readWeightMod(character))
    if target == current then return true end
    if not writeBase(character, target) then return false end
    record.applied = target
    return true
end

-- The skill panel reads this and renders it. It never derives a number.
function ClientRuntime.state(character)
    if character == nil then return nil end
    return ClientRuntime.LastState[character]
end

function ClientRuntime.reset(character)
    if character == nil then return false end
    local record = ClientRuntime.CarryBases[character]
    local hadState = record ~= nil or ClientRuntime.LastState[character] ~= nil
    if record ~= nil and record.applied ~= record.original
            and readBase(character) == record.applied then
        writeBase(character, record.original)
    end
    ClientRuntime.CarryBases[character] = nil
    ClientRuntime.LastState[character] = nil
    return hadState
end

function ClientRuntime.onServerCommand(module, command, payload)
    if module ~= "PPO" or command ~= "state" then return false end
    local player = ClientRuntime.resolveCharacter(payload)
    if player == nil then return false end
    return ClientRuntime.applyState(player, payload)
end

if Events ~= nil and Events.OnServerCommand ~= nil then
    Events.OnServerCommand.Add(ClientRuntime.onServerCommand)
end

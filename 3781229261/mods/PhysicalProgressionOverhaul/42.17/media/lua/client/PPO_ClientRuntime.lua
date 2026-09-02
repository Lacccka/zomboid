require "PPO_Num"
require "PPO_CarrySeam"
require "PPO_Identity"

PPO = PPO or {}
PPO.ClientRuntime = PPO.ClientRuntime or {
    Loaded = true,
    CarryBases = {},
    LastState = {},
}

local ClientRuntime = PPO.ClientRuntime
ClientRuntime.onStateChanged = ClientRuntime.onStateChanged or function() end

local Num = PPO.Num
local Identity = PPO.Identity
local CarrySeam = PPO.CarrySeam

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
    count = ok and Num.finite(count, 0) or 0
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

function ClientRuntime.resolveCharacter(payload)
    if type(payload) ~= "table" then return nil end
    local players = activePlayers()
    local ownerID = Num.finite(payload.ownerOnlineID, -1)
    local ownerName = payload.ownerUsername
    local matches = {}

    if ownerID >= 0 then
        for _, character in ipairs(players) do
            if Identity.onlineID(character) == ownerID then
                table.insert(matches, character)
            end
        end
    elseif type(ownerName) == "string" and ownerName ~= "" then
        for _, character in ipairs(players) do
            if Identity.username(character) == ownerName then
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

    return CarrySeam.apply(ClientRuntime.CarryBases, character,
        payload.Strength.carryBonus)
end

-- The skill panel reads this and renders it. It never derives a number.
function ClientRuntime.state(character)
    if character == nil then return nil end
    return ClientRuntime.LastState[character]
end

function ClientRuntime.reset(character)
    if character == nil then return false end
    local hadState = ClientRuntime.LastState[character] ~= nil
    if CarrySeam.release(ClientRuntime.CarryBases, character) then
        hadState = true
    end
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

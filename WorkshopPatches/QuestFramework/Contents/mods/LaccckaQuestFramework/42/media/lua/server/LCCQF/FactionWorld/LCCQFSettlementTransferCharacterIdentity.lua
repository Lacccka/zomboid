-- Per-life identity snapshot for settlement transfer intents.
-- The transfer observer proves that an exact InventoryItem reached exact settlement stock;
-- this bridge separately proves that quest credit is still being applied to the same
-- persistent character life that owned the item before the vanilla engine transaction.
if isClient and isClient() and not (isServer and isServer()) then return {} end

require "LCCQF/LCCQFConstants"
require "LCCQF/Persistence/LCCQFCharacterIdentity"

LCCQF = LCCQF or {}

local C = LCCQF.Constants
local CharacterIdentity = LCCQF.CharacterIdentity
local Bridge = LCCQF.SettlementTransferCharacterIdentity or {}
local snapshots = Bridge.snapshots or {}

local function nowMs()
    return getTimestampMs and (tonumber(getTimestampMs()) or 0) or 0
end

local function log(message)
    print(C.LOG_PREFIX .. "[FACTION:SITE:TRANSFER:IDENTITY] " .. tostring(message))
end

local function finiteItemId(value)
    local number = tonumber(value)
    if number == nil or number ~= number or number == math.huge or number == -math.huge then return nil end
    number = math.floor(number)
    if number < 0 or number > 2147483647 then return nil end
    return number
end

local function username(player)
    if not player or not player.getUsername then return "" end
    local ok, value = pcall(function() return player:getUsername() end)
    return ok and value ~= nil and tostring(value) or ""
end

local function onlineId(player)
    if not player or not player.getOnlineID then return nil end
    local ok, value = pcall(function() return player:getOnlineID() end)
    return ok and value ~= nil and tonumber(value) or nil
end

local function snapshotKey(playerUsername, itemId)
    if type(playerUsername) ~= "string" or playerUsername == "" or itemId == nil then return nil end
    return playerUsername .. "|" .. tostring(itemId)
end

local function snapshotCount()
    local count = 0
    for _ in pairs(snapshots) do count = count + 1 end
    return count
end

local function pruneExpired(now)
    now = tonumber(now) or nowMs()
    local removed = 0
    for key, row in pairs(snapshots) do
        if type(row) ~= "table" or now >= (tonumber(row.expiresMs) or 0) then
            snapshots[key] = nil
            removed = removed + 1
        end
    end
    Bridge.snapshots = snapshots
    return removed
end

local function maximumSnapshots()
    local pendingPerPlayer = math.max(1, math.floor(tonumber(C.FACTION_SITE_TRANSFER_MAX_PENDING_PER_PLAYER) or 64))
    return math.max(128, pendingPerPlayer * 8)
end

local function captureIntent(player, args)
    if type(args) ~= "table" then return false end
    local itemId = finiteItemId(args.itemId)
    local playerUsername = username(player)
    local key = snapshotKey(playerUsername, itemId)
    if not key then return false end

    local characterId = CharacterIdentity.GetExistingCharacterId(player)
    if not characterId then
        characterId = CharacterIdentity.Resolve(player)
    end
    if type(characterId) ~= "string" or characterId == "" then return false end

    local now = nowMs()
    pruneExpired(now)
    if snapshots[key] == nil and snapshotCount() >= maximumSnapshots() then
        log("capture rejected player=" .. playerUsername .. " itemId=" .. tostring(itemId) .. " reason=capacity")
        return false
    end

    local ttl = math.max(1000, math.floor(tonumber(C.FACTION_SITE_TRANSFER_INTENT_TTL_MS) or 15000))
    local debounce = math.max(50, math.floor(tonumber(C.FACTION_SITE_TRANSFER_REFRESH_DEBOUNCE_MS) or 500))
    snapshots[key] = {
        characterId = characterId,
        playerUsername = playerUsername,
        playerOnlineId = onlineId(player),
        itemId = itemId,
        capturedMs = now,
        expiresMs = now + ttl + debounce + 5000,
    }
    Bridge.snapshots = snapshots
    return true
end

function Bridge.ConsumeConfirmedTransfer(event, player)
    if type(event) ~= "table" or not player then return nil, "invalid-event" end
    local itemId = finiteItemId(event.itemId)
    local eventUsername = tostring(event.playerUsername or "")
    local key = snapshotKey(eventUsername, itemId)
    if not key then return nil, "invalid-event-identity" end

    local row = snapshots[key]
    snapshots[key] = nil
    Bridge.snapshots = snapshots
    if type(row) ~= "table" then return nil, "identity-snapshot-missing" end
    if nowMs() >= (tonumber(row.expiresMs) or 0) then return nil, "identity-snapshot-expired" end
    if username(player) ~= row.playerUsername then return nil, "username-changed" end

    local currentCharacterId = CharacterIdentity.GetExistingCharacterId(player)
    if type(currentCharacterId) ~= "string" or currentCharacterId == "" then
        return nil, "current-character-unavailable"
    end
    if tostring(currentCharacterId) ~= tostring(row.characterId) then
        return nil, "character-life-changed"
    end
    return row.characterId, nil
end

function Bridge.GetSnapshotCount()
    pruneExpired(nowMs())
    return snapshotCount()
end

local function onClientCommand(module, command, player, args)
    if module ~= C.MODULE or command ~= C.COMMAND.REPORT_SETTLEMENT_TRANSFER_INTENT then return end
    captureIntent(player, args)
end

if isServer and isServer() then
    if Events.OnClientCommand then Events.OnClientCommand.Add(onClientCommand) end
    if Events.EveryOneMinute then Events.EveryOneMinute.Add(function() pruneExpired(nowMs()) end) end
end

Bridge.snapshots = snapshots
LCCQF.SettlementTransferCharacterIdentity = Bridge
return Bridge

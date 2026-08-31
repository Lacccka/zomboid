-- Runtime wiring from confirmed settlement transfers into the normal QuestService tick path.
-- No client report reaches this bridge directly; it consumes only events emitted after
-- server-side exact-item reconciliation and a successful stock refresh.
if isClient and isClient() and not (isServer and isServer()) then return {} end

require "LCCQF/LCCQFConstants"
require "LCCQF/Quest/LCCQFQuestService"
require "LCCQF/Quest/Objectives/LCCQFObjectiveSettlementSupply"
require "LCCQF/Quest/zz_LCCQFFactionSupplyQuestServiceExtension"
require "LCCQF/FactionWorld/LCCQFSettlementTransferObserver"
require "LCCQF/FactionWorld/LCCQFSettlementTransferCharacterIdentity"
require "LCCQF/FactionWorld/zz_LCCQFFactionSupplyQuestBridge"

LCCQF = LCCQF or {}

local C = LCCQF.Constants
local QuestService = LCCQF.QuestService
local Objective = LCCQF.QuestObjectives.SettlementSupply
local Observer = LCCQF.SettlementTransferServerObserver
local TransferIdentity = LCCQF.SettlementTransferCharacterIdentity
local SupplyBridge = LCCQF.FactionSupplyQuestBridge
local Runtime = LCCQF.FactionSupplyQuestRuntimeBridge or {}

local function log(message)
    print(C.LOG_PREFIX .. "[FACTION:SUPPLY:QUEST:RUNTIME] " .. tostring(message))
end

local function resolvePlayer(event)
    if type(event) ~= "table" or not getOnlinePlayers then return nil end
    local players = getOnlinePlayers()
    if not players then return nil end
    for index = 0, players:size() - 1 do
        local player = players:get(index)
        local onlineId = nil
        if player and player.getOnlineID then pcall(function() onlineId = tonumber(player:getOnlineID()) end) end
        if event.playerOnlineId ~= nil and onlineId ~= nil and tonumber(event.playerOnlineId) == onlineId then
            return player
        end
        local username = ""
        if player and player.getUsername then pcall(function() username = tostring(player:getUsername()) end) end
        if tostring(event.playerUsername or "") ~= "" and username == tostring(event.playerUsername) then
            return player
        end
    end
    return nil
end

local function onConfirmedTransfer(event)
    if type(event) ~= "table" or event.stockRefreshOk ~= true then return end
    local player = resolvePlayer(event)
    if not player then
        log("confirmed transfer has no online player itemId=" .. tostring(event.itemId))
        return
    end

    local characterId, identityError = TransferIdentity.ConsumeConfirmedTransfer(event, player)
    if not characterId then
        log("confirmed transfer rejected player=" .. tostring(event.playerUsername)
            .. " itemId=" .. tostring(event.itemId)
            .. " reason=" .. tostring(identityError or "character-identity-mismatch"))
        return
    end

    -- Operations were refreshed before the observer emitted this event. Update the
    -- persistent offer lifecycle before evaluating the objective so dialogue state and
    -- quest state see the same need revision.
    SupplyBridge.RunOnce()

    if not Objective.QueueConfirmedTransfer(player, event) then return end
    local completed = QuestService.UpdatePlayer(player)
    Objective.DiscardQueuedTransfers(player)

    log("applied player=" .. tostring(event.playerUsername)
        .. " characterId=" .. tostring(characterId)
        .. " siteId=" .. tostring(event.siteId)
        .. " itemId=" .. tostring(event.itemId)
        .. " fullType=" .. tostring(event.fullType)
        .. " completed=" .. tostring(completed))
end

function Runtime.Install()
    if Runtime.installed == true then return true end
    if not Observer or type(Observer.AddListener) ~= "function" then return false end
    if not TransferIdentity or type(TransferIdentity.ConsumeConfirmedTransfer) ~= "function" then return false end
    if not Observer.AddListener(onConfirmedTransfer) then return false end
    Runtime.installed = true
    log("confirmed transfer listener installed perLifeIdentity=true")
    return true
end

Runtime.Install()
LCCQF.FactionSupplyQuestRuntimeBridge = Runtime
return Runtime

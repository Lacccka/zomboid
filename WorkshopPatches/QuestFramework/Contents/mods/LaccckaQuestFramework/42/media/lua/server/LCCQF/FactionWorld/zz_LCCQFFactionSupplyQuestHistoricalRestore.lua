-- Restart-safe reconstruction of generated settlement-supply quest definitions.
-- Historical quest definitions must outlive the operational state of their settlement:
-- a saved character may still own an active/completed/failed instance after the site has
-- entered RELOCATING or ABANDONED. QuestPersistence can restore that instance only when
-- the generated definition is present in the common QuestRegistry first.
if isClient and isClient() and not (isServer and isServer()) then return {} end

require "LCCQF/LCCQFConstants"
require "LCCQF/FactionWorld/LCCQFFactionSiteRegistry"
require "LCCQF/FactionWorld/zz_LCCQFFactionSupplyQuestBridge"

LCCQF = LCCQF or {}

local C = LCCQF.Constants
local Sites = LCCQF.FactionSiteRegistry
local SupplyBridge = LCCQF.FactionSupplyQuestBridge
local Restore = LCCQF.FactionSupplyQuestHistoricalRestore or {}

local function log(message)
    print(C.LOG_PREFIX .. "[FACTION:SUPPLY:QUEST:RESTORE] " .. tostring(message))
end

function Restore.RunOnce()
    local registered, rejected, visited = 0, 0, 0
    for _, site in ipairs(Sites.ListSites()) do
        visited = visited + 1
        local offers = site.operations and site.operations.questOffers or nil
        if type(offers) == "table" then
            for _, offer in pairs(offers) do
                if type(offer) == "table" and type(offer.questId) == "string" and offer.questId ~= "" then
                    local ok, result = SupplyBridge.RegisterDefinition(offer)
                    if ok then
                        registered = registered + 1
                    else
                        rejected = rejected + 1
                        log("definition rejected siteId=" .. tostring(site.siteId)
                            .. " state=" .. tostring(site.state)
                            .. " questId=" .. tostring(offer.questId)
                            .. " error=" .. tostring(result))
                    end
                end
            end
        end
    end

    log("historical definitions restored sites=" .. tostring(visited)
        .. " registered=" .. tostring(registered)
        .. " rejected=" .. tostring(rejected))
    return rejected == 0, registered, rejected
end

local function onServerStarted()
    Restore.RunOnce()
end

if isServer and isServer() and Events.OnServerStarted then
    Events.OnServerStarted.Add(onServerStarted)
end

LCCQF.FactionSupplyQuestHistoricalRestore = Restore
return Restore

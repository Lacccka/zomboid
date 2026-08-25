ExtractionMode = ExtractionMode or {}
if isClient and isClient() then
    ExtractionMode.RaidQuestAuthority = ExtractionMode.RaidQuestAuthority or {}
    return ExtractionMode.RaidQuestAuthority
end

require "ExtractionMode/Config"
require "ExtractionMode/Util"
require "ExtractionMode/Groups"
require "ExtractionMode/Quests"

local Config = ExtractionMode.Config
local Util = ExtractionMode.Util
local Groups = ExtractionMode.Groups
local Quests = ExtractionMode.Quests
local Runtime = ExtractionMode.RaidRuntime
local QuestAuthority = {}
local lastQuestVisitCheck = -1

local function activePlayers()
    local result = {}
    for _, player in ipairs(Util.players()) do
        if player and not player:isDead() and Util.username(player) ~= "" then
            result[#result + 1] = player
        end
    end
    return result
end

function QuestAuthority.announceQuestGroup(owner, message, messageKey, messageArgs)
    local data = Runtime.currentStore()
    for _, player in ipairs(Util.players()) do
        if Groups.same(owner, Groups.forPlayer(player, data.groupRegistry)) then
            Runtime.deliver(player, "Announcement", {
                message = tostring(message),
                messageKey = messageKey,
                messageArgs = messageArgs,
                audioCue = "quest_completed",
            })
        end
    end
end

local function prepareQuestRewardItem(item, reward)
    if item == nil or reward == nil then return end
    if reward.fillPetrol == true then
        pcall(function()
            local container = item:getFluidContainer()
            if container then container:addFluid(Fluid.Petrol, container:getCapacity()) end
        end)
    end
    local container = nil
    if reward.randomContents or reward.fixedContents then
        pcall(function() container = item:getInventory() end)
        if container == nil then pcall(function() container = item:getItemContainer() end) end
    end
    if container == nil then
        if reward.randomContents or reward.fixedContents then
            Util.log("Quest reward " .. tostring(item:getFullType()) .. " cannot hold configured contents")
        end
        return
    end
    local contents = reward.randomContents
    local types = contents and contents.types or {}
    local amount = math.max(0, math.floor(tonumber(contents and contents.amount) or 0))
    if amount > 0 and #types > 0 then
        for _ = 1, amount do
            local fullType = tostring(types[ZombRand(#types) + 1] or "")
            local added = nil
            if fullType ~= "" then pcall(function() added = container:AddItem(fullType) end) end
            if added == nil then
                Util.log("Failed to add " .. fullType .. " to quest reward " .. tostring(item:getFullType()))
            end
        end
    end
    for _, entry in ipairs(reward.fixedContents or {}) do
        local fullType = tostring(entry.fullType or "")
        local fixedAmount = math.max(0, math.floor(tonumber(entry.amount) or 0))
        for _ = 1, fixedAmount do
            local added = nil
            if fullType ~= "" then pcall(function() added = container:AddItem(fullType) end) end
            if added == nil then
                Util.log("Failed to add " .. fullType .. " to quest reward " .. tostring(item:getFullType()))
            end
        end
    end
end

local function giveQuestRewardItem(player, fullType, reward)
    if player == nil or fullType == nil or fullType == "" then return false, false end
    local inventory = player:getInventory()
    local item = nil
    if inventory ~= nil then
        pcall(function() item = inventory:AddItem(fullType) end)
        if item ~= nil then
            prepareQuestRewardItem(item, reward)
            if sendAddItemToContainer then sendAddItemToContainer(inventory, item) end
            return true, false
        end
    end
    local square = player:getSquare()
    if square ~= nil then
        pcall(function() item = square:AddWorldInventoryItem(fullType, 0.5, 0.5, 0) end)
        if item ~= nil then
            prepareQuestRewardItem(item, reward)
            pcall(function() item:SynchSpawn() end)
            return true, true
        end
    end
    return false, false
end

function QuestAuthority.prepareBarterRewardItems(definition)
    local prepared = {}
    for _, reward in ipairs(definition and definition.received or {}) do
        local amount = math.max(0, math.floor(tonumber(reward.amount) or 0))
        for _ = 1, amount do
            local fullType = tostring(reward.fullType or "")
            local randomTypes = reward.randomTypes or {}
            if #randomTypes > 0 then
                fullType = tostring(randomTypes[ZombRand(#randomTypes) + 1] or "")
            end
            local item = nil
            pcall(function() item = instanceItem(fullType) end)
            if item == nil then return nil, fullType end
            prepared[#prepared + 1] = item
        end
    end
    return prepared, nil
end

function QuestAuthority.givePreparedBarterItem(player, item)
    if player == nil or item == nil then return false, false end
    local inventory = player:getInventory()
    local added = nil
    if inventory then
        pcall(function() added = inventory:AddItem(item) end)
        if added then
            if sendAddItemToContainer then sendAddItemToContainer(inventory, item) end
            return true, false
        end
    end
    local square = player:getSquare()
    local worldItem = nil
    if square then
        pcall(function() worldItem = square:AddWorldInventoryItem(item, 0.5, 0.5, 0, false) end)
        if worldItem then
            pcall(function()
                local isoWorldItem = worldItem:getWorldItem()
                if isoWorldItem then isoWorldItem:transmitCompleteItemToClients() end
            end)
            return true, true
        end
    end
    return false, false
end

function QuestAuthority.grantQuestItemRewards(data, owner, definition)
    if data == nil or owner == nil or definition == nil then return end
    local itemRewards = {}
    for _, reward in ipairs(definition.rewards or {}) do
        if reward.type == "item" then itemRewards[#itemRewards + 1] = reward end
    end
    if #itemRewards == 0 then return end
    for _, recipient in ipairs(activePlayers()) do
        if Groups.same(owner, Groups.forPlayer(recipient, data.groupRegistry)) then
            local granted = 0
            local dropped = 0
            local failed = 0
            for _, reward in ipairs(itemRewards) do
                local amount = math.max(0, math.floor(tonumber(reward.amount) or 0))
                for _ = 1, amount do
                    local ok, wasDropped = giveQuestRewardItem(recipient,
                        tostring(reward.fullType or ""), reward)
                    if ok then
                        granted = granted + 1
                        if wasDropped then dropped = dropped + 1 end
                    else
                        failed = failed + 1
                    end
                end
            end
            if dropped > 0 then
                Runtime.deliver(recipient, "Announcement", {
                    message = tostring(dropped) .. " quest reward item(s) would not fit and were dropped at your feet.",
                    messageKey = "IGUI_ExtractionMode_Message_QuestRewardsDropped",
                    messageArgs = { tostring(dropped) },
                })
            end
            if failed > 0 then
                Runtime.deliver(recipient, "Error", {
                    message = tostring(failed) .. " quest reward item(s) could not be created. Check the server log.",
                    messageKey = "IGUI_ExtractionMode_Error_QuestRewardsFailed",
                    messageArgs = { tostring(failed) },
                })
                Util.log("Failed to grant " .. tostring(failed) .. " reward item(s) for quest "
                    .. tostring(definition.id) .. " to " .. Util.username(recipient))
            elseif granted > 0 then
                Runtime.deliver(recipient, "Announcement", {
                    message = "Received " .. tostring(granted) .. " item reward(s) for " .. definition.name .. ".",
                    messageKey = "IGUI_ExtractionMode_Message_QuestRewardsReceived",
                    messageArgs = { tostring(granted), { key = definition.nameKey, fallback = definition.name } },
                })
            end
        end
    end
end

local function ensureRaidVisitObjectivesReset(data, player)
    local completions, objectives, _, owner = Runtime.questStateFor(data, player)
    local raidId = math.max(0, math.floor(tonumber(data.raidId) or 0))
    if tonumber(data.questVisitRaidIds[owner.key]) == raidId then
        return false, completions, objectives, owner
    end
    local changed = Quests.resetRaidVisitObjectives(objectives, completions)
    data.questVisitRaidIds[owner.key] = raidId
    return changed, completions, objectives, owner
end

function QuestAuthority.resetRaidVisitObjectivesForParticipants(data)
    for _, player in ipairs(activePlayers()) do
        if data.participants[Util.username(player)] == true then
            ensureRaidVisitObjectivesReset(data, player)
        end
    end
end

function QuestAuthority.definitionHasRaidVisitObjective(definition)
    for _, objective in ipairs((definition and definition.objectives) or {}) do
        if objective.type == "raid_visit" then return true end
    end
    return false
end

function QuestAuthority.raidVisitProgressIsSecured(data, owner)
    if data == nil or owner == nil then return false end
    local visitRaidId = tonumber(data.questVisitRaidIds[owner.key])
    return visitRaidId ~= nil
        and tonumber(data.questVisitSuccessfulRaidIds[owner.key]) == visitRaidId
end

function QuestAuthority.markSuccessfulRaidVisitExtraction(data, player)
    local _, _, _, owner = Runtime.questStateFor(data, player)
    local raidId = math.max(0, math.floor(tonumber(data.raidId) or 0))
    if tonumber(data.questVisitRaidIds[owner.key]) ~= raidId then return false end
    data.questVisitSuccessfulRaidIds[owner.key] = raidId
    return true
end

function QuestAuthority.resetUnsecuredRaidVisitProgress(data)
    local raidId = math.max(0, math.floor(tonumber(data.raidId) or 0))
    local resetOwners = 0
    for ownerKey, visitRaidId in pairs(data.questVisitRaidIds or {}) do
        if tonumber(visitRaidId) == raidId
            and tonumber(data.questVisitSuccessfulRaidIds[ownerKey]) ~= raidId
            and Quests.resetRaidVisitObjectives(data.questObjectiveProgress[ownerKey],
                data.questProgress[ownerKey]) then
            resetOwners = resetOwners + 1
        end
    end
    if resetOwners > 0 then
        Util.log("Reset unsecured raid-visit progress for " .. tostring(resetOwners)
            .. " quest owner(s) after failed raid " .. tostring(raidId))
    end
    return resetOwners
end

function QuestAuthority.processQuestVisitObjectives(data, nowSecond)
    local raidActive = data.state == Config.STATE_RAID or data.state == Config.STATE_EXTRACTING
        or data.state == Config.STATE_BOARDING
    if not raidActive or nowSecond == lastQuestVisitCheck then return end
    lastQuestVisitCheck = nowSecond
    local changedOwners = {}
    for _, player in ipairs(activePlayers()) do
        local username = Util.username(player)
        if data.participants[username] == true and not player:isDead() then
            local resetChanged, completions, objectives, owner = ensureRaidVisitObjectivesReset(data, player)
            local changed = resetChanged
            local locationChecked = false
            for _, definition in ipairs(Quests.definitions()) do
                if Quests.isAcquired(completions, definition)
                    and not Quests.isCompleted(completions, definition.id) then
                    for _, objective in ipairs(definition.objectives or {}) do
                        local radius = math.max(1, tonumber(objective.radius) or 10)
                        local destinationMatches = objective.townKey == nil
                            or tostring(objective.townKey) == tostring(data.selectedTownKey)
                        if objective.type == "raid_visit" and destinationMatches
                            and Quests.objectiveCount(objectives, definition, objective)
                                < math.max(1, math.floor(tonumber(objective.amount) or 1))
                            and Util.distanceSquaredXY({ x = player:getX(), y = player:getY() }, objective)
                                <= radius * radius then
                            local progress = Quests.incrementObjective(objectives, definition, objective, 1)
                            changed = true
                            locationChecked = true
                            Util.log("Raid visit checked: player=" .. tostring(username)
                                .. " owner=" .. tostring(owner.key)
                                .. " raid=" .. tostring(data.raidId)
                                .. " town=" .. tostring(data.selectedTownKey)
                                .. " quest=" .. tostring(definition.id)
                                .. " objective=" .. tostring(objective.id)
                                .. " progress=" .. tostring(progress) .. "/"
                                .. tostring(math.max(1, math.floor(tonumber(objective.amount) or 1)))
                                .. " playerPosition=" .. tostring(math.floor(player:getX())) .. ","
                                .. tostring(math.floor(player:getY())) .. ","
                                .. tostring(math.floor(player:getZ()))
                                .. " target=" .. tostring(objective.x) .. ","
                                .. tostring(objective.y) .. "," .. tostring(objective.z or "any")
                                .. " radius=" .. tostring(radius))
                        end
                    end
                end
            end
            if locationChecked then
                Runtime.deliverLocalized(player, "LocationChecked",
                    "IGUI_ExtractionMode_Message_LocationChecked", "Location Checked")
            end
            if changed then changedOwners[owner.key] = owner end
        end
    end
    for _, owner in pairs(changedOwners) do Runtime.sendStateToQuestGroup(owner) end
end

ExtractionMode.RaidQuestAuthority = QuestAuthority
return QuestAuthority

ExtractionMode = ExtractionMode or {}
if isClient and isClient() then
    ExtractionMode.RaidLossAuthority = ExtractionMode.RaidLossAuthority or {}
    return ExtractionMode.RaidLossAuthority
end

require "ExtractionMode/Config"
require "ExtractionMode/Util"
require "ExtractionMode/RaidFlareAuthority"

local Config = ExtractionMode.Config
local Util = ExtractionMode.Util
local RaidFlares = ExtractionMode.RaidFlareAuthority
local Runtime = ExtractionMode.RaidRuntime
local Loss = {}
local ESSENTIAL_CLOTHING_LOCATIONS = {
    underwear = true,
    underwearbottom = true,
    underweartop = true,
    socks = true,
    shoes = true,
    shirt = true,
    shortsleeveshirt = true,
    tshirt = true,
    tanktop = true,
    sweater = true,
    jersey = true,
    fulltop = true,
    pants = true,
    pantsskinny = true,
    shortpants = true,
    shortsshort = true,
    skirt = true,
    longskirt = true,
    legs1 = true,
    dress = true,
    longdress = true,
}

local function activePlayers()
    local result = {}
    for _, player in ipairs(Util.players()) do
        if player and not player:isDead() and Util.username(player) ~= "" then
            result[#result + 1] = player
        end
    end
    return result
end

local function singleplayerAuthority()
    return not (isServer and isServer()) and not (isClient and isClient())
end

local function normalizedBodyLocation(value)
    local location = string.lower(tostring(value or ""))
    location = string.match(location, "([^:]+)$") or location
    return string.gsub(location, "[^%w]", "")
end

local function protectedReconnectClothing(player)
    local protected = {}
    local wornItems = player and player:getWornItems()
    if wornItems == nil then return protected end
    for index = 0, wornItems:size() - 1 do
        local worn = wornItems:get(index)
        local item = worn and worn:getItem()
        local location = worn and worn:getLocation()
        if item and ESSENTIAL_CLOTHING_LOCATIONS[normalizedBodyLocation(location)] == true then
            protected[item] = true
        end
    end
    return protected
end

local function collectReconnectLossCandidates(container, protected, output)
    if container == nil then return end
    local items = container:getItems()
    if items == nil then return end
    for index = 0, items:size() - 1 do
        local item = items:get(index)
        if item then
            if protected[item] ~= true then output[#output + 1] = item end
            if item:IsInventoryContainer() then
                collectReconnectLossCandidates(item:getInventory(), protected, output)
            end
        end
    end
end

local function shuffleItems(items)
    for index = #items, 2, -1 do
        local other = ZombRand(index) + 1
        items[index], items[other] = items[other], items[index]
    end
end

local function dropExistingItem(player, item, square)
    if player == nil or item == nil or square == nil then return false end
    local container = item:getContainer()
    if container == nil then return false end
    pcall(function() player:removeWornItem(item) end)
    pcall(function() player:removeAttachedItem(item) end)
    pcall(function() player:removeFromHands(item) end)
    local worldItem = nil
    local added = pcall(function()
        worldItem = square:AddWorldInventoryItem(item,
            0.2 + ZombRand(60) / 100, 0.2 + ZombRand(60) / 100, 0, false)
    end)
    if not added or worldItem == nil then return false end
    local removed = pcall(function() container:Remove(item) end)
    if not removed then
        pcall(function() square:transmitRemoveItemFromSquare(worldItem:getWorldItem()) end)
        return false
    end
    if sendRemoveItemFromContainer then
        pcall(function() sendRemoveItemFromContainer(container, item) end)
    end
    pcall(function()
        local isoWorldItem = worldItem:getWorldItem()
        if isoWorldItem then isoWorldItem:transmitCompleteItemToClients() end
    end)
    if sendEquip then pcall(function() sendEquip(player) end) end
    return true
end

local function applyDisconnectedRaidLoss(player)
    if player == nil or player:isDead() then return false end
    local inventory = player:getInventory()
    local square = player:getCurrentSquare()
    if inventory == nil or square == nil then return false end
    local candidates = {}
    collectReconnectLossCandidates(inventory, protectedReconnectClothing(player), candidates)
    if #candidates == 0 then
        Runtime.deliver(player, "Announcement", {
            message = "You were left behind, but had no eligible equipment to lose.",
            messageKey = "IGUI_ExtractionMode_Message_LeftBehindNoLoss",
        })
        return true
    end
    shuffleItems(candidates)
    local percent = 20 + ZombRand(31)
    local selectedCount = math.max(1, math.ceil(#candidates * percent / 100))
    local selectedBag = nil
    for index = 1, selectedCount do
        if candidates[index]:IsInventoryContainer() then
            selectedBag = candidates[index]
            break
        end
    end
    local dropped = 0
    if selectedBag then
        if dropExistingItem(player, selectedBag, square) then dropped = 1 end
    else
        for index = 1, selectedCount do
            if dropExistingItem(player, candidates[index], square) then dropped = dropped + 1 end
        end
    end
    if dropped == 0 then
        Runtime.deliver(player, "Announcement", {
            message = "You were left behind, but the selected equipment could not be dropped.",
            messageKey = "IGUI_ExtractionMode_Message_LeftBehindDropFailed",
        })
        Util.log("Disconnected raid loss selected equipment for "
            .. tostring(Util.username(player)) .. " but no item accepted a world drop")
        return true
    end
    if selectedBag then
        Runtime.deliver(player, "Announcement", {
            message = "You were left behind when the raid ended. " .. tostring(selectedBag:getDisplayName())
                .. " and everything inside it were dropped at your last location.",
            messageKey = "IGUI_ExtractionMode_Message_LeftBehindBag",
            messageArgs = { { key = selectedBag:getFullType(),
                fallback = tostring(selectedBag:getDisplayName()) } },
        })
    else
        Runtime.deliver(player, "Announcement", {
            message = "You were left behind when the raid ended. " .. tostring(dropped)
                .. " item(s) were dropped at your last location (" .. tostring(percent) .. "% loss roll).",
            messageKey = "IGUI_ExtractionMode_Message_LeftBehindItems",
            messageArgs = { tostring(dropped), tostring(percent) },
        })
    end
    Util.log("Applied disconnected raid loss to " .. tostring(Util.username(player))
        .. ": roll=" .. tostring(percent) .. ", dropped=" .. tostring(dropped)
        .. ", bagOnly=" .. tostring(selectedBag ~= nil))
    return true
end

function Loss.dropAllDeathEquipment(player)
    local inventory = player and player:getInventory()
    local square = player and player:getCurrentSquare()
    local items = inventory and inventory:getItems()
    if items == nil or square == nil then return 0, 100 end
    local topLevel = {}
    for index = 0, items:size() - 1 do topLevel[#topLevel + 1] = items:get(index) end
    local dropped = 0
    for _, item in ipairs(topLevel) do
        if dropExistingItem(player, item, square) then dropped = dropped + 1 end
    end
    return dropped, 100
end

function Loss.dropPartialDeathEquipment(player)
    local inventory = player and player:getInventory()
    local square = player and player:getCurrentSquare()
    local percent = 50
    if inventory == nil or square == nil then return 0, percent end
    local heldItems = {}
    local primary = player:getPrimaryHandItem()
    local secondary = player:getSecondaryHandItem()
    if primary then heldItems[#heldItems + 1] = primary end
    if secondary and secondary ~= primary then heldItems[#heldItems + 1] = secondary end
    local dropped = 0
    local heldDropFailures = 0
    for _, item in ipairs(heldItems) do
        if dropExistingItem(player, item, square) then
            dropped = dropped + 1
        else
            heldDropFailures = heldDropFailures + 1
        end
    end
    if heldDropFailures > 0 then
        Util.log("Raid death rescue could not world-drop " .. tostring(heldDropFailures)
            .. " held item(s) for " .. tostring(Util.username(player)))
    end
    local candidates = {}
    collectReconnectLossCandidates(inventory, protectedReconnectClothing(player), candidates)
    if #candidates == 0 then return dropped, percent end
    shuffleItems(candidates)
    local selectedCount = math.max(1, math.ceil(#candidates * percent / 100))
    local selectedBag = nil
    for index = 1, selectedCount do
        if candidates[index]:IsInventoryContainer() then
            selectedBag = candidates[index]
            break
        end
    end
    if selectedBag then
        if dropExistingItem(player, selectedBag, square) then dropped = dropped + 1 end
        return dropped, percent
    end
    for index = 1, selectedCount do
        if dropExistingItem(player, candidates[index], square) then dropped = dropped + 1 end
    end
    return dropped, percent
end

local function randomActiveRaidPlayer(data, excludedUsername)
    local candidates = {}
    for _, player in ipairs(activePlayers()) do
        local username = Util.username(player)
        if username ~= excludedUsername and data.participants[username] == true then
            candidates[#candidates + 1] = player
        end
    end
    if #candidates == 0 then return nil end
    return candidates[ZombRand(#candidates) + 1]
end

function Loss.handleDisconnectedRaidReconnect(data, player)
    if singleplayerAuthority() or player == nil then return nil end
    local username = Util.username(player)
    local disconnected = data.disconnectedRaidPlayers[username]
    if disconnected == nil then return nil end
    if player:isDead() then
        data.disconnectedRaidPlayers[username] = nil
        data.returnPending[username] = true
        return "WAITING"
    end
    if data.state == Config.STATE_TRANSIT then return "WAITING" end
    local activeRaid = data.state == Config.STATE_RAID
        or data.state == Config.STATE_EXTRACTING or data.state == Config.STATE_BOARDING
    if activeRaid then
        local target = randomActiveRaidPlayer(data, username)
        local destination = target and {
            x = target:getX(), y = target:getY(), z = target:getZ(),
        } or {
            x = tonumber(disconnected.x), y = tonumber(disconnected.y),
            z = tonumber(disconnected.z) or 0,
        }
        if destination.x == nil or destination.y == nil
            or not Config.pointInsideRaidBounds(data.selectedTownKey, destination) then
            destination = data.raidSpawn
        end
        if destination == nil or destination.x == nil or destination.y == nil
            or not Config.pointInsideRaidBounds(data.selectedTownKey, destination) then
            return "WAITING"
        end
        data.disconnectedRaidPlayers[username] = nil
        data.participants[username] = true
        data.extractedPlayers[username] = nil
        data.returnPending[username] = nil
        data.boardingPending[username] = nil
        data.groundExtractionPending[username] = nil
        data.deathRescuePending[username] = nil
        data.flareUpgradePending[username] = nil
        RaidFlares.giveFlare(player)
        Runtime.teleport(player, destination, target and (ZombRand(8) + 2) or 0)
        Runtime.deliver(player, "Announcement", {
            message = target and ("Rejoined the active raid near "
                .. tostring(Util.username(target)) .. ".") or "Rejoined the active raid.",
            messageKey = target and "IGUI_ExtractionMode_Message_RejoinedRaid" or nil,
            messageArgs = target and { tostring(Util.username(target)) } or nil,
            audioCue = data.state == Config.STATE_EXTRACTING and "extraction_music" or nil,
        })
        Util.log("Restored disconnected raid participant " .. tostring(username)
            .. " from raid " .. tostring(disconnected.raidId) .. " to active raid "
            .. tostring(data.raidId) .. (target and (" near "
                .. tostring(Util.username(target))) or " at the saved disconnect position"))
        Runtime.broadcastState()
        return "REJOINED"
    end
    if not applyDisconnectedRaidLoss(player) then return "WAITING" end
    data.disconnectedRaidPlayers[username] = nil
    data.returnPending[username] = nil
    data.boardingPending[username] = nil
    data.groundExtractionPending[username] = nil
    data.deathRescuePending[username] = nil
    data.flareUpgradePending[username] = nil
    Runtime.getRootStore().playerRaidKeys[username] = nil
    Runtime.teleport(player, Config.hideout(), 1)
    return "PENALIZED"
end

ExtractionMode.RaidLossAuthority = Loss
return Loss

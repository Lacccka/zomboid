MissionsEvents = MissionsEvents or {}
MissionsEvents.M2 = MissionsEvents.M2 or {}

local M2 = MissionsEvents.M2

-- =========================
-- INVENTORY CACHE
-- =========================
M2.InventoryCache = {}
M2.InventoryCacheLastUpdate = {}

local CACHE_INTERVAL = 1

local function getPlayerKey(player)
    return player:getUsername()
end

-- =========================
-- BUILD CACHE
-- =========================
function M2.buildInventoryCache(player)
    if not player then return end

    local inv = player:getInventory()
    if not inv then return end

    local cache = {}
    local items = inv:getItems()

    for i = 0, items:size() - 1 do
        local it = items:get(i)
        if it and it.getFullType then
            local ft = it:getFullType()
            cache[ft] = (cache[ft] or 0) + 1
        end
    end

    local key = getPlayerKey(player)
    M2.InventoryCache[key] = cache
    M2.InventoryCacheLastUpdate[key] = getTimestampMs()
end

-- =========================
-- GET CACHE
-- =========================
local function getInventoryCache(player)
    local key = getPlayerKey(player)

    local cache = M2.InventoryCache[key]
    local last = M2.InventoryCacheLastUpdate[key] or 0

    if not cache or (getTimestampMs() - last) > (CACHE_INTERVAL * 1000) then
        M2.buildInventoryCache(player)
        cache = M2.InventoryCache[key]
    end

    return cache or {}
end

-- =========================
-- COUNT PROGRESS
-- =========================
function M2.getProgress(player, requiredItems)

    if not player or not requiredItems then
        return 0, 0, false
    end

    local cache = getInventoryCache(player)

    local total = 0
    local have = 0

    for item, requiredAmount in pairs(requiredItems) do
        total = total + requiredAmount

        local playerAmount = cache[item] or 0

        if playerAmount >= requiredAmount then
            have = have + requiredAmount
        else
            have = have + playerAmount
        end
    end

    local completed = (have >= total)

    return have, total, completed
end

function M2.hasAllItems(player, requiredItems)
    local _, _, completed = M2.getProgress(player, requiredItems)
    return completed
end

function M2.start()

    sendClientCommand("MissionsEvents", "StartM2", {})
end

function M2.confirm()

    sendClientCommand("MissionsEvents", "ConfirmM2", {})
end

-- =========================
-- FROM SERVER
-- =========================
local function onServerCommand(module, command, args)

    if module ~= "MissionsEvents" then return end

    local player = getPlayer()
    if not player then return end

    -- =========================
    -- SYNC M2 DATA
    -- =========================
    if command == "SyncM2" then

        if not MissionsEvents.M2 then return end

        MissionsEvents.M2.updateFromServer(player, args)

    end

    -- =========================
    -- REMOVE ITEM
    -- =========================
    if command == "RemoveItem" then

        local inv = player:getInventory()
        if not inv then return end

        local items = inv:getItems()

        for i = items:size()-1, 0, -1 do
            local it = items:get(i)

            if it and args and it:getID() == args.id then
                inv:Remove(it)

                break
            end
        end

        inv:setDrawDirty(true)
        player:resetEquippedHandsModels()
    end
	
	-- =========================
	-- HALO MESSAGE
	-- =========================
	if command == "HaloMessage" then
	
		local player = getPlayer()
		if not player then return end
	
		if args and args.text then
			HaloTextHelper.addText(player, getText(args.text))
		end
	end	
	
end

Events.OnServerCommand.Add(onServerCommand)

-- =========================
-- AUTO CACHE UPDATE
-- =========================
Events.OnPlayerUpdate.Add(function(player)
    local key = getPlayerKey(player)
    if not M2.InventoryCache[key] then
        M2.buildInventoryCache(player)
    end
end)
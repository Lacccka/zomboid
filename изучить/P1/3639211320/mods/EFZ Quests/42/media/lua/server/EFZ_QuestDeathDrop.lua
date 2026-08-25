if isClient() then
    return
end

if not EFZ then
    EFZ = {}
end

EFZ.QuestDeathDrop = EFZ.QuestDeathDrop or {}
local Drop = EFZ.QuestDeathDrop

local LOG_PREFIX = "[EFZ][QuestDeathDrop] "
local SYNC_MODULE = "EFZ"
local SYNC_COMMAND = "QuestDeathDropSync"
local ZOMBIE_SWEEP_INTERVAL_MS = 1000

-- Items below are used as quest objectives or quest progression currency.
local QUEST_ITEM_TYPES = {
    ["EFZ.ArmyCrateMQ03"] = true,
    ["EFZ.RoadControlPlan"] = true,
    ["EFZ.OperationCleanSlatePlan"] = true,
    ["EFZ.NBCEquipmentCrate"] = true,
    ["EFZ.DetaineeVisitationVoiceLog"] = true,
    ["EFZ.PatientFile"] = true,
    ["EFZ.DeltaFacilityDocument"] = true,
    ["EFZ.LastLetterToDoctor"] = true,
    ["EFZ.RareAlcohol"] = true,
    ["EFZ.PrototypeWeaponBlueprint"] = true,
    ["EFZ.OverrideCommand"] = true,
    ["EFZ.InfectionSuppressorBox"] = true,
    ["EFZ.LivingSpaceFloorPlanUpper"] = true,
    ["EFZ.LivingSpaceFloorPlanLower"] = true,
    ["EFZ.SupplyTicket"] = true,
    ["EFZ.MassSupplyTicket"] = true,
    ["EFZ.LittleSupplyTicket"] = true,
}

local function isQuestItem(item)
    if not item or not item.getFullType then
        return false
    end
    if item.getModule and item:getModule() ~= "EFZ" then
        return false
    end
    return QUEST_ITEM_TYPES[item:getFullType()] == true
end

local function isExtremeModeEnabled()
    return SandboxVars and SandboxVars.EFZQuests and SandboxVars.EFZQuests.ExtremeMode == true
end

local function isIsoPlayer(obj)
    return obj ~= nil and instanceof(obj, "IsoPlayer")
end

local function disablePlayerReanimation(playerObj)
    if playerObj.setReanim then
        playerObj:setReanim(false)
    end
    if playerObj.setReanimateTimer then
        playerObj:setReanimateTimer(-1)
    end
    if playerObj.setReanimAnimDelay then
        playerObj:setReanimAnimDelay(0)
    end
    if playerObj.setReanimAnimFrame then
        playerObj:setReanimAnimFrame(0)
    end
    print(LOG_PREFIX .. "ExtremeMode: blocked player reanimation.")
end

local function syncClients(action, x, y, z)
    if not sendServerCommand then
        return
    end

    local payload = {
        action = action,
        x = math.floor(tonumber(x) or 0),
        y = math.floor(tonumber(y) or 0),
        z = math.floor(tonumber(z) or 0),
    }
    sendServerCommand(SYNC_MODULE, SYNC_COMMAND, payload)
end

local function getInnerContainer(item)
    if item.getInventory then
        local inv = item:getInventory()
        if inv and inv.getItems then
            return inv
        end
    end

    if item.getItemContainer then
        local inv = item:getItemContainer()
        if inv and inv.getItems then
            return inv
        end
    end

    return nil
end

local function collectQuestItems(container, out, visited)
    if visited[container] then
        return
    end
    visited[container] = true

    local items = container:getItems()
    for i = 0, items:size() - 1 do
        local item = items:get(i)
        if isQuestItem(item) then
            out[#out + 1] = item
        end

        if item then
            local inner = getInnerContainer(item)
            if inner then
                collectQuestItems(inner, out, visited)
            end
        end
    end
end

local function unequipItem(playerObj, item)
    playerObj:removeAttachedItem(item)
    item:setAttachedSlot(-1)
    item:setAttachedSlotType(nil)
    item:setAttachedToModel(nil)

    if item == playerObj:getPrimaryHandItem() then
        if (item:isTwoHandWeapon() or item:isRequiresEquippedBothHands()) and item == playerObj:getSecondaryHandItem() then
            playerObj:setSecondaryHandItem(nil)
        end
        playerObj:setPrimaryHandItem(nil)
    end

    if item == playerObj:getSecondaryHandItem() then
        if (item:isTwoHandWeapon() or item:isRequiresEquippedBothHands()) and item == playerObj:getPrimaryHandItem() then
            playerObj:setPrimaryHandItem(nil)
        end
        playerObj:setSecondaryHandItem(nil)
    end
end

local function dropQuestItemsOnDeath(playerObj)
    local square = playerObj:getSquare()
    if not square then
        print(LOG_PREFIX .. "Death square is nil.")
        return
    end

    local questItems = {}
    collectQuestItems(playerObj:getInventory(), questItems, {})

    for i = #questItems, 1, -1 do
        local item = questItems[i]
        local container = item and item:getContainer() or nil
        if item and container then
            local fullType = item:getFullType()
            unequipItem(playerObj, item)

            local dropItem = square:AddWorldInventoryItem(item, 0, 0, 0)
            local worldItem = dropItem and dropItem:getWorldItem() or nil
            if worldItem then
                worldItem:setIgnoreRemoveSandbox(true)
            end

            container:Remove(item)
            if sendRemoveItemFromContainer then
                sendRemoveItemFromContainer(container, item)
            end
            print(LOG_PREFIX .. "Dropped quest item: " .. tostring(fullType))
        end
    end

    if #questItems > 0 then
        print(LOG_PREFIX .. "Dropped " .. tostring(#questItems) .. " quest items on death.")
    end
end

local function onCharacterDeath(character)
    if not isIsoPlayer(character) then
        return
    end
    if isExtremeModeEnabled() then
        disablePlayerReanimation(character)
    end
    dropQuestItemsOnDeath(character)
end

local function onDeadBodySpawn(body)
    if not isExtremeModeEnabled() then
        return
    end
    if not body or not body:isPlayer() then
        return
    end

    local square = body:getSquare()
    if not square then
        print(LOG_PREFIX .. "ExtremeMode: player corpse has no square.")
        return
    end

    local x, y, z = square:getX(), square:getY(), square:getZ()
    if square.removeCorpse then
        square:removeCorpse(body, false)
        syncClients("RemovePlayerCorpse", x, y, z)
        print(LOG_PREFIX .. "ExtremeMode: removed player corpse via removeCorpse.")
        return
    end

    if square.transmitRemoveItemFromSquare then
        square:transmitRemoveItemFromSquare(body)
    end
    body:removeFromWorld()
    body:removeFromSquare()
    syncClients("RemovePlayerCorpse", x, y, z)
    print(LOG_PREFIX .. "ExtremeMode: removed player corpse via fallback.")
end

local function onZombieCreate(zombie)
    if not isExtremeModeEnabled() then
        return
    end
    if not zombie or not zombie:isReanimatedPlayer() then
        return
    end

    local square = zombie:getSquare()
    local x = square and square:getX() or zombie:getX()
    local y = square and square:getY() or zombie:getY()
    local z = square and square:getZ() or zombie:getZ()

    zombie:removeFromWorld()
    zombie:removeFromSquare()
    syncClients("RemoveReanimatedPlayerZombie", x, y, z)
    print(LOG_PREFIX .. "ExtremeMode: removed reanimated player zombie.")
end

local _nextZombieSweepMs = 0
local _tickSweepCounter = 0
local function onTickZombieSweep()
    if not isExtremeModeEnabled() then
        return
    end

    local shouldRun = false
    if getTimestampMs then
        local now = getTimestampMs()
        if now >= _nextZombieSweepMs then
            _nextZombieSweepMs = now + ZOMBIE_SWEEP_INTERVAL_MS
            shouldRun = true
        end
    else
        _tickSweepCounter = _tickSweepCounter + 1
        if _tickSweepCounter >= 60 then
            _tickSweepCounter = 0
            shouldRun = true
        end
    end

    if not shouldRun then
        return
    end

    local cell = getCell()
    if not cell then
        return
    end
    local zombies = cell:getZombieList()
    if not zombies then
        return
    end

    for i = zombies:size() - 1, 0, -1 do
        local zombie = zombies:get(i)
        if zombie and zombie:isReanimatedPlayer() then
            local square = zombie:getSquare()
            local x = square and square:getX() or zombie:getX()
            local y = square and square:getY() or zombie:getY()
            local z = square and square:getZ() or zombie:getZ()
            zombie:removeFromWorld()
            zombie:removeFromSquare()
            syncClients("RemoveReanimatedPlayerZombie", x, y, z)
            print(LOG_PREFIX .. "ExtremeMode: sweep removed reanimated player zombie.")
        end
    end
end

if not Drop.deathHooked then
    if Events and Events.OnCharacterDeath then
        Events.OnCharacterDeath.Add(onCharacterDeath)
        Drop.deathHooked = true
        print(LOG_PREFIX .. "OnCharacterDeath hook installed.")
    else
        print(LOG_PREFIX .. "OnCharacterDeath is unavailable.")
    end
end

if not Drop.bodyHooked then
    if Events and Events.OnDeadBodySpawn then
        Events.OnDeadBodySpawn.Add(onDeadBodySpawn)
        Drop.bodyHooked = true
        print(LOG_PREFIX .. "OnDeadBodySpawn hook installed.")
    else
        print(LOG_PREFIX .. "OnDeadBodySpawn is unavailable.")
    end
end

if not Drop.zombieHooked then
    if Events and Events.OnZombieCreate then
        Events.OnZombieCreate.Add(onZombieCreate)
        Drop.zombieHooked = true
        print(LOG_PREFIX .. "OnZombieCreate hook installed.")
    else
        print(LOG_PREFIX .. "OnZombieCreate is unavailable.")
    end
end

if not Drop.sweepHooked then
    if Events and Events.OnTick then
        Events.OnTick.Add(onTickZombieSweep)
        Drop.sweepHooked = true
        print(LOG_PREFIX .. "OnTick sweep hook installed.")
    else
        print(LOG_PREFIX .. "OnTick is unavailable.")
    end
end

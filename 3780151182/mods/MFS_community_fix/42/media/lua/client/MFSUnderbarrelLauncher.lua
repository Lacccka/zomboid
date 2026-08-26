-- Generic Phase 6 underbarrel switch layer. Registered launcher parts turn the
-- exact host rifle into a convention-matched functional pseudo only while held.
-- Add future launcher names in shared/MFSUnderbarrelRegistry.lua, not here.

require "MFSUnderbarrelRegistry"

MFSUnderbarrelLauncher = MFSUnderbarrelLauncher or {}
local Switch = MFSUnderbarrelLauncher

Switch.VERSION = "1.0.8-mp-hardening-audit"
Switch.DEBUG = getDebug()
Switch.MODE_SWITCH_COOLDOWN_MS = 300
Switch.MODE_TOGGLE_BIND = "ToggleGrenadeLauncherMode"
Switch.LAUNCHERS = MFSUnderbarrelRegistry.LAUNCHERS
Switch.MP_MODULE = MFSUnderbarrelRegistry.MP.MODULE
Switch.MP_VERSION = MFSUnderbarrelRegistry.MP.VERSION
Switch.MP_CREATE = MFSUnderbarrelRegistry.MP.CREATE_UNDERBARREL
Switch.MP_CREATE_ACK = MFSUnderbarrelRegistry.MP.CREATE_UNDERBARREL_ACK
Switch.MP_REMOVE = MFSUnderbarrelRegistry.MP.REMOVE_UNDERBARREL
Switch.MP_CREATE_TIMEOUT_MS = 5000
Switch.VISUAL_CHECK_IDLE_MS = 500
Switch.VISUAL_CHECK_ACTIVE_MS = 150
Switch.VISUAL_REPAIR_WINDOW_MS = 1200

local function debugLog(message)
    if Switch.DEBUG then print(message) end
end

local function findItemRecursive(container, itemID, depth)
    if not container or not itemID or depth > 8 then return nil end
    local direct = container:getItemById(itemID)
    if direct then return direct end

    local items = container:getItems()
    for index = 0, items:size() - 1 do
        local item = items:get(index)
        if item and item:getID() == itemID then return item end
        if item and instanceof(item, "InventoryContainer") then
            local nested = findItemRecursive(item:getInventory(), itemID, depth + 1)
            if nested then return nested end
        end
    end
    return nil
end

local function copyWeaponParts(source, destination)
    -- Server copyAuthoritativeVisualParts() establishes replicated native part
    -- identity. This deliberately separate client path also sees MFS's
    -- client-only setWeaponPart patch, ModData visual map and GunPos overrides.
    local sourceParts = source:getModData().weaponpart
    local copied = {}
    if type(sourceParts) == "table" then
        for slot, fullType in pairs(sourceParts) do
            if type(slot) == "string" and type(fullType) == "string" then
                copied[slot] = fullType
            end
        end
    end

    -- Installed parts are native HandWeapon state in Build 42. They are not
    -- guaranteed to be mirrored into MFS's legacy ModData table. Derive the
    -- visual proxy map from the native list without moving the original parts.
    local nativeParts = source:getAllWeaponParts()
    if nativeParts then
        for index = 0, nativeParts:size() - 1 do
            local part = nativeParts:get(index)
            if part then
                local slot = part:getPartType()
                local fullType = part:getFullType()
                if type(slot) == "string" and type(fullType) == "string" then
                    copied[slot] = fullType
                end
            end
        end
    end
    -- Rebuild the pseudo's native visual-part set on every switch. Clearing the
    -- previous clones first prevents stale models when the host configuration
    -- changes between launcher-mode entries.
    local oldNativeParts = destination:getAllWeaponParts()
    local oldSlots = {}
    if oldNativeParts then
        for index = 0, oldNativeParts:size() - 1 do
            local oldPart = oldNativeParts:get(index)
            if oldPart and oldPart:getPartType() then
                oldSlots[#oldSlots + 1] = oldPart:getPartType()
            end
        end
    end
    for _, slot in ipairs(oldSlots) do
        destination:clearWeaponPart(slot)
    end

    -- Every registered pseudo must own the maintained ModelWeaponPart catalog.
    -- Rebuild every installed host part as a fresh native visual clone.
    -- The preserved host keeps its original objects; the pseudo receives no
    -- mechanical part stat transfer because setWeaponPart is used directly.
    local nativeCopyCount = 0
    for slot, fullType in pairs(copied) do
        local visualPart = instanceItem(fullType)
        if visualPart and instanceof(visualPart, "WeaponPart") then
            destination:setWeaponPart(slot, visualPart)
            nativeCopyCount = nativeCopyCount + 1
        end
    end

    local destinationData = destination:getModData()
    destinationData.weaponpart = nil
    destinationData.MFSUnderbarrelVisualParts = copied
    destinationData.MFSUnderbarrelNativePartCount = nativeCopyCount

    -- Inspection may store per-fullType placement overrides here. Copy the
    -- nested numeric records so the pseudo does not share mutable tables with
    -- the preserved host rifle.
    local sourceGunPos = source:getModData().GunPos
    if type(sourceGunPos) == "table" then
        local copiedGunPos = {}
        for fullType, position in pairs(sourceGunPos) do
            if type(fullType) == "string" and type(position) == "table" then
                copiedGunPos[fullType] = {
                    x = position.x,
                    y = position.y,
                    z = position.z,
                }
            end
        end
        destination:getModData().GunPos = copiedGunPos
    else
        destination:getModData().GunPos = nil
    end
    return copied
end

local function collectNativePartMap(weapon)
    local result = {}
    local parts = weapon and weapon:getAllWeaponParts() or nil
    if parts then
        for index = 0, parts:size() - 1 do
            local part = parts:get(index)
            if part and part:getPartType() and part:getFullType() then
                result[part:getPartType()] = part:getFullType()
            end
        end
    end
    return result
end

local function nativePartMapsMatch(host, pseudo)
    local expected = collectNativePartMap(host)
    local actual = collectNativePartMap(pseudo)
    local expectedCount = 0
    local actualCount = 0
    for slot, fullType in pairs(expected) do
        expectedCount = expectedCount + 1
        if actual[slot] ~= fullType then return false end
    end
    for _ in pairs(actual) do actualCount = actualCount + 1 end
    return expectedCount == actualCount
end

local function reconcileVisualProxy(player, host, pseudo, forceModelRefresh)
    local spriteMismatch = pseudo:getWeaponSprite() ~= host:getWeaponSprite()
    local partsMismatch = not nativePartMapsMatch(host, pseudo)
    if spriteMismatch then pseudo:setWeaponSprite(host:getWeaponSprite()) end
    if partsMismatch then copyWeaponParts(host, pseudo) end
    if spriteMismatch or partsMismatch or forceModelRefresh then
        player:resetEquippedHandsModels()
    end
    if spriteMismatch or partsMismatch then
        debugLog("[MFSUnderbarrel][VisualRepair] sprite=" .. tostring(spriteMismatch)
            .. " parts=" .. tostring(partsMismatch)
            .. " ammo=" .. tostring(pseudo:getCurrentAmmoCount()))
    elseif forceModelRefresh then
        debugLog("[MFSUnderbarrel][VisualRefresh] native parts intact; refreshed model ammo="
            .. tostring(pseudo:getCurrentAmmoCount()))
    end
end

local visualWatch = setmetatable({}, { __mode = "k" })

local function updateVisualProxyAfterAmmoSync(player, host, pseudo)
    local now = getTimestampMs()
    local ammo = pseudo:getCurrentAmmoCount()
    local state = visualWatch[pseudo]
    if not state then
        state = { ammo = ammo, nextCheckAt = now + Switch.VISUAL_CHECK_IDLE_MS }
        visualWatch[pseudo] = state
    elseif state.ammo ~= ammo then
        -- B42 MP replaces/refreshes firearm fields a few frames after both fire
        -- and reload. Watch the full synchronization window, rather than doing
        -- one immediate repair that the later authoritative update can erase.
        state.ammo = ammo
        state.repairUntil = now + Switch.VISUAL_REPAIR_WINDOW_MS
        state.nextCheckAt = now + Switch.VISUAL_CHECK_ACTIVE_MS
        state.forceModelRefresh = true
    end

    if now < (state.nextCheckAt or 0) then return end
    local activeWindow = state.repairUntil and now <= state.repairUntil
    reconcileVisualProxy(player, host, pseudo, state.forceModelRefresh == true)
    state.forceModelRefresh = false
    state.nextCheckAt = now + (activeWindow
        and Switch.VISUAL_CHECK_ACTIVE_MS or Switch.VISUAL_CHECK_IDLE_MS)
    if not activeWindow then state.repairUntil = nil end
end

local function equipTwoHanded(player, item)
    -- A hotbar-assigned weapon can otherwise remain rendered on the back when
    -- hands are changed directly instead of through ISEquipWeaponAction.
    pcall(function() player:removeAttachedItem(item) end)
    player:setPrimaryHandItem(nil)
    player:setSecondaryHandItem(nil)
    player:setPrimaryHandItem(item)
    player:setSecondaryHandItem(item)
    player:resetEquippedHandsModels()
    if type(AWCWF_Attach) == "table" and type(AWCWF_Attach.Apply_Effect) == "function" then
        AWCWF_Attach.Apply_Effect(player, item, true)
    end
end

local function detachFromBack(player, item)
    if not player or not item then return end
    pcall(function() player:removeAttachedItem(item) end)
end

local function clearSavedHotbar(host)
    local data = host and host:getModData() or nil
    if not data then return end
    data.MFSUnderbarrelHotbarSlot = nil
    data.MFSUnderbarrelHotbarSlotType = nil
end

local function suppressHostHotbar(player, host)
    if not player or not host then return end
    local slotIndex = host:getAttachedSlot()
    if not slotIndex or slotIndex < 0 then
        clearSavedHotbar(host)
        detachFromBack(player, host)
        return
    end

    local data = host:getModData()
    data.MFSUnderbarrelHotbarSlot = slotIndex
    data.MFSUnderbarrelHotbarSlotType = host:getAttachedSlotType()

    local hotbar = getPlayerHotbar(player:getPlayerNum())
    if hotbar then
        hotbar:removeItem(host, false)
        if type(syncItemFields) == "function" then
            pcall(syncItemFields, player, host)
        end
    else
        detachFromBack(player, host)
        host:setAttachedSlot(-1)
        host:setAttachedSlotType(nil)
        host:setAttachedToModel(nil)
    end
end

local function restoreHostHotbar(player, host)
    if not player or not host then return false end
    local data = host:getModData()
    local savedIndex = tonumber(data.MFSUnderbarrelHotbarSlot)
    local savedType = data.MFSUnderbarrelHotbarSlotType
    if not savedIndex or type(savedType) ~= "string" then return false end

    local hotbar = getPlayerHotbar(player:getPlayerNum())
    if not hotbar then return false end

    local slotIndex = savedIndex
    local slot = hotbar.availableSlot and hotbar.availableSlot[slotIndex] or nil
    if not slot or slot.slotType ~= savedType then
        slotIndex = hotbar:getThisSlotIndex(savedType)
        slot = slotIndex and hotbar.availableSlot[slotIndex] or nil
    end
    if not slot or not slot.def then
        clearSavedHotbar(host)
        print("[MFSUnderbarrel] hotbar slot no longer available type=" .. tostring(savedType))
        return false
    end

    local attachment = slot.def.attachments
        and slot.def.attachments[host:getAttachmentType()] or nil
    if not attachment then
        clearSavedHotbar(host)
        print("[MFSUnderbarrel] hotbar attachment no longer valid type=" .. tostring(savedType))
        return false
    end

    hotbar:attachItem(host, attachment, slotIndex, slot.def, false)
    if type(syncItemFields) == "function" then
        pcall(syncItemFields, player, host)
    end
    clearSavedHotbar(host)
    return true
end

local function getLauncherState(host, definition, create)
    if not host or not definition then return nil end
    local data = host:getModData()
    if type(data.MFSUnderbarrelLauncherStates) ~= "table" then
        if not create then return nil end
        data.MFSUnderbarrelLauncherStates = {}
    end
    local state = data.MFSUnderbarrelLauncherStates[definition.name]
    if type(state) ~= "table" and create then
        state = {}
        data.MFSUnderbarrelLauncherStates[definition.name] = state
    end
    return state
end

local function savePseudoState(host, pseudo, definition)
    if not host or not pseudo then return end
    definition = definition or MFSUnderbarrelRegistry.getForPseudo(pseudo)
    local state = getLauncherState(host, definition, true)
    if not state then return end
    state.ammo = pseudo:getCurrentAmmoCount()
    state.condition = pseudo:getCondition()
    state.jammed = pseudo:isJammed()
end

local function restorePseudoState(host, pseudo, definition)
    if not host or not pseudo then return end
    definition = definition or MFSUnderbarrelRegistry.getForPseudo(pseudo)
    local state = getLauncherState(host, definition, false)
    local data = host:getModData()

    -- One-time compatibility read for rifles saved by the M203-only testbed.
    if not state and definition and definition.name == "MFS_M203"
        and (type(data.MFSUnderbarrelSavedAmmo) == "number"
            or type(data.MFSUnderbarrelSavedCondition) == "number"
            or type(data.MFSUnderbarrelSavedJammed) == "boolean") then
        state = {
            ammo = data.MFSUnderbarrelSavedAmmo,
            condition = data.MFSUnderbarrelSavedCondition,
            jammed = data.MFSUnderbarrelSavedJammed,
        }
        data.MFSUnderbarrelLauncherStates = data.MFSUnderbarrelLauncherStates or {}
        data.MFSUnderbarrelLauncherStates[definition.name] = state
    end

    if not state then return end
    if type(state.ammo) == "number" then
        pseudo:setCurrentAmmoCount(math.max(0,
            math.min(pseudo:getMaxAmmo(), state.ammo)))
    end
    if type(state.condition) == "number" then
        pseudo:setCondition(math.max(0,
            math.min(pseudo:getConditionMax(), state.condition)))
    end
    if type(state.jammed) == "boolean" then
        pseudo:setJammed(state.jammed)
    end
end

local function removeVirtualPseudo(player, pseudo)
    if not player or not pseudo then return end
    local hotbar = getPlayerHotbar(player:getPlayerNum())
    if hotbar then
        -- removeAttachedItem() clears only the rendered model. The full hotbar
        -- path also clears AttachedSlot/Type/Model and rebuilds attachedItems,
        -- preventing a removed pseudo from leaving a phantom back render.
        hotbar:removeItem(pseudo, false)
    else
        detachFromBack(player, pseudo)
        pseudo:setAttachedSlot(-1)
        pseudo:setAttachedSlotType(nil)
        pseudo:setAttachedToModel(nil)
    end
    if player:getPrimaryHandItem() == pseudo then player:setPrimaryHandItem(nil) end
    if player:getSecondaryHandItem() == pseudo then player:setSecondaryHandItem(nil) end
    player:resetEquippedHandsModels()

    local container = pseudo:getContainer()
    if container then
        pcall(function()
            if isClient() then
                sendClientCommand(player, Switch.MP_MODULE, Switch.MP_REMOVE, {
                    protocolVersion = Switch.MP_VERSION,
                    pseudoID = pseudo:getID(),
                    hostID = pseudo:getModData().MFSUnderbarrelHostID,
                })
            end
            container:Remove(pseudo)
        end)
    end
end

local function collectInactivePseudos(container, activePseudo, found, depth)
    if not container or depth > 8 then return end
    local items = container:getItems()
    for index = 0, items:size() - 1 do
        local item = items:get(index)
        if item then
            if item ~= activePseudo and MFSUnderbarrelRegistry.isPseudo(item) then
                found[#found + 1] = item
            end
            if instanceof(item, "InventoryContainer") then
                collectInactivePseudos(item:getInventory(), activePseudo, found, depth + 1)
            end
        end
    end
end

local function cleanupInactivePseudos(player, activePseudo)
    local found = {}
    collectInactivePseudos(player:getInventory(), activePseudo, found, 0)
    for _, pseudo in ipairs(found) do
        local definition = MFSUnderbarrelRegistry.getForPseudo(pseudo)
        local hostID = tonumber(pseudo:getModData().MFSUnderbarrelHostID)
        local host = hostID and findItemRecursive(player:getInventory(), hostID, 0) or nil
        local validHost = host and MFSUnderbarrelRegistry.getForHost(host) == definition
        if validHost then
            savePseudoState(host, pseudo, definition)
            if tonumber(host:getModData().MFSUnderbarrelPseudoID) == pseudo:getID() then
                host:getModData().MFSUnderbarrelPseudoID = nil
            end
        end
        removeVirtualPseudo(player, pseudo)
        if validHost then
            restoreHostHotbar(player, host)
        end
        if Switch._activeHosts then
            Switch._activeHosts[player:getPlayerNum()] = nil
        end
        debugLog("[MFSUnderbarrel] removed inactive virtual launcher id="
            .. tostring(pseudo:getID()))
    end
end

local function hasQueuedTimedAction(player)
    local queues = type(ISTimedActionQueue) == "table" and ISTimedActionQueue.queues or nil
    local queue = queues and queues[player] or nil
    return queue and queue.queue and #queue.queue > 0 or false
end

local function findLinkedPseudo(player, host, definition)
    local pseudoID = tonumber(host:getModData().MFSUnderbarrelPseudoID)
    local pseudo = pseudoID and findItemRecursive(player:getInventory(), pseudoID, 0) or nil
    if pseudo and MFSUnderbarrelRegistry.getForPseudo(pseudo) == definition then return pseudo end
    return nil
end

local function activateLauncherMode(player, host, pseudo, definition)
    if not player or not host or not pseudo or not definition then return false end
    host:getModData().MFSUnderbarrelPseudoID = pseudo:getID()
    local pseudoData = pseudo:getModData()
    pseudoData.MFSUnderbarrelVisualProxy = nil
    pseudoData.MFSUnderbarrelUseHostHeldCoordinates = nil
    pseudoData.MFSUnderbarrelHostID = host:getID()
    pseudoData.MFSUnderbarrelHostType = host:getFullType()
    pseudoData.MFSUnderbarrelLauncherName = definition.name
    local hostSprite = host:getWeaponSprite()
    pseudoData.MFSUnderbarrelExpectedSprite = hostSprite
    pseudoData.MFSUnderbarrelProbeAt = getTimestampMs() + 750
    pseudoData.MFSUnderbarrelProbeDone = false
    pseudo:setWeaponSprite(hostSprite)
    local copiedParts = copyWeaponParts(host, pseudo)
    suppressHostHotbar(player, host)
    Switch._activeHosts = Switch._activeHosts or {}
    Switch._activeHosts[player:getPlayerNum()] = host
    equipTwoHanded(player, pseudo)
    local partCount = 0
    for _ in pairs(copiedParts) do partCount = partCount + 1 end
    debugLog("[MFSUnderbarrel] entered " .. tostring(definition.name)
        .. " mode on " .. tostring(host:getFullType()) .. " id=" .. tostring(host:getID())
        .. " sprite=" .. tostring(pseudo:getWeaponSprite()) .. " parts=" .. tostring(partCount))
    local partDescriptions = {}
    for slot, fullType in pairs(copiedParts) do
        partDescriptions[#partDescriptions + 1] = tostring(slot) .. "=" .. tostring(fullType)
    end
    table.sort(partDescriptions)
    debugLog("[MFSUnderbarrel][VisualParts] " .. table.concat(partDescriptions, ", "))
    return true
end

local function enterLauncherMode(player, host)
    local definition = MFSUnderbarrelRegistry.getForHost(host)
    if not definition then return false end

    local pseudo = findLinkedPseudo(player, host, definition)
    if pseudo then return activateLauncherMode(player, host, pseudo, definition) end

    if isClient() then
        Switch._pendingEntries = Switch._pendingEntries or {}
        local playerNum = player:getPlayerNum()
        if Switch._pendingEntries[playerNum] then return false end
        local state = getLauncherState(host, definition, false)
        local requestID = tostring(player:getOnlineID()) .. ":" .. tostring(host:getID())
            .. ":" .. tostring(getTimestampMs())
        Switch._pendingEntries[playerNum] = {
            requestID = requestID,
            hostID = host:getID(),
            launcherName = definition.name,
            requestedAt = getTimestampMs(),
        }
        sendClientCommand(player, Switch.MP_MODULE, Switch.MP_CREATE, {
            protocolVersion = Switch.MP_VERSION,
            requestID = requestID,
            hostID = host:getID(),
            launcherName = definition.name,
            ammo = state and state.ammo or 0,
            condition = state and state.condition or nil,
            jammed = state and state.jammed or false,
        })
        return true
    end

    pseudo = instanceItem(definition.pseudoType)
    if not pseudo then return false end
    player:getInventory():AddItem(pseudo)
    restorePseudoState(host, pseudo, definition)
    return activateLauncherMode(player, host, pseudo, definition)
end

local function processPendingEntry(player)
    local pendingEntries = Switch._pendingEntries
    local playerNum = player and player:getPlayerNum() or nil
    local pending = playerNum and pendingEntries and pendingEntries[playerNum] or nil
    if not pending then return false end

    if pending.accepted == false then
        print("[MFSUnderbarrel] server rejected launcher mode: " .. tostring(pending.reason))
        pendingEntries[playerNum] = nil
        return false
    end
    if getTimestampMs() - pending.requestedAt > Switch.MP_CREATE_TIMEOUT_MS then
        print("[MFSUnderbarrel] server launcher creation timed out request="
            .. tostring(pending.requestID))
        pendingEntries[playerNum] = nil
        return false
    end
    if not pending.pseudoID then return true end

    local host = findItemRecursive(player:getInventory(), pending.hostID, 0)
    local pseudo = findItemRecursive(player:getInventory(), pending.pseudoID, 0)
    local definition = MFSUnderbarrelRegistry.LAUNCHERS[pending.launcherName]
    if not host or MFSUnderbarrelRegistry.getForHost(host) ~= definition then
        pendingEntries[playerNum] = nil
        if pseudo then removeVirtualPseudo(player, pseudo) end
        return false
    end
    if not pseudo then return true end

    pendingEntries[playerNum] = nil
    debugLog("[MFSUnderbarrel] server-authorized pseudo ready id="
        .. tostring(pseudo:getID()) .. " request=" .. tostring(pending.requestID))
    activateLauncherMode(player, host, pseudo, definition)
    return false
end

local function runGenericModelProbe(player)
    if processPendingEntry(player) then return end
    local pseudo = player and player:getPrimaryHandItem() or nil
    local definition = pseudo and MFSUnderbarrelRegistry.getForPseudo(pseudo) or nil
    if not definition then
        if player then
            -- ISAttachItemHotbar removes the item from the hands at its animation
            -- event, then attaches it in perform(). Removing the pseudo between
            -- those two stages lets perform() resurrect a model-only phantom.
            if hasQueuedTimedAction(player) then return end
            Switch._nextPseudoCleanupAt = Switch._nextPseudoCleanupAt or {}
            local playerNum = player:getPlayerNum()
            local now = getTimestampMs()
            if now >= (Switch._nextPseudoCleanupAt[playerNum] or 0) then
                Switch._nextPseudoCleanupAt[playerNum] = now + 1000
                cleanupInactivePseudos(player, nil)
            end
        end
        return
    end

    local data = pseudo:getModData()
    local hostID = tonumber(data.MFSUnderbarrelHostID)
    local host = hostID and findItemRecursive(player:getInventory(), hostID, 0) or nil
    local validHostType = host ~= nil
    local capabilityMissing = validHostType
        and MFSUnderbarrelRegistry.getForHost(host) ~= definition
    if not validHostType or capabilityMissing then
        data.MFSUnderbarrelHostMissingAt = data.MFSUnderbarrelHostMissingAt or getTimestampMs()
        if getTimestampMs() - data.MFSUnderbarrelHostMissingAt >= 500 then
            if capabilityMissing then
                savePseudoState(host, pseudo, definition)
                host:getModData().MFSUnderbarrelPseudoID = nil
                removeVirtualPseudo(player, pseudo)
                restoreHostHotbar(player, host)
                equipTwoHanded(player, host)
            else
                local activeHost = Switch._activeHosts
                    and Switch._activeHosts[player:getPlayerNum()] or nil
                if activeHost and activeHost:getID() == hostID then
                    savePseudoState(activeHost, pseudo, definition)
                    activeHost:getModData().MFSUnderbarrelPseudoID = nil
                    clearSavedHotbar(activeHost)
                end
                removeVirtualPseudo(player, pseudo)
            end
            if Switch._activeHosts then
                Switch._activeHosts[player:getPlayerNum()] = nil
            end
            print("[MFSUnderbarrel] launcher access removed; linked host missing or "
                .. tostring(definition.partType) .. " absent id=" .. tostring(hostID))
        end
        return
    end
    data.MFSUnderbarrelHostMissingAt = nil

    updateVisualProxyAfterAmmoSync(player, host, pseudo)

    if data.MFSUnderbarrelProbeDone or not data.MFSUnderbarrelProbeAt
        or getTimestampMs() < data.MFSUnderbarrelProbeAt then
        return
    end
    data.MFSUnderbarrelProbeDone = true

    local expected = data.MFSUnderbarrelExpectedSprite
    local actual = pseudo:getWeaponSprite()
    local model = actual and ScriptManager.instance:getModelScript("Base." .. actual) or nil
    local missingParts = 0
    local parts = data.MFSUnderbarrelVisualParts
    if type(parts) == "table" then
        for _, partFullType in pairs(parts) do
            if not ScriptManager.instance:getModelScript(partFullType) then
                missingParts = missingParts + 1
            end
        end
    end

    local modelInstanceFound = false
    if type(AWCWF_AdditionalParts) == "table"
        and type(AWCWF_AdditionalParts.GetWeaponModelInstance) == "function" then
        local ok, instance = pcall(AWCWF_AdditionalParts.GetWeaponModelInstance, player, pseudo)
        modelInstanceFound = ok and instance ~= nil
    end

    local passed = expected ~= nil and actual == expected and model ~= nil and missingParts == 0
    debugLog("[MFSUnderbarrel][GenericProbe] passed=" .. tostring(passed)
        .. " expected=" .. tostring(expected)
        .. " actual=" .. tostring(actual)
        .. " model=" .. tostring(model ~= nil)
        .. " modelInstance=" .. tostring(modelInstanceFound)
        .. " nativeParts=" .. tostring(data.MFSUnderbarrelNativePartCount or 0)
        .. " missingPartModels=" .. tostring(missingParts))
end

local function onServerCommand(module, command, args)
    if module ~= Switch.MP_MODULE or command ~= Switch.MP_CREATE_ACK
        or type(args) ~= "table" or args.protocolVersion ~= Switch.MP_VERSION then return end
    local player = getPlayer()
    local playerNum = player and player:getPlayerNum() or nil
    local pending = playerNum and Switch._pendingEntries
        and Switch._pendingEntries[playerNum] or nil
    if not pending or pending.requestID ~= args.requestID then return end
    pending.accepted = args.accepted == true
    pending.reason = args.reason
    pending.pseudoID = tonumber(args.pseudoID)
end

local function leaveLauncherMode(player, pseudo)
    local definition = MFSUnderbarrelRegistry.getForPseudo(pseudo)
    if not definition then return false end
    local hostID = tonumber(pseudo:getModData().MFSUnderbarrelHostID)
    local host = hostID and findItemRecursive(player:getInventory(), hostID, 0) or nil
    if not host then
        print("[MFSUnderbarrel] cannot restore missing host id=" .. tostring(hostID))
        local activeHost = Switch._activeHosts
            and Switch._activeHosts[player:getPlayerNum()] or nil
        if activeHost and activeHost:getID() == hostID then
            savePseudoState(activeHost, pseudo, definition)
            activeHost:getModData().MFSUnderbarrelPseudoID = nil
            clearSavedHotbar(activeHost)
        end
        removeVirtualPseudo(player, pseudo)
        if Switch._activeHosts then
            Switch._activeHosts[player:getPlayerNum()] = nil
        end
        return false
    end

    savePseudoState(host, pseudo, definition)
    host:getModData().MFSUnderbarrelPseudoID = nil
    removeVirtualPseudo(player, pseudo)
    restoreHostHotbar(player, host)
    equipTwoHanded(player, host)
    if Switch._activeHosts then
        Switch._activeHosts[player:getPlayerNum()] = nil
    end
    debugLog("[MFSUnderbarrel] restored " .. tostring(host:getFullType())
        .. " and removed " .. tostring(definition.name)
        .. " virtual launcher id=" .. tostring(host:getID()))
    return true
end

local function convertPseudoForHotbar(player, pseudo, host, definition)
    savePseudoState(host, pseudo, definition)
    host:getModData().MFSUnderbarrelPseudoID = nil
    -- The selected destination replaces the slot saved when launcher mode was
    -- entered. Do not restore the old slot before vanilla attaches the host.
    clearSavedHotbar(host)
    removeVirtualPseudo(player, pseudo)
    equipTwoHanded(player, host)
    if Switch._activeHosts then
        Switch._activeHosts[player:getPlayerNum()] = nil
    end
    debugLog("[MFSUnderbarrel] converted " .. tostring(definition.name)
        .. " virtual launcher to host for hotbar id=" .. tostring(host:getID()))
    return host
end

local function installHotbarAttachRedirect()
    if type(ISHotbar) ~= "table" or type(ISHotbar.attachItem) ~= "function" then
        return
    end

    -- Undo this file's previous wrapper when Lua is reloaded in debug mode.
    if Switch._hotbarAttachWrapper
        and ISHotbar.attachItem == Switch._hotbarAttachWrapper
        and type(Switch._originalHotbarAttachItem) == "function" then
        ISHotbar.attachItem = Switch._originalHotbarAttachItem
    end

    local originalAttachItem = ISHotbar.attachItem
    local wrapper = function(hotbar, item, slot, slotIndex, slotDef, doAnim)
        local definition = item and MFSUnderbarrelRegistry.getForPseudo(item) or nil
        if definition then
            local player = hotbar and hotbar.chr or nil
            local hostID = tonumber(item:getModData().MFSUnderbarrelHostID)
            local host = player and hostID
                and findItemRecursive(player:getInventory(), hostID, 0) or nil
            if not host then
                if player then removeVirtualPseudo(player, item) end
                print("[MFSUnderbarrel] rejected pseudo hotbar attach; host missing id="
                    .. tostring(hostID))
                return
            end
            if MFSUnderbarrelRegistry.getForHost(host) ~= definition then
                savePseudoState(host, item, definition)
                host:getModData().MFSUnderbarrelPseudoID = nil
                removeVirtualPseudo(player, item)
                restoreHostHotbar(player, host)
                equipTwoHanded(player, host)
                if Switch._activeHosts then
                    Switch._activeHosts[player:getPlayerNum()] = nil
                end
                print("[MFSUnderbarrel] rejected pseudo hotbar attach; "
                    .. tostring(definition.partType) .. " absent id=" .. tostring(hostID))
                return
            end

            local hostAttachment = slotDef and slotDef.attachments
                and slotDef.attachments[host:getAttachmentType()] or nil
            if not hostAttachment then
                print("[MFSUnderbarrel] rejected rifle hotbar destination slot="
                    .. tostring(slotIndex))
                return
            end

            item = convertPseudoForHotbar(player, item, host, definition)
            slot = hostAttachment
        end
        return originalAttachItem(hotbar, item, slot, slotIndex, slotDef, doAnim)
    end

    Switch._originalHotbarAttachItem = originalAttachItem
    Switch._hotbarAttachWrapper = wrapper
    ISHotbar.attachItem = wrapper
end

local function onKeyPressed(key)
    local player = getPlayer()
    if not player or player:isDead() then return end

    local primary = player:getPrimaryHandItem()
    if primary and MFSUnderbarrelRegistry.isPseudo(primary) then
        local hotbar = getPlayerHotbar(player:getPlayerNum())
        local slotIndex = hotbar and hotbar:getSlotForKey(key) or -1
        if slotIndex and slotIndex > -1 and not hotbar.attachedItems[slotIndex]
            and hotbar:isAllowedToActivateSlot() and not hotbar.radialWasVisible then
            local slot = hotbar.availableSlot[slotIndex]
            local slotDef = slot and slot.def or nil
            local pseudoAttachment = slotDef and slotDef.attachments
                and slotDef.attachments[primary:getAttachmentType()] or nil
            if pseudoAttachment then
                -- The wrapped attachItem validates the linked host and launcher,
                -- converts to rifle mode, then gives this slot to the real gun.
                hotbar:attachItem(primary, pseudoAttachment, slotIndex, slotDef, true)
                return
            end
        end
    end

    if key ~= getCore():getKey(Switch.MODE_TOGGLE_BIND) then return end
    if not primary then return end

    -- MP PERFORMANCE SAFETY CONCERN:
    -- Each accepted mode entry creates a pseudo firearm plus fresh visual-part
    -- clones, while exit may synchronize inventory/hotbar fields. A macro could
    -- otherwise generate needless allocations, log traffic and MP packets. The
    -- short per-player cooldown is deliberately silent so rejected spam does not
    -- create the very logging load this guard prevents. Blocking during attacks
    -- and timed actions also prevents switching midway through fire/reload/hotbar
    -- state transitions, where item ownership is temporarily incomplete.
    if player:isAttacking() or hasQueuedTimedAction(player) then return end
    Switch._nextModeSwitchAt = Switch._nextModeSwitchAt or {}
    local playerNum = player:getPlayerNum()
    local now = getTimestampMs()
    if now < (Switch._nextModeSwitchAt[playerNum] or 0) then return end
    Switch._nextModeSwitchAt[playerNum] = now + Switch.MODE_SWITCH_COOLDOWN_MS

    if MFSUnderbarrelRegistry.isPseudo(primary) then
        leaveLauncherMode(player, primary)
    else
        enterLauncherMode(player, primary)
    end
end

if Switch._keyCallback then Events.OnKeyPressed.Remove(Switch._keyCallback) end
if Switch._probeCallback then Events.OnPlayerUpdate.Remove(Switch._probeCallback) end
if Switch._serverCommandCallback then Events.OnServerCommand.Remove(Switch._serverCommandCallback) end
Switch._keyCallback = onKeyPressed
Switch._probeCallback = runGenericModelProbe
Switch._serverCommandCallback = onServerCommand
installHotbarAttachRedirect()
Events.OnKeyPressed.Add(onKeyPressed)
Events.OnPlayerUpdate.Add(runGenericModelProbe)
Events.OnServerCommand.Add(onServerCommand)

print("[MFSUnderbarrel] version " .. Switch.VERSION .. " loaded")

-- Read-only physical membership trace for PackFlow mass consolidation.
--
-- This deliberately does not alter ISInventoryTransferAction, timed-action
-- queues, GridInventory metadata or the consolidation plan. It exists to
-- distinguish a real ItemContainer transfer from a GridInventory-only visual
-- relocation when multiplayer replication is faster than the UI animation.

local okMass, GridMassSort = pcall(require, "LCC/GridMassSort")
if not okMass or not GridMassSort then return end
if GridMassSort._lccPhysicalTraceInstalled then return end
GridMassSort._lccPhysicalTraceInstalled = true

local traces = {}
local TRACE_SETTLE_MS = 2000
local TRACE_SAMPLE_MS = 100

local function nowMs()
    return getTimestampMs and getTimestampMs()
        or (getTimeInMillis and getTimeInMillis() or 0)
end

local function getPlayerByNum(playerNum)
    if getSpecificPlayer then return getSpecificPlayer(playerNum or 0) end
    return getPlayer and getPlayer() or nil
end

local function isCorpseContainer(container)
    local parent = container and container.getParent and container:getParent() or nil
    return parent and instanceof and instanceof(parent, "IsoDeadBody") or false
end

local function isPlayerContainer(container, player)
    if not container or not player then return false end
    if player.getInventory and container == player:getInventory() then return true end
    if container.isInCharacterInventory then
        local ok, result = pcall(function()
            return container:isInCharacterInventory(player)
        end)
        if ok and result then return true end
    end
    return false
end

local function containerLabel(container)
    if not container then return "nil" end

    local parts = {}
    local ctype = container.getType and container:getType() or "?"
    table.insert(parts, tostring(ctype))

    local containingItem = container.getContainingItem and container:getContainingItem() or nil
    if containingItem then
        local id = containingItem.getID and containingItem:getID() or "?"
        local fullType = containingItem.getFullType and containingItem:getFullType() or "?"
        table.insert(parts, "bag=" .. tostring(fullType) .. "#" .. tostring(id))
    end

    local vehiclePart = container.getVehiclePart and container:getVehiclePart() or nil
    if vehiclePart then
        local vehicle = vehiclePart.getVehicle and vehiclePart:getVehicle() or nil
        local vehicleId = vehicle and vehicle.getId and vehicle:getId() or "?"
        local partId = vehiclePart.getId and vehiclePart:getId() or "?"
        table.insert(parts, "vehicle=" .. tostring(vehicleId) .. ":" .. tostring(partId))
    end

    local parent = container.getParent and container:getParent() or nil
    local square = parent and parent.getSquare and parent:getSquare() or nil
    if square and square.getX then
        table.insert(parts, "sq=" .. tostring(square:getX())
            .. "," .. tostring(square:getY()) .. "," .. tostring(square:getZ()))
    end

    -- Java tostring identity is diagnostic only; gameplay never depends on it.
    table.insert(parts, "obj=" .. tostring(container))
    return table.concat(parts, "|")
end

local function scopeContainers(scope, playerNum)
    local player = getPlayerByNum(playerNum)
    if not player then return {} end

    local page = nil
    if scope == GridMassSort.SCOPE_PLAYER then
        page = getPlayerInventory and getPlayerInventory(playerNum) or nil
    elseif scope == GridMassSort.SCOPE_EXTERNAL then
        page = getPlayerLoot and getPlayerLoot(playerNum) or nil
    end
    if not page then return {} end

    local seen, out = {}, {}
    local function add(container)
        if not container or seen[container] then return end
        if container.getType and container:getType() == "floor" then return end
        if isCorpseContainer(container) then return end

        local owned = isPlayerContainer(container, player)
        local wanted = (scope == GridMassSort.SCOPE_PLAYER and owned)
            or (scope == GridMassSort.SCOPE_EXTERNAL and not owned)
        if not wanted then return end

        seen[container] = true
        table.insert(out, container)
    end

    if scope == GridMassSort.SCOPE_PLAYER and player.getInventory then
        local root = player:getInventory()
        add(root)
        local items = root and root.getItems and root:getItems() or nil
        if items then
            for i = 0, items:size() - 1 do
                local item = items:get(i)
                if item and item.getInventory then
                    local ok, child = pcall(function() return item:getInventory() end)
                    if ok and child then add(child) end
                end
            end
        end
    end

    for _, button in ipairs(page.backpacks or {}) do
        if button and button.inventory then add(button.inventory) end
    end

    return out
end

local function snapshot(scope, playerNum)
    local result = {}
    local typeContainers = {}

    for _, container in ipairs(scopeContainers(scope, playerNum)) do
        local label = containerLabel(container)
        local items = container.getItems and container:getItems() or nil
        if items then
            for i = 0, items:size() - 1 do
                local item = items:get(i)
                local id = item and item.getID and item:getID() or nil
                if id ~= nil then
                    local fullType = item.getFullType and item:getFullType() or "?"
                    result[id] = {
                        id = id,
                        fullType = tostring(fullType),
                        container = container,
                        label = label,
                    }
                    local byContainer = typeContainers[tostring(fullType)]
                    if not byContainer then
                        byContainer = {}
                        typeContainers[tostring(fullType)] = byContainer
                    end
                    byContainer[container] = true
                end
            end
        end
    end

    local duplicateTypes = 0
    for _, byContainer in pairs(typeContainers) do
        local count = 0
        for _ in pairs(byContainer) do count = count + 1 end
        if count > 1 then duplicateTypes = duplicateTypes + 1 end
    end

    return result, duplicateTypes
end

local originalStart = GridMassSort.start
function GridMassSort.start(scope, playerNum)
    playerNum = playerNum or 0
    local before, duplicateTypes = snapshot(scope, playerNum)
    local ok, reason = originalStart(scope, playerNum)
    if ok then
        local itemCount = 0
        for _ in pairs(before) do itemCount = itemCount + 1 end
        local startedAt = nowMs()
        traces[playerNum] = {
            playerNum = playerNum,
            scope = scope,
            lastKnown = before,
            startedAt = startedAt,
            nextSampleAt = startedAt,
            finishedAt = nil,
            changes = 0,
        }
        print("[LCC GridSort][physical-trace] start scope=" .. tostring(scope)
            .. " items=" .. tostring(itemCount)
            .. " duplicateTypes=" .. tostring(duplicateTypes))
    end
    return ok, reason
end

local function traceTick(trace, sampleTime)
    local current = snapshot(trace.scope, trace.playerNum)
    local elapsed = sampleTime - trace.startedAt

    -- Compare each currently visible physical item to its last known container.
    -- Do NOT discard lastKnown entries when an ID disappears for a replication
    -- frame: MP can expose source-removal before destination-addition.
    for id, nextEntry in pairs(current) do
        local previous = trace.lastKnown[id]
        if previous and nextEntry.container ~= previous.container then
            trace.changes = trace.changes + 1
            print("[LCC GridSort][physical-trace] item=" .. tostring(id)
                .. " type=" .. tostring(previous.fullType)
                .. " elapsedMs=" .. tostring(elapsed)
                .. " from={" .. tostring(previous.label) .. "}"
                .. " to={" .. tostring(nextEntry.label) .. "}"
                .. " busy=" .. tostring(GridMassSort.isBusy(trace.playerNum)))
        end
        trace.lastKnown[id] = nextEntry
    end

    if GridMassSort.isBusy(trace.playerNum) then
        trace.finishedAt = nil
        return false
    end

    trace.finishedAt = trace.finishedAt or sampleTime
    if sampleTime - trace.finishedAt < TRACE_SETTLE_MS then return false end

    print("[LCC GridSort][physical-trace] end scope=" .. tostring(trace.scope)
        .. " physicalChanges=" .. tostring(trace.changes))
    return true
end

local function onTick()
    local sampleTime = nowMs()
    for playerNum, trace in pairs(traces) do
        if sampleTime >= (trace.nextSampleAt or 0) then
            trace.nextSampleAt = sampleTime + TRACE_SAMPLE_MS
            local ok, done = pcall(traceTick, trace, sampleTime)
            if not ok then
                print("[LCC GridSort][physical-trace] disabled after error: " .. tostring(done))
                traces[playerNum] = nil
            elseif done then
                traces[playerNum] = nil
            end
        end
    end
end

Events.OnTick.Add(onTick)
print("[LCC GridSort] read-only physical mass-transfer trace installed")

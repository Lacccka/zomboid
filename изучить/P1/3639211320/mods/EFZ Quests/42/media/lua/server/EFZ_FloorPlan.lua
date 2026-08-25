if not EFZ then
    EFZ = {}
end

pcall(require, "EFZ_FloorPlan_Shared")

local MODDATA_KEY = (EFZ and EFZ.FloorPlan and EFZ.FloorPlan.MODDATA_KEY) or "EFZ_FloorPlanState"
local openedState = ModData and ModData.getOrCreate and ModData.getOrCreate(MODDATA_KEY) or {}

local function getMajorBuild()
    local ver = getCore():getVersionNumber()
    local major = tonumber(string.match(ver, "^(%d+)"))
    return major or 41
end

local function getChunkTileSize(major)
    -- B41 chunks are 10x10 tiles, B42 chunks are 8x8 tiles
    if major >= 42 then return 8 end
    return 10
end

local MAJOR = getMajorBuild()
local CHUNK_TILES = getChunkTileSize(MAJOR)
local TARGET_CHUNK_WX = math.floor(20396 / CHUNK_TILES)
local TARGET_CHUNK_WY = math.floor(5 / CHUNK_TILES)

local function applyOpenedWallsIfPossible()
    if not (EFZ and EFZ.FloorPlan and EFZ.FloorPlan.OpenByFullType) then
        pcall(require, "EFZ_FloorPlan_Shared")
    end
    if not (EFZ and EFZ.FloorPlan and EFZ.FloorPlan.OpenByFullType) then
        return
    end

    -- 서버 권한으로 월드 변경(벽 제거). 청크가 아직 로드되지 않았으면 내부에서 그냥 실패/무시된다.
    if openedState and openedState["EFZ.LivingSpaceFloorPlanUpper"] == true then
        EFZ.FloorPlan.OpenByFullType("EFZ.LivingSpaceFloorPlanUpper")
    end
    if openedState and openedState["EFZ.LivingSpaceFloorPlanLower"] == true then
        EFZ.FloorPlan.OpenByFullType("EFZ.LivingSpaceFloorPlanLower")
    end
end

local _floorPlanHookAdded = false
local function ensureFloorPlanLoadHook()
    if _floorPlanHookAdded then
        return
    end

    if MAJOR >= 42 and Events and Events.LoadChunk and type(Events.LoadChunk.Add) == "function" then
        Events.LoadChunk.Add(function(chunk)
            if not chunk then return end
            local wx = chunk.wx
            local wy = chunk.wy
            if wx == nil and chunk.getX then wx = chunk:getX() end
            if wy == nil and chunk.getY then wy = chunk:getY() end
            if wx == nil and chunk.getWX then wx = chunk:getWX() end
            if wy == nil and chunk.getWY then wy = chunk:getWY() end
            if wx == nil or wy == nil then return end

            if wx == TARGET_CHUNK_WX and wy == TARGET_CHUNK_WY then
                applyOpenedWallsIfPossible()
            end
        end)
        _floorPlanHookAdded = true
        return
    end

    if Events and Events.LoadGridsquare and type(Events.LoadGridsquare.Add) == "function" then
        Events.LoadGridsquare.Add(function(square)
            if not square then return end
            local x = square:getX()
            local y = square:getY()
            local wx = math.floor(x / CHUNK_TILES)
            local wy = math.floor(y / CHUNK_TILES)
            if wx == TARGET_CHUNK_WX and wy == TARGET_CHUNK_WY then
                applyOpenedWallsIfPossible()
            end
        end)
        _floorPlanHookAdded = true
    end
end

local function safeGetNumber(obj, fnName)
    if not obj or not fnName or not obj[fnName] then
        return nil
    end
    local ok, res = pcall(obj[fnName], obj)
    if not ok then
        return nil
    end
    return tonumber(res)
end

local function getInnerContainer(item)
    if not item then
        return nil
    end

    if item.getInventory then
        local ok, res = pcall(function()
            return item:getInventory()
        end)
        if ok and res and res.getItems then
            return res
        end
    end

    if item.getItemContainer then
        local ok, res = pcall(function()
            return item:getItemContainer()
        end)
        if ok and res and res.getItems then
            return res
        end
    end

    return nil
end

local function findItemByIdRecursive(container, targetId, visited)
    if not container or not targetId or not container.getItems then
        return nil
    end

    visited = visited or {}
    if visited[container] then
        return nil
    end
    visited[container] = true

    local items = container:getItems()
    if not items then
        return nil
    end

    for i = 0, items:size() - 1 do
        local it = items:get(i)
        if it then
            if it.getID then
                local id = safeGetNumber(it, "getID")
                if id and id == targetId then
                    return it
                end
            end
            local inner = getInnerContainer(it)
            if inner then
                local found = findItemByIdRecursive(inner, targetId, visited)
                if found then
                    return found
                end
            end
        end
    end

    return nil
end

local function findItemByFullTypeRecursive(container, fullType, visited)
    if not container or not fullType or not container.getItems then
        return nil
    end

    visited = visited or {}
    if visited[container] then
        return nil
    end
    visited[container] = true

    local items = container:getItems()
    if not items then
        return nil
    end

    for i = 0, items:size() - 1 do
        local it = items:get(i)
        if it and it.getFullType then
            local ok, ft = pcall(function()
                return it:getFullType()
            end)
            if ok and ft == fullType then
                return it
            end
        end
        local inner = it and getInnerContainer(it) or nil
        if inner then
            local found = findItemByFullTypeRecursive(inner, fullType, visited)
            if found then
                return found
            end
        end
    end

    return nil
end

local function transmitState()
    if ModData and ModData.transmit then
        ModData.transmit(MODDATA_KEY)
    end
end

local function findItemById(container, targetId)
    if not container or not targetId or not container.getItems then
        return nil
    end
    local items = container:getItems()
    if not items then
        return nil
    end
    for i = 0, items:size() - 1 do
        local it = items:get(i)
        if it and it.getID then
            local id = safeGetNumber(it, "getID")
            if id and id == targetId then
                return it
            end
        end
    end
    return nil
end

local function removeItemFromItsContainer(character, item)
    if not item then
        return false
    end

    if character and character.removeFromHands then
        pcall(function()
            character:removeFromHands(item)
        end)
    end

    local container = item.getContainer and item:getContainer() or nil
    if container and container.Remove then
        container:Remove(item)
        if sendRemoveItemFromContainer then
            sendRemoveItemFromContainer(container, item)
        end
        return true
    end

    local inv = character and character.getInventory and character:getInventory() or nil
    if inv and inv.Remove then
        inv:Remove(item)
        if sendRemoveItemFromContainer then
            sendRemoveItemFromContainer(inv, item)
        end
        return true
    end

    return false
end

local function isAllowedFloorPlanType(fullType)
    return fullType == "EFZ.LivingSpaceFloorPlanUpper" or fullType == "EFZ.LivingSpaceFloorPlanLower"
end

local function onClientCommand(module, command, playerObj, args)
    if module ~= "EFZ" then
        return
    end
    if command == "RequestFloorPlanState" then
        transmitState()
        ensureFloorPlanLoadHook()
        applyOpenedWallsIfPossible()
        return
    end
    if command ~= "RequestOpenFloorPlan" then
        return
    end

    if not playerObj or not args then
        return
    end

    local fullType = args.fullType and tostring(args.fullType) or nil
    if not fullType or not isAllowedFloorPlanType(fullType) then
        return
    end

    -- already opened: don't consume and don't rebroadcast
    if openedState and openedState[fullType] == true then
        return
    end

    -- 1) 서버 권한으로 아이템 제거(가능하면 itemId로 정확히)
    local removed = false
    local inv = playerObj.getInventory and playerObj:getInventory() or nil
    if inv then
        local itemId = tonumber(args.itemId) or nil
        local targetItem = nil
        if itemId then
            targetItem = findItemByIdRecursive(inv, itemId, {})
        end
        if not targetItem then
            targetItem = findItemByFullTypeRecursive(inv, fullType, {})
        end
        if targetItem then
            removed = removeItemFromItsContainer(playerObj, targetItem) == true
        end
    end
    if not removed then
        return
    end

    -- 2) 개방 상태 저장 + ModData 동기화
    if openedState then
        openedState[fullType] = true
    end
    if EFZ and EFZ.FloorPlan and EFZ.FloorPlan.MarkOpened then
        EFZ.FloorPlan.MarkOpened(fullType)
    end
    transmitState()

    -- 3) 전체 클라에게 개방 커맨드 브로드캐스트
    if sendServerCommand then
        sendServerCommand("EFZ", "ApplyOpenFloorPlan", { fullType = fullType })
    end

    -- 4) 요청 직후(로드되어 있으면) 서버에서 즉시 적용 시도
    ensureFloorPlanLoadHook()
    applyOpenedWallsIfPossible()
end

Events.OnClientCommand.Add(onClientCommand)

-- Ensure new joiners receive the current state.
Events.OnCreatePlayer.Add(function()
    transmitState()
    ensureFloorPlanLoadHook()
    applyOpenedWallsIfPossible()
end)



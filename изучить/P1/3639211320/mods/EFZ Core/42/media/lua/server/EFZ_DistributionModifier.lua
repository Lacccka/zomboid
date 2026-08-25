local ResidentialRooms = {
    kitchen = true, bedroom = true, livingroom = true, bathroom = true,
    garage = true, closet = true, diningroom = true, laundry = true,
    hallway = true, shed = true, storeroom = true
}

local function isResidential(roomName)
    if not roomName then return false end
    return ResidentialRooms[string.lower(roomName)] == true
end

local function getSquareFromContainer(container)
    if not container then return nil end

    -- B41/B42 공통으로 흔히 존재
    if container.getSourceGrid then
        local square = container:getSourceGrid()
        if square then return square end
    end

    -- 일부 빌드/상황에서 이름이 다를 수 있어 방어
    if container.getSourceGridSquare then
        local square = container:getSourceGridSquare()
        if square then return square end
    end

    -- parent(예: IsoObject)에서 추적 가능한 경우
    if container.getParent then
        local parent = container:getParent()
        if parent and parent.getSquare then
            local square = parent:getSquare()
            if square then return square end
        end
    end

    -- 일부 컨테이너는 getSource만 제공
    if container.getSource then
        local source = container:getSource()
        if source and source.getSquare then
            local square = source:getSquare()
            if square then return square end
        end
    end

    return nil
end

local function getRoomNameFromContainer(container)
    local square = getSquareFromContainer(container)
    if not square then return nil end

    if square.getRoom then
        local room = square:getRoom()
        if room and room.getName then
            return room:getName()
        end
    end

    return nil
end

local function isItemContainer(obj)
    if not obj then return false end
    if instanceof and instanceof(obj, "ItemContainer") then return true end
    if type(obj) == "table" and obj.getItems then return true end
    return false
end

local function findContainerFromArgs(...)
    for i = 1, select("#", ...) do
        local v = select(i, ...)
        if isItemContainer(v) then
            return v
        end
    end
    return nil
end

local function getContainerSourceObject(container)
    if not container then return nil end

    if container.getSource then
        local source = container:getSource()
        if source then return source end
    end

    if container.getParent then
        local parent = container:getParent()
        if parent then return parent end
    end

    return nil
end

local function getContainerFlagKey(prefix, container)
    local suffix = "unknown"
    if container and container.getType then
        local ctype = container:getType()
        if type(ctype) == "string" and ctype ~= "" then
            suffix = string.lower(ctype)
        end
    end
    return prefix .. "_" .. suffix
end

local function getContainerApplyFlagKey(container)
    return getContainerFlagKey("EFZ_LootCutApplied", container)
end

local function getGunLimitAppliedFlagKey(container)
    return getContainerFlagKey("EFZ_GunLimitApplied", container)
end

local function hasLootCutApplied(container)
    local source = getContainerSourceObject(container)
    if not (source and source.getModData) then return false end

    local modData = source:getModData()
    if not modData then return false end

    return modData[getContainerApplyFlagKey(container)] == true
end

local function hasGunLimitApplied(container)
    local source = getContainerSourceObject(container)
    if not (source and source.getModData) then return false end

    local modData = source:getModData()
    if not modData then return false end

    return modData[getGunLimitAppliedFlagKey(container)] == true
end

local function markLootCutApplied(container)
    local source = getContainerSourceObject(container)
    if not (source and source.getModData) then return end

    local modData = source:getModData()
    if not modData then return end

    modData[getContainerApplyFlagKey(container)] = true
    if source.transmitModData then
        source:transmitModData()
    end
end

local function markGunLimitApplied(container)
    local source = getContainerSourceObject(container)
    if not (source and source.getModData) then return end

    local modData = source:getModData()
    if not modData then return end

    modData[getGunLimitAppliedFlagKey(container)] = true
    if source.transmitModData then
        source:transmitModData()
    end
end

local function isNewlyInstalledFurnitureContainer(container)
    local source = getContainerSourceObject(container)
    if not source then return false end

    return source.isMovedThumpable and source:isMovedThumpable()
end

local function isPlayerCraftedFurnitureContainer(container)
    local source = getContainerSourceObject(container)
    if not (source and source.getModData) then return false end

    local modData = source:getModData()
    if not modData then return false end

    if modData.EFZ_PlayerBuilt == true or modData.playerBuilt == true or modData.builtByPlayer == true then
        return true
    end

    if modData["xp:Woodwork"] or modData["xp:MetalWelding"] then
        return true
    end

    if modData["need:Base.Plank"] or modData["need:Base.Nails"] or modData["need:Base.Screws"] then
        return true
    end

    if modData["need:Base.SheetMetal"] or modData["need:Base.SmallSheetMetal"] or modData["need:Base.MetalPipe"] then
        return true
    end

    return false
end

local function isBagItem(item)
    if not item then return false end
    if instanceof and instanceof(item, "InventoryContainer") then
        return true
    end
    return item.IsInventoryContainer and item:IsInventoryContainer()
end

local function isBagContainer(container)
    if not (container and container.getContainingItem) then return false end
    return isBagItem(container:getContainingItem())
end

local function isFirearmItem(item)
    if not item then return false end
    if not (instanceof and instanceof(item, "HandWeapon")) then return false end
    return item:isAimedFirearm()
end

local function isZombieLootContainer(containerType, container)
    if type(containerType) == "string" then
        local lowered = string.lower(containerType)
        if lowered == "inventorymale" or lowered == "inventoryfemale" then
            return true
        end
    end

    if container and container.getType then
        local ctype = container:getType()
        if type(ctype) == "string" then
            local lowered = string.lower(ctype)
            if lowered == "inventorymale" or lowered == "inventoryfemale" then
                return true
            end
        end
    end

    local source = getContainerSourceObject(container)

    if source and instanceof then
        if instanceof(source, "IsoDeadBody") or instanceof(source, "IsoZombie") then
            return true
        end
    end

    if source and source.getObjectName then
        local objectName = source:getObjectName()
        if type(objectName) == "string" then
            local lowered = string.lower(objectName)
            if string.find(lowered, "deadbody", 1, true) or string.find(lowered, "zombie", 1, true) then
                return true
            end
        end
    end

    return false
end

local function removeItemSafe(container, inv, idx)
    local item = inv and inv:get(idx)
    if not item then return end

    local removed = false
    -- 서버/클라 공용: Remove가 가장 널리 노출
    if container and container.Remove then
        container:Remove(item)
        removed = true
    elseif container and container.DoRemoveItem then
        container:DoRemoveItem(item)
        removed = true
    elseif container and container.RemoveItem then
        container:RemoveItem(item)
        removed = true
    elseif container and container.removeItem then
        container:removeItem(item)
        removed = true
    elseif inv and inv.remove then
        inv:remove(item)
        removed = true
    elseif inv and inv.removeAt then
        inv:removeAt(idx)
        removed = true
    end

    -- 멀티 동기화(서버에서만, 함수가 존재할 때만)
    if removed and container and isServer and isServer() and sendRemoveItemFromContainer then
        sendRemoveItemFromContainer(container, item)
    end
end

local function getBuildingFromContainer(container)
    if not container then return nil end

    local square = getSquareFromContainer(container)
    if square and square.getBuilding then
        local building = square:getBuilding()
        if building then return building end
    end

    -- 혹시 source가 이미 building을 줄 수도 있음(안전망)
    if container.getSource then
        local source = container:getSource()
        if source and source.getBuilding then
            local building = source:getBuilding()
            if building then return building end
        end
    end

    return nil
end

local function limitFirearmsPerContainer(container)
    local inv = container:getItems()
    if not inv then return false end

    local size = inv:size() - 1
    if size < 0 then return true end

    local firearmSeen = false
    local removalIndexes = {}

    for i = 0, size do
        local item = inv:get(i)
        if isFirearmItem(item) then
            if firearmSeen then
                removalIndexes[#removalIndexes + 1] = i
            else
                firearmSeen = true
            end
        end
    end

    for i = #removalIndexes, 1, -1 do
        removeItemSafe(container, inv, removalIndexes[i])
    end

    return true
end

local loggedUnexpectedContainerArg = false

local function cutLootToFivePercent(roomName, containerType, container, ...)
    -- B42에서 이벤트 인자 순서가 달라져도 동작하도록, 컨테이너를 찾아냄
    if not isItemContainer(container) then
        if isItemContainer(containerType) then
            container = containerType
        elseif isItemContainer(roomName) then
            container = roomName
        else
            container = findContainerFromArgs(roomName, containerType, container, ...)
        end
    end

    if not isItemContainer(container) then
        if not loggedUnexpectedContainerArg then
            loggedUnexpectedContainerArg = true
            print("[EFZ Core] OnFillContainer skipped unexpected container arg: roomType=" .. tostring(roomName) .. ", containerType=" .. tostring(containerType) .. ", container=" .. tostring(container))
        end
        return
    end

    if isZombieLootContainer(containerType, container) then return end
    if isBagContainer(container) then return end
    if isNewlyInstalledFurnitureContainer(container) then return end
    if isPlayerCraftedFurnitureContainer(container) then return end

    if not hasGunLimitApplied(container) and limitFirearmsPerContainer(container) then
        markGunLimitApplied(container)
    end

    if hasLootCutApplied(container) then return end

    -- 건물 타입 확인: 주거용이 아니면 제외
    local building = getBuildingFromContainer(container)
    if not (building and building.isResidential and building:isResidential()) then
        return
    end

    -- roomName이 누락/뒤바뀐 경우를 대비해, 인자/컨테이너에서 방 이름을 최대한 복원
    local resolvedRoomName = nil
    if type(roomName) == "string" and isResidential(roomName) then
        resolvedRoomName = roomName
    elseif type(containerType) == "string" and isResidential(containerType) then
        resolvedRoomName = containerType
    else
        resolvedRoomName = getRoomNameFromContainer(container)
    end

    if not isResidential(resolvedRoomName) then return end

    local inv = container:getItems()
    if not inv then return end
    markLootCutApplied(container)
    local size = inv:size() - 1
    if size < 0 then return end

    -- 95% 확률로 각 아이템 제거 → 남는 양은 약 15% (원래 5%였는데 개빡센 거 같아서 바꿈)
    for i = size, 0, -1 do
        local item = inv:get(i)
        if item and not isBagItem(item) and ZombRandFloat(0, 1) > 0.15 then
            removeItemSafe(container, inv, i)
        end
    end
end

Events.OnFillContainer.Add(cutLootToFivePercent)
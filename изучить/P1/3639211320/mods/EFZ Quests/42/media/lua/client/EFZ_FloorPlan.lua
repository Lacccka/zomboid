if not EFZ then
    EFZ = {}
end

require "TimedActions/ISTimedActionQueue"
require "TimedActions/EFZ_OpenFloorPlanAction"
require "ISUI/ISInventoryPane"
require "EFZ_FloorPlan_Shared"

local MODDATA_KEY = (EFZ and EFZ.FloorPlan and EFZ.FloorPlan.MODDATA_KEY) or "EFZ_FloorPlanState"

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

local function getOpenedState()
    if ModData and ModData.getOrCreate then
        return ModData.getOrCreate(MODDATA_KEY)
    end
    return nil
end

local function isOpened(fullType)
    local state = getOpenedState()
    return state ~= nil and state[tostring(fullType)] == true
end

local function applyOpenedPlansIfPossible()
    if not (EFZ and EFZ.FloorPlan and EFZ.FloorPlan.OpenByFullType) then
        return
    end
    if isOpened("EFZ.LivingSpaceFloorPlanUpper") then
        EFZ.FloorPlan.OpenByFullType("EFZ.LivingSpaceFloorPlanUpper")
    end
    if isOpened("EFZ.LivingSpaceFloorPlanLower") then
        EFZ.FloorPlan.OpenByFullType("EFZ.LivingSpaceFloorPlanLower")
    end
end

local _floorPlanLoadHookAdded = false
local function ensureFloorPlanChunkHook()
    if _floorPlanLoadHookAdded then
        return
    end

    local major = getMajorBuild()
    local CHUNK_TILES = getChunkTileSize(major)
    local targetChunkWX = math.floor(20396 / CHUNK_TILES)
    local targetChunkWY = math.floor(5 / CHUNK_TILES)

    if major >= 42 and Events and Events.LoadChunk and type(Events.LoadChunk.Add) == "function" then
        Events.LoadChunk.Add(function(chunk)
            if not chunk then return end
            local wx = chunk.wx
            local wy = chunk.wy
            if wx == nil and chunk.getX then wx = chunk:getX() end
            if wy == nil and chunk.getY then wy = chunk:getY() end
            if wx == nil and chunk.getWX then wx = chunk:getWX() end
            if wy == nil and chunk.getWY then wy = chunk:getWY() end
            if wx == nil or wy == nil then return end
            if wx == targetChunkWX and wy == targetChunkWY then
                applyOpenedPlansIfPossible()
            end
        end)
        _floorPlanLoadHookAdded = true
        return
    end

    -- B41 fallback: per-square load event
    if Events and Events.LoadGridsquare and type(Events.LoadGridsquare.Add) == "function" then
        Events.LoadGridsquare.Add(function(square)
            if not square then return end
            local x = square:getX()
            local y = square:getY()
            local wx = math.floor(x / CHUNK_TILES)
            local wy = math.floor(y / CHUNK_TILES)
            if wx == targetChunkWX and wy == targetChunkWY then
                applyOpenedPlansIfPossible()
            end
        end)
        _floorPlanLoadHookAdded = true
    end
end

local function queueFloorPlanAction(playerObj, item)
    if not playerObj or not item then
        return
    end
    if ISTimedActionQueue and EFZ_OpenFloorPlanAction and ISTimedActionQueue.add then
        ISTimedActionQueue.add(EFZ_OpenFloorPlanAction:new(playerObj, item))
    end
end

local function onFillInventoryObjectContextMenu(playerNum, context, items)
    if not context or not items then
        return
    end

    local playerObj = getSpecificPlayer(playerNum)
    if not playerObj or playerObj:isDead() then
        return
    end

    local actualItems = ISInventoryPane.getActualItems(items)
    for _, item in ipairs(actualItems) do
        if item and item.getFullType then
            local ok, fullType = pcall(function()
                return item:getFullType()
            end)
            if ok and fullType == "EFZ.LivingSpaceFloorPlanUpper" then
                context:addOption(getText("ContextMenu_Open_LivingSpaceFloorPlanUpper"), item, function(it)
                    queueFloorPlanAction(playerObj, it)
                end)
            elseif ok and fullType == "EFZ.LivingSpaceFloorPlanLower" then
                context:addOption(getText("ContextMenu_Open_LivingSpaceFloorPlanLower"), item, function(it)
                    queueFloorPlanAction(playerObj, it)
                end)
            end
        end
    end
end

Events.OnFillInventoryObjectContextMenu.Add(onFillInventoryObjectContextMenu)

-- MP: 요청하면 서버가 ModData(transmit)로 개방 상태를 내려준다.
Events.OnGameStart.Add(function()
    ensureFloorPlanChunkHook()

    if isClient and isClient() and sendClientCommand then
        sendClientCommand("EFZ", "RequestFloorPlanState", {})

        -- State transmit가 늦게 도착해도, 이미 로드된 청크에 즉시 적용되도록 잠깐 재시도
        if Events and Events.OnTick and type(Events.OnTick.Add) == "function" then
            local ticks = 0
            local function retryTick()
                ticks = ticks + 1
                if ticks % 60 == 0 then
                    applyOpenedPlansIfPossible()
                end
                if ticks >= 600 then
                    if type(Events.OnTick.Remove) == "function" then
                        Events.OnTick.Remove(retryTick)
                    end
                end
            end
            Events.OnTick.Add(retryTick)
        end
    end

    -- SP/MP 공통: 시작 시점에도 한번 적용 시도(이미 로드된 경우)
    applyOpenedPlansIfPossible()
end)

local function onServerCommand(module, command, args)
    if module ~= "EFZ" then
        return
    end
    if command ~= "ApplyOpenFloorPlan" then
        return
    end
    local fullType = args and args.fullType or nil
    local state = getOpenedState()
    if state and fullType then
        state[tostring(fullType)] = true
    end
    if EFZ and EFZ.FloorPlan and EFZ.FloorPlan.OpenByFullType then
        EFZ.FloorPlan.OpenByFullType(fullType)
    end
end

Events.OnServerCommand.Add(onServerCommand)



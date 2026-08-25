if not EFZ then
    EFZ = {}
end

local function isIsoPlayer(obj)
    return obj ~= nil and instanceof(obj, "IsoPlayer")
end

local function resolvePlayer(obj)
    if isIsoPlayer(obj) then
        return obj
    end
    if type(obj) == "table" and isIsoPlayer(obj.player) then
        return obj.player
    end
    if type(getPlayer) == "function" then
        local p = getPlayer()
        if isIsoPlayer(p) then
            return p
        end
    end
    return nil
end

local function ensureFloorPlanLoaded()
    if EFZ and EFZ.FloorPlan and (EFZ.FloorPlan.OpenLower or EFZ.FloorPlan.OpenUpper) then
        return true
    end
    pcall(require, "EFZ_FloorPlan_Shared")
    return EFZ and EFZ.FloorPlan and (EFZ.FloorPlan.OpenLower or EFZ.FloorPlan.OpenUpper) ~= nil
end

local function tryQueueOpenAction(playerObj, item)
    if not playerObj or not item then
        return false
    end

    local okQueue = pcall(require, "TimedActions/ISTimedActionQueue")
    local okAction = pcall(require, "TimedActions/EFZ_OpenFloorPlanAction")
    if okQueue and okAction and ISTimedActionQueue and EFZ_OpenFloorPlanAction and ISTimedActionQueue.add then
        ISTimedActionQueue.add(EFZ_OpenFloorPlanAction:new(playerObj, item))
        return true
    end

    return false
end

function Open_LivingSpaceFloorPlanLower(food, player, percent)
    local targetPlayer = resolvePlayer(player)
    if tryQueueOpenAction(targetPlayer, food) then
        return
    end

    -- Fallback (server-only / no timed action queue available)
    ensureFloorPlanLoaded()
    if EFZ and EFZ.FloorPlan and EFZ.FloorPlan.OpenLower then
        EFZ.FloorPlan.OpenLower()
    end
end

function Open_LivingSpaceFloorPlanUpper(food, player, percent)
    local targetPlayer = resolvePlayer(player)
    if tryQueueOpenAction(targetPlayer, food) then
        return
    end

    -- Fallback (server-only / no timed action queue available)
    ensureFloorPlanLoaded()
    if EFZ and EFZ.FloorPlan and EFZ.FloorPlan.OpenUpper then
        EFZ.FloorPlan.OpenUpper()
    end
end



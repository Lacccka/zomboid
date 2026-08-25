-- Helps Bandits NPCs climb through blocking windows while moving.

if isServer() then
    return
end

local tickCount = 0
local scanIds = {}
local scanIndex = 1
local lastAssistById = {}
local ASSIST_COOLDOWN_MS = 900
local MAX_BANDITS_PER_FAST_TICK = 8
local lastFenceAssistById = {}
local lastPosById = {}
local FENCE_STUCK_MS = 800
local FENCE_ASSIST_COOLDOWN_MS = 2500

local function distSqXY(ax, ay, bx, by)
    local dx = ax - bx
    local dy = ay - by
    return dx * dx + dy * dy
end

local function getBrain(obj)
    if not obj or not obj.getModData then
        return nil
    end
    if BanditBrain and BanditBrain.Get then
        local ok, brain = pcall(BanditBrain.Get, obj)
        if ok and brain then
            return brain
        end
    end
    local md = obj:getModData()
    return md and md.brain
end

local function isSister(obj)
    return ModpackFestivalSister
        and ModpackFestivalSister.isSisterBandit
        and ModpackFestivalSister.isSisterBandit(obj)
end

local function isWindowLike(obj)
    if not obj or not instanceof then
        return false
    end
    if instanceof(obj, "IsoWindow") or instanceof(obj, "IsoWindowFrame") then
        return true
    end
    return instanceof(obj, "IsoThumpable") and obj.isWindow and obj:isWindow()
end

local function getObjectNorth(obj)
    if obj and obj.getNorth then
        local ok, north = pcall(function() return obj:getNorth() end)
        if ok then
            return north == true
        end
    end
    if obj and obj.isNorth then
        local ok, north = pcall(function() return obj:isNorth() end)
        if ok then
            return north == true
        end
    end
    local props = obj and obj.getProperties and obj:getProperties()
    return props and props.has and IsoFlagType and IsoFlagType.WindowN
        and props:has(IsoFlagType.WindowN)
end

local function windowSeparatesNpcFromTarget(window, bandit, tx, ty)
    local sq = window and window.getSquare and window:getSquare()
    if not sq then
        return false
    end
    local wx = sq:getX()
    local wy = sq:getY()
    local bx = bandit:getX()
    local by = bandit:getY()
    if getObjectNorth(window) then
        return ((by < wy and ty >= wy) or (by >= wy and ty < wy))
            and math.abs(bx - wx) <= 1.75
    end
    return ((bx < wx and tx >= wx) or (bx >= wx and tx < wx))
        and math.abs(by - wy) <= 1.75
end

local function getCurrentMoveTarget(bandit)
    local brain = getBrain(bandit)
    if not brain or not brain.tasks then
        return nil
    end
    for _, task in pairs(brain.tasks) do
        if (task.action == "Move" or task.action == "GoTo") and task.x and task.y then
            return task.x, task.y, task.z or bandit:getZ() or 0
        end
    end
    return nil
end

local function findBlockingWindowNearBandit(bandit, tx, ty, tz)
    local sq = bandit and bandit.getSquare and bandit:getSquare()
    local cell = sq and sq.getCell and sq:getCell()
    if not cell then
        return nil
    end
    local bx = math.floor(bandit:getX())
    local by = math.floor(bandit:getY())
    local bz = math.floor((bandit:getZ() or 0) + 0.5)
    if math.abs((tz or bz) - bz) > 0.75 then
        return nil
    end
    for dx = -1, 1 do
        for dy = -1, 1 do
            local testSq = cell:getGridSquare(bx + dx, by + dy, bz)
            local objects = testSq and testSq.getObjects and testSq:getObjects()
            if objects then
                for i = 0, objects:size() - 1 do
                    local obj = objects:get(i)
                    if isWindowLike(obj) and windowSeparatesNpcFromTarget(obj, bandit, tx, ty) then
                        return obj
                    end
                end
            end
        end
    end
    return nil
end

local function canClimbThrough(window, bandit)
    if not window or not window.canClimbThrough then
        return false
    end
    local ok, result = pcall(function()
        return window:canClimbThrough(bandit)
    end)
    return ok and result == true
end

local function openOrBreakWindow(window, bandit)
    if not window or not instanceof then
        return
    end
    if instanceof(window, "IsoWindow") then
        pcall(function()
            if not window:IsOpen() and not window:isSmashed() then
                window:ToggleWindow(bandit)
            end
        end)
        if not canClimbThrough(window, bandit) then
            pcall(function()
                if not window:isSmashed() then
                    window:smashWindow()
                    local sq = window:getSquare()
                    if sq and sq.playSound then
                        sq:playSound("SmashWindow")
                    end
                end
            end)
        end
    elseif instanceof(window, "IsoThumpable") and window.isWindow and window:isWindow() then
        pcall(function()
            if window.ToggleWindow and not window:IsOpen() then
                window:ToggleWindow(bandit)
            end
        end)
        if not canClimbThrough(window, bandit) then
            pcall(function()
                if window.smashWindow then
                    window:smashWindow()
                end
            end)
        end
    end
end

local function climbThroughWindow(window, bandit)
    if not window or not bandit or not ClimbThroughWindowState then
        return false
    end
    if not canClimbThrough(window, bandit) then
        return false
    end
    local ok = pcall(function()
        ClimbThroughWindowState.instance():setParams(bandit, window)
        bandit:changeState(ClimbThroughWindowState.instance())
        if bandit.setBumpType then
            bandit:setBumpType("ClimbWindow")
        end
    end)
    return ok == true
end

local function assistBanditWindow(bandit, banditId)
    if not bandit or isSister(bandit) then
        return false
    end
    if bandit.isDead and bandit:isDead() then
        return false
    end
    if bandit.getVariableBoolean and not bandit:getVariableBoolean("Bandit") then
        return false
    end

    local tx, ty, tz = getCurrentMoveTarget(bandit)
    if not tx or not ty then
        return false
    end
    if distSqXY(bandit:getX(), bandit:getY(), tx, ty) < 2.25 then
        return false
    end

    local now = getTimestampMs and getTimestampMs() or 0
    if now - (lastAssistById[banditId] or 0) < ASSIST_COOLDOWN_MS then
        return false
    end

    local window = findBlockingWindowNearBandit(bandit, tx, ty, tz)
    if not window then
        return false
    end

    lastAssistById[banditId] = now
    openOrBreakWindow(window, bandit)
    if climbThroughWindow(window, bandit) then
        local brain = getBrain(bandit)
        if brain and brain.tasks then
            brain.tasks = {}
            if BanditBrain and BanditBrain.Update then
                pcall(BanditBrain.Update, bandit, brain)
            end
        end
        return true
    end
    return false
end

local function refreshScanIds()
    scanIds = {}
    scanIndex = 1
    if not BanditZombie or not BanditZombie.CacheLightB then
        return
    end
    for id in pairs(BanditZombie.CacheLightB) do
        table.insert(scanIds, id)
    end
end

local function isFenceObject(obj)
    if not obj then return false end
    -- isFence() is the most reliable check; walls return false here
    if obj.isFence then
        local ok, result = pcall(function() return obj:isFence() end)
        if ok and result then return true end
    end
    -- climbSheet flags are fence-specific; collideN/W are shared with interior walls so skip them
    local props = obj.getProperties and obj:getProperties()
    if not props or not props.has or not IsoFlagType then return false end
    return (IsoFlagType.climbSheetN and props:has(IsoFlagType.climbSheetN))
        or (IsoFlagType.climbSheetW and props:has(IsoFlagType.climbSheetW))
end

local function findBlockingFenceNearBandit(bandit, tx, ty)
    local cell = bandit.getCell and bandit:getCell()
    if not cell then return nil end
    local bx, by, bz = bandit:getX(), bandit:getY(), bandit:getZ() or 0
    -- scan 2-tile radius in direction of travel
    local dx = tx - bx
    local dy = ty - by
    local len = math.sqrt(dx * dx + dy * dy)
    if len == 0 then return nil end
    dx, dy = dx / len, dy / len
    for step = 1, 3 do
        local cx = math.floor(bx + dx * step + 0.5)
        local cy = math.floor(by + dy * step + 0.5)
        local sq = cell:getGridSquare(cx, cy, math.floor(bz + 0.5))
        if sq then
            local objs = sq.getObjects and sq:getObjects()
            if objs then
                for i = 0, objs:size() - 1 do
                    local obj = objs:get(i)
                    if isFenceObject(obj) then
                        return obj, cx, cy
                    end
                end
            end
        end
    end
    return nil
end

local function assistBanditFence(bandit, banditId)
    if not bandit or bandit.isDead and bandit:isDead() then return false end
    if bandit.getVariableBoolean and not bandit:getVariableBoolean("Bandit") then return false end

    local tx, ty = getCurrentMoveTarget(bandit)
    if not tx or not ty then
        lastPosById[banditId] = nil
        return false
    end

    local bx, by = bandit:getX(), bandit:getY()
    if distSqXY(bx, by, tx, ty) < 1.5 then
        lastPosById[banditId] = nil
        return false
    end

    local now = getTimestampMs and getTimestampMs() or 0
    if now - (lastFenceAssistById[banditId] or 0) < FENCE_ASSIST_COOLDOWN_MS then return false end

    local currDistSq = distSqXY(bx, by, tx, ty)
    local prev = lastPosById[banditId]
    if not prev then
        lastPosById[banditId] = { x = bx, y = by, t = now, distSq = currDistSq }
        return false
    end

    -- consider "making progress" only if she closed meaningful distance toward the target
    local madeprogress = currDistSq < prev.distSq - 0.3
    if madeprogress then
        lastPosById[banditId] = { x = bx, y = by, t = now, distSq = currDistSq }
        return false
    end

    if now - prev.t < FENCE_STUCK_MS then return false end

    -- bandit is stuck — look for a fence in the path
    local fence, fx, fy = findBlockingFenceNearBandit(bandit, tx, ty)
    if not fence then
        lastPosById[banditId] = { x = bx, y = by, t = now, distSq = currDistSq }
        return false
    end

    lastFenceAssistById[banditId] = now
    lastPosById[banditId] = nil

    -- try ClimbOverFenceState first; fall back to nudge teleport
    local climbed = false
    if ClimbOverFenceState then
        pcall(function()
            ClimbOverFenceState.instance():setParams(bandit, fence)
            bandit:changeState(ClimbOverFenceState.instance())
            climbed = true
        end)
    end
    if not climbed then
        -- nudge: move bandit to the tile on the far side of the fence
        local bz = bandit:getZ() or 0
        local dx = tx - bx
        local dy = ty - by
        local len = math.sqrt(dx * dx + dy * dy)
        if len > 0 then
            pcall(function()
                bandit:setX(bx + (dx / len) * 2.2)
                bandit:setY(by + (dy / len) * 2.2)
            end)
        end
    end
    return true
end

local function processBandits()
    if not BanditZombie or not BanditZombie.Cache then
        return
    end
    if #scanIds == 0 or scanIndex > #scanIds then
        refreshScanIds()
    end
    local processed = 0
    while processed < MAX_BANDITS_PER_FAST_TICK and scanIndex <= #scanIds do
        local id = scanIds[scanIndex]
        scanIndex = scanIndex + 1
        processed = processed + 1
        local bandit = BanditZombie.Cache[id]
            or (BanditZombie.GetInstanceById and BanditZombie.GetInstanceById(id))
        if bandit then
            assistBanditWindow(bandit, id)
            assistBanditFence(bandit, id)
        end
    end
end

local function onTick()
    tickCount = tickCount + 1
    if ModpackFestivalTick.every(tickCount, ModpackFestivalTick.GAME) then
        refreshScanIds()
    end
    if not ModpackFestivalTick.every(tickCount, ModpackFestivalTick.UI_FAST) then
        return
    end
    processBandits()
end

Events.OnTick.Add(onTick)
print("[ModpackFestivalSpawn] Bandits window assist loaded")

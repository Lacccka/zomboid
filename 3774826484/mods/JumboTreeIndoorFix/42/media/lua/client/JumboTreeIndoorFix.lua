
local SCAN_RADIUS = 24
local RESCAN_INTERVAL = 30
local VEHICLE_RESCAN_INTERVAL = 10
local PATH_SAMPLE_STEP = 1
local LATERAL_HALF = 3
local DEPTH_XL = 12
local DEPTH_XXL = 16
local DEPTH_L = 12
local RESTORE_BEHIND_DIST = 20
local WALK_RESCAN_INTERVAL = 20
local WALK_RELEASE_MARGIN = 4
local VEHICLE_STOP_SPEED = 10.0
local VEHICLE_FORWARD_PER_KMH_STRONG = 0.5
local VEHICLE_FORWARD_MAX_STRONG = 36
local VEHICLE_FORWARD_PER_KMH_WEAK = 0.25
local VEHICLE_FORWARD_MAX_WEAK = 18
local RESTORE_INTERVAL = 2
local RESTORE_PER_STEP = 1

local hiddenTrees = {}
local indoorState = {}
local lastVehiclePos = {}
local lastWalkPos = {}
local wasMoving = {}
local stoppedQueue = {}
local tickCounter = 0

local config = { enabled = true, drive = true, lookAheadLevel = 1, walk = true, walkScale = 1.0,
    fadeStyle = true, walkIncludeL = false }
local styleDirty = false

local options = PZAPI.ModOptions:create("JumboTreeIndoorFix", getText("UI_JTIF_ModName"))
local enabledBox = options:addTickBox("enabled", getText("UI_JTIF_Enabled"), true,
    getText("UI_JTIF_Enabled_TT"))
local driveBox = options:addTickBox("vehicleEnabled", getText("UI_JTIF_Drive"), true,
    getText("UI_JTIF_Drive_TT"))
local lookAheadCombo = options:addComboBox("vehicleLookAhead", getText("UI_JTIF_LookAhead"),
    getText("UI_JTIF_LookAhead_TT"))
lookAheadCombo:addItem("UI_JTIF_LookAhead_Long", true)
lookAheadCombo:addItem("UI_JTIF_LookAhead_Short", false)
local walkBox = options:addTickBox("walkEnabled", getText("UI_JTIF_Walk"), true,
    getText("UI_JTIF_Walk_TT"))
local walkRangeCombo = options:addComboBox("walkRange", getText("UI_JTIF_WalkRange"),
    getText("UI_JTIF_WalkRange_TT"))
walkRangeCombo:addItem("UI_JTIF_WalkRange_Standard", true)
walkRangeCombo:addItem("UI_JTIF_WalkRange_Narrow", false)
walkRangeCombo:addItem("UI_JTIF_WalkRange_Minimal", false)
local walkIncludeLBox = options:addTickBox("walkIncludeL", getText("UI_JTIF_WalkIncludeL"), false,
    getText("UI_JTIF_WalkIncludeL_TT"))
local styleCombo = options:addComboBox("hideStyle", getText("UI_JTIF_HideStyle"),
    getText("UI_JTIF_HideStyle_TT"))
styleCombo:addItem("UI_JTIF_HideStyle_Fade", true)
styleCombo:addItem("UI_JTIF_HideStyle_Trunk", false)
local WALK_RANGE_SCALE = { 1.0, 0.66, 0.33 }
options.apply = function(self)
    config.enabled = enabledBox:getValue()
    config.drive = driveBox:getValue()
    config.lookAheadLevel = lookAheadCombo:getValue()
    config.walk = walkBox:getValue()
    config.walkScale = WALK_RANGE_SCALE[walkRangeCombo:getValue()] or 1.0
    local fade = styleCombo:getValue() == 1
    local includeL = walkIncludeLBox:getValue()
    if includeL ~= config.walkIncludeL then
        config.walkIncludeL = includeL
        styleDirty = true
    end
    if fade ~= config.fadeStyle then
        config.fadeStyle = fade
        styleDirty = true
    end
end

local function isJumboTree(tree)
    local sprite = tree:getSprite()
    local name = sprite and sprite:getName()
    return name and string.find(name, "JUMBO", 1, true) ~= nil
end

local function isJumboXLTree(tree)
    local sprite = tree:getSprite()
    local name = sprite and sprite:getName()
    return name and string.find(name, "JUMBOX", 1, true) ~= nil
end

local function trunkSpriteNameFor(name)
    local base, idx = string.match(name, "^(.*JUMBOXXL_1_)(%d+)$")
    if not base then
        base, idx = string.match(name, "^(.*JUMBOXL_1_)(%d+)$")
    end
    if not base then return nil end
    local i = tonumber(idx)
    if not i or i > 5 then return nil end
    local t = (i == 0 and 12) or (i == 1 and 13) or 14
    return base .. t
end

local fadeTextureKnown = {}
local function fadeSpriteNameFor(name)
    local maxIdx = 5
    local base, idx = string.match(name, "^(.*JUMBOXXL_1_)(%d+)$")
    if not base then
        base, idx = string.match(name, "^(.*JUMBOXL_1_)(%d+)$")
    end
    if not base then
        base, idx = string.match(name, "^(.*JUMBO_1_)(%d+)$")
        if base then maxIdx = 11 end
    end
    if not base then return nil end
    local i = tonumber(idx)
    if not i or i > maxIdx then return nil end
    local fname = base .. (20 + i)
    local known = fadeTextureKnown[fname]
    if known == nil then
        known = getTexture(fname) ~= nil
        fadeTextureKnown[fname] = known
        if not known then
        end
    end
    if known then return fname end
    return trunkSpriteNameFor(name)
end

local fadePropsCopied = {}
local function ensureFadeProps(fadeName, fadeSprite, sourceSprite)
    if fadePropsCopied[fadeName] then return end
    if not (fadeSprite and sourceSprite) then return end
    local tp = fadeSprite:getProperties()
    local sp = sourceSprite:getProperties()
    if tp and sp then
        tp:AddProperties(sp)
        fadePropsCopied[fadeName] = true
    end
end

local FADE_SPECIES = {
    "e_redmaple", "e_easternredbud", "e_dogwood", "e_cockspurhawthorn",
    "e_carolinasilverbell", "e_americanlinden", "e_canadianhemlock",
    "e_americanholly", "e_yellowwood", "e_virginiapine", "e_riverbirch",
}
local FADE_TYPES = { { "JUMBOXL", 5 }, { "JUMBOXXL", 5 }, { "JUMBO", 11 } }
local prewarmDone = false
local function prewarmFadeTextures()
    if prewarmDone then return end
    prewarmDone = true
    Events.OnPostUIDraw.Remove(prewarmFadeTextures)
    local count = 0
    for _, sp in ipairs(FADE_SPECIES) do
        for _, tydef in ipairs(FADE_TYPES) do
            local ty, maxIdx = tydef[1], tydef[2]
            for i = 0, maxIdx do
                local fname = sp .. ty .. "_1_" .. (20 + i)
                local tex = getTexture(fname)
                fadeTextureKnown[fname] = tex ~= nil
                if tex then
                    UIManager.DrawTexture(tex, -10000, -10000, 1, 1, 1)
                    count = count + 1
                end
            end
        end
    end
end
Events.OnPostUIDraw.Add(prewarmFadeTextures)

local function restoreEntry(entry)
    if entry.orig then
        local sprite = getSprite(entry.orig)
        if sprite then
            entry.tree:setSprite(sprite)
        end
        if entry.attachedNames then
            entry.tree:clearAttachedAnimSprite()
            for _, an in ipairs(entry.attachedNames) do
                if an ~= "" and getSprite(an) then
                    entry.tree:addAttachedAnimSpriteByName(an)
                end
            end
        end
        entry.tree:invalidateRenderChunkLevel(FBORenderChunk.DIRTY_REDRAW)
    elseif not entry.tree:getDoRender() then
        entry.tree:setDoRender(true)
        entry.tree:invalidateRenderChunkLevel(FBORenderChunk.DIRTY_REDRAW)
    end
end

local function applyTrunkEntry(obj, name, tx, ty, mapper)
    mapper = mapper or trunkSpriteNameFor
    local entry = { tree = obj, x = tx, y = ty }
    local trunkName = mapper(name)
    local trunkSprite = trunkName and getSprite(trunkName)
    if trunkSprite then
        if string.match(trunkName, "_1_[23]%d$") then
            ensureFadeProps(trunkName, trunkSprite, obj:getSprite())
        end
        local attached = obj:getAttachedAnimSprite()
        if attached and attached:size() > 0 then
            local attachedNames = {}
            for ai = 0, attached:size() - 1 do
                local inst = attached:get(ai)
                local ps = inst and inst:getParentSprite()
                attachedNames[#attachedNames + 1] = (ps and ps:getName()) or ""
            end
            entry.attachedNames = attachedNames
        end
        entry.orig = name
        obj:setSprite(trunkSprite)
        if entry.attachedNames then
            obj:clearAttachedAnimSprite()
            for _, an in ipairs(entry.attachedNames) do
                local at = mapper(an)
                if at and getSprite(at) then
                    obj:addAttachedAnimSpriteByName(at)
                end
            end
        end
    else
        obj:setDoRender(false)
    end
    obj:invalidateRenderChunkLevel(FBORenderChunk.DIRTY_REDRAW)
    return entry, (trunkSprite == nil), (trunkName == nil)
end

local function surveyTrees(player)
    local cell = getCell()
    local px = math.floor(player:getX())
    local py = math.floor(player:getY())
    local parts = {}
    local total = 0
    for dx = -SCAN_RADIUS, SCAN_RADIUS do
        for dy = -SCAN_RADIUS, SCAN_RADIUS do
            local sq = cell:getGridSquare(px + dx, py + dy, 0)
            if sq then
                local objects = sq:getObjects()
                for i = 0, objects:size() - 1 do
                    local obj = objects:get(i)
                    if instanceof(obj, "IsoTree") then
                        total = total + 1
                        if total <= 40 then
                            local sprite = obj:getSprite()
                            local name = (sprite and sprite:getName()) or "?"
                            parts[#parts + 1] = name .. "@" .. (px + dx) .. "," .. (py + dy) .. "#" .. i
                        end
                    end
                end
            end
        end
    end
end

local SB_SPRITES = {
    ["steamboat_0"] = true, ["steamboat_1"] = true, ["steamboat_2"] = true,
    ["steamboat_3"] = true, ["steamboat_4"] = true, ["steamboat_5"] = true,
    ["steamboat_6"] = true, ["steamboat_8"] = true, ["steamboat_9"] = true,
    ["steamboat_10"] = true, ["steamboat_11"] = true, ["steamboat_12"] = true,
    ["steamboat_13"] = true, ["steamboat_16"] = true, ["steamboat_17"] = true,
    ["steamboat_18"] = true, ["steamboat_19"] = true,
}

local function hideSteamboatAround(playerNum, player, radius)
    local cell = getCell()
    local px = math.floor(player:getX())
    local py = math.floor(player:getY())
    local applied = hiddenTrees[playerNum]
    for dz = 0, 1 do
        for dx = -radius, radius do
            for dy = -radius, radius do
                local sq = cell:getGridSquare(px + dx, py + dy, dz)
                if sq then
                    local objects = sq:getObjects()
                    for i = 0, objects:size() - 1 do
                        local obj = objects:get(i)
                        local sprite = obj:getSprite()
                        local name = sprite and sprite:getName()
                        if name and SB_SPRITES[name] then
                            local key = "sb:" .. (px + dx) .. ":" .. (py + dy) .. ":" .. dz .. ":" .. i
                            applied[key] = { tree = obj, x = px + dx, y = py + dy }
                            if obj:getDoRender() then
                                obj:setDoRender(false)
                                obj:invalidateRenderChunkLevel(FBORenderChunk.DIRTY_REDRAW)
                            end
                        end
                    end
                end
            end
        end
    end
end

local SB_ROOMS = { restaurantkitchen = true, hall = true }
local function isInSteamboatRoom(player)
    local sq = player:getSquare()
    local room = sq and sq:getRoom()
    if not room then return false end
    return SB_ROOMS[room:getName()] == true
end

local function restoreSteamboat(playerNum)
    local applied = hiddenTrees[playerNum]
    if not applied then return end
    for key, entry in pairs(applied) do
        if string.sub(key, 1, 3) == "sb:" then
            restoreEntry(entry)
            applied[key] = nil
        end
    end
end

local function hideTreesAround(playerNum, player, radius, isTarget)
    local cell = getCell()
    local px = math.floor(player:getX())
    local py = math.floor(player:getY())
    local applied = hiddenTrees[playerNum]
    local newCount = 0
    local newNames = {}
    for dx = -radius, radius do
        for dy = -radius, radius do
            local sq = cell:getGridSquare(px + dx, py + dy, 0)
            if sq then
                local objects = sq:getObjects()
                for i = 0, objects:size() - 1 do
                    local obj = objects:get(i)
                    if instanceof(obj, "IsoTree") and isTarget(obj) then
                        local key = (px + dx) .. ":" .. (py + dy) .. ":" .. i
                        applied[key] = { tree = obj, x = px + dx, y = py + dy }
                        if obj:getDoRender() then
                            obj:setDoRender(false)
                            obj:invalidateRenderChunkLevel(FBORenderChunk.DIRTY_REDRAW)
                            newCount = newCount + 1
                            if newCount <= 10 then
                                local sprite = obj:getSprite()
                                newNames[#newNames + 1] = ((sprite and sprite:getName()) or "?")
                                    .. "@" .. (px + dx) .. "," .. (py + dy) .. "#" .. i
                            end
                        end
                    end
                end
            end
        end
    end
    if newCount > 0 then
    end
end

local POLE_SPRITES = {}
for _, i in ipairs({ 88, 89, 90, 91, 92, 93, 94, 95, 142, 143 }) do
    POLE_SPRITES["appliances_com_01_" .. i] = true
end

local function hasPoleBehindNW(tx, ty)
    local cell = getCell()
    for dx = -3, 0 do
        for dy = -3, 0 do
            local sq = cell:getGridSquare(tx + dx, ty + dy, 0)
            if sq then
                local objects = sq:getObjects()
                for i = 0, objects:size() - 1 do
                    local sprite = objects:get(i):getSprite()
                    local name = sprite and sprite:getName()
                    if name and (POLE_SPRITES[name]
                            or string.find(name, "electricity_pylon", 1, true) == 1) then
                        return true
                    end
                end
            end
        end
    end
    return false
end

local function hideTreesAlongPath(playerNum, player, fx, fy, flen)
    local cell = getCell()
    local px = math.floor(player:getX())
    local py = math.floor(player:getY())
    local applied = hiddenTrees[playerNum]
    local newCount = 0
    local fallbackCount = 0
    local fbPattern = 0
    local fbSprite = 0
    local newNames = {}
    local samples = { { x = px, y = py }, { x = px + 1, y = py } }
    if fx and flen and flen > 0 then
        local d = PATH_SAMPLE_STEP
        while d <= flen do
            samples[#samples + 1] = {
                x = math.floor(px + fx * d + 0.5),
                y = math.floor(py + fy * d + 0.5),
            }
            d = d + PATH_SAMPLE_STEP
        end
    end
    local visited = {}
    for k = 0, DEPTH_XXL do
        for s = 1, #samples do
            local sp = samples[s]
            for a = -LATERAL_HALF, LATERAL_HALF do
                local tx, ty = sp.x + k + a, sp.y + k - a
                local vkey = tx .. ":" .. ty
                if not visited[vkey] then
                    visited[vkey] = true
                    local sq = cell:getGridSquare(tx, ty, 0)
                    if sq then
                        local objects = sq:getObjects()
                        for oi = 0, objects:size() - 1 do
                            local obj = objects:get(oi)
                            if instanceof(obj, "IsoTree") and isJumboXLTree(obj) then
                                local sprite = obj:getSprite()
                                local name = (sprite and sprite:getName()) or ""
                                local reach = (string.find(name, "JUMBOXXL", 1, true) and DEPTH_XXL) or DEPTH_XL
                                if k <= reach then
                                    local key = tx .. ":" .. ty .. ":" .. oi
                                    local existing = applied[key]
                                    if not (existing and existing.tree == obj) then
                                        local mapper = config.fadeStyle and fadeSpriteNameFor or nil
                                        if mapper and hasPoleBehindNW(tx, ty) then
                                            mapper = nil
                                        end
                                        local entry, fb, fbPat = applyTrunkEntry(obj, name, tx, ty, mapper)
                                        if fb then
                                            fallbackCount = fallbackCount + 1
                                            if fbPat then
                                                fbPattern = fbPattern + 1
                                            else
                                                fbSprite = fbSprite + 1
                                            end
                                        end
                                        applied[key] = entry
                                        newCount = newCount + 1
                                        if newCount <= 10 then
                                            newNames[#newNames + 1] = name .. "@" .. tx .. "," .. ty .. "#" .. oi
                                        end
                                    end
                                end
                            end
                        end
                    end
                end
            end
        end
    end
    if newCount > 0 then
    end
    local releaseKeys = {}
    if fx then
        for key, entry in pairs(applied) do
            if not visited[entry.x .. ":" .. entry.y] then
                local ddx, ddy = entry.x - px, entry.y - py
                if ddx * fx + ddy * fy < 0
                        and ddx * ddx + ddy * ddy > RESTORE_BEHIND_DIST * RESTORE_BEHIND_DIST then
                    releaseKeys[#releaseKeys + 1] = key
                end
            end
        end
    end
    for _, key in ipairs(releaseKeys) do
        restoreEntry(applied[key])
        applied[key] = nil
    end
    if #releaseKeys > 0 then
    end
end

local function hideTreesWalk(playerNum, player)
    local cx, cy = player:getX(), player:getY()
    local lastPos = lastWalkPos[playerNum]
    lastWalkPos[playerNum] = { x = cx, y = cy }
    if lastPos then
        local mx, my = cx - lastPos.x, cy - lastPos.y
        if mx * mx + my * my < 0.25 then
            local walkApplied = hiddenTrees[playerNum]
            if walkApplied then
                for _, entry in pairs(walkApplied) do
                    restoreEntry(entry)
                end
                hiddenTrees[playerNum] = {}
            end
            return
        end
    end
    local cell = getCell()
    local px = math.floor(player:getX())
    local py = math.floor(player:getY())
    local applied = hiddenTrees[playerNum]
    local newCount = 0
    local fallbackCount = 0
    local scale = config.walkScale or 1.0
    local rMax = math.floor(DEPTH_XXL * scale)
    local includeL = config.walkIncludeL and config.fadeStyle
    for dx = -rMax, rMax do
        local adx = (dx < 0) and -dx or dx
        for dy = -rMax, rMax do
            local ady = (dy < 0) and -dy or dy
            local sq = cell:getGridSquare(px + dx, py + dy, 0)
            if sq then
                local objects = sq:getObjects()
                for oi = 0, objects:size() - 1 do
                    local obj = objects:get(oi)
                    local isBig = instanceof(obj, "IsoTree") and isJumboXLTree(obj)
                    local isL = (not isBig) and includeL and instanceof(obj, "IsoTree")
                        and isJumboTree(obj)
                    if isBig or isL then
                        local sprite = obj:getSprite()
                        local name = (sprite and sprite:getName()) or ""
                        local reach = DEPTH_L
                        if string.find(name, "JUMBOXXL", 1, true) then
                            reach = DEPTH_XXL
                        elseif string.find(name, "JUMBOXL", 1, true) then
                            reach = DEPTH_XL
                        end
                        local r = math.floor(reach * scale)
                        if adx <= r and ady <= r then
                            local key = (px + dx) .. ":" .. (py + dy) .. ":" .. oi
                            local existing = applied[key]
                            local mapper = config.fadeStyle and fadeSpriteNameFor or nil
                            if mapper and not (existing and existing.tree == obj)
                                and hasPoleBehindNW(px + dx, py + dy) then
                                mapper = nil
                            end
                            if not (existing and existing.tree == obj) then
                                local entry, fb = applyTrunkEntry(obj, name, px + dx, py + dy, mapper)
                                if entry then entry.reach = r end
                                if fb then
                                    fallbackCount = fallbackCount + 1
                                end
                                applied[key] = entry
                                newCount = newCount + 1
                            end
                        end
                    end
                end
            end
        end
    end
    local releaseKeys = {}
    for key, entry in pairs(applied) do
        local releaseLimit = (entry.reach or rMax) + WALK_RELEASE_MARGIN
        local ddx = entry.x - px
        local ddy = entry.y - py
        if ddx < 0 then ddx = -ddx end
        if ddy < 0 then ddy = -ddy end
        if ddx > releaseLimit or ddy > releaseLimit then
            releaseKeys[#releaseKeys + 1] = key
        end
    end
    local released = 0
    for _, key in ipairs(releaseKeys) do
        restoreEntry(applied[key])
        applied[key] = nil
        released = released + 1
    end
    if newCount > 0 or released > 0 then
    end
end

local function buildRestoreQueue(playerNum, player)
    local applied = hiddenTrees[playerNum]
    if not applied then
        stoppedQueue[playerNum] = nil
        return
    end
    local px, py = player:getX(), player:getY()
    local sorted = {}
    for key, entry in pairs(applied) do
        local dx, dy = entry.x - px, entry.y - py
        sorted[#sorted + 1] = { key = key, d = dx * dx + dy * dy }
    end
    table.sort(sorted, function(a, b) return a.d < b.d end)
    local queue = { pos = 1 }
    for idx = 1, #sorted do
        queue[idx] = sorted[idx].key
    end
    stoppedQueue[playerNum] = queue
end

local function restoreSomeTrees(playerNum, maxCount)
    local applied = hiddenTrees[playerNum]
    if not applied then return end
    local count = 0
    local queue = stoppedQueue[playerNum]
    if queue then
        while count < maxCount do
            local key = queue[queue.pos]
            if not key then break end
            queue.pos = queue.pos + 1
            local entry = applied[key]
            if entry then
                restoreEntry(entry)
                applied[key] = nil
                count = count + 1
            end
        end
    end
    if count >= maxCount then return end
    local keys = {}
    for key in pairs(applied) do
        keys[#keys + 1] = key
        if #keys >= maxCount - count then break end
    end
    for _, key in ipairs(keys) do
        restoreEntry(applied[key])
        applied[key] = nil
    end
end

local function restoreTrees(playerNum)
    stoppedQueue[playerNum] = nil
    local applied = hiddenTrees[playerNum]
    if not applied then return end
    local count = 0
    for _, entry in pairs(applied) do
        restoreEntry(entry)
        count = count + 1
    end
    hiddenTrees[playerNum] = {}
    if count > 0 then
    end
end

local function vehicleForward(playerNum, player, speed)
    local cx, cy = player:getX(), player:getY()
    local last = lastVehiclePos[playerNum]
    lastVehiclePos[playerNum] = { x = cx, y = cy }
    if not last then return nil end
    local mx, my = cx - last.x, cy - last.y
    local mlen = math.sqrt(mx * mx + my * my)
    if mlen < 0.2 then return nil end
    local perKmh = (config.lookAheadLevel == 2) and VEHICLE_FORWARD_PER_KMH_WEAK or VEHICLE_FORWARD_PER_KMH_STRONG
    local maxLen = (config.lookAheadLevel == 2) and VEHICLE_FORWARD_MAX_WEAK or VEHICLE_FORWARD_MAX_STRONG
    return mx / mlen, my / mlen, math.min(speed * perKmh, maxLen)
end

local function onTick()
    if not config.enabled then
        for i = 0, getNumActivePlayers() - 1 do
            restoreTrees(i)
            indoorState[i] = nil
        end
        return
    end
    if styleDirty then
        styleDirty = false
        for i = 0, getNumActivePlayers() - 1 do
            restoreTrees(i)
        end
    end
    tickCounter = tickCounter + 1
    for i = 0, getNumActivePlayers() - 1 do
        local player = getSpecificPlayer(i)
        if player and not player:isDead() then
            local indoor = not player:isOutside()
            local inVehicle = player:getVehicle() ~= nil
            local speed = inVehicle and math.abs(player:getVehicle():getCurrentSpeedKmHour()) or 0
            local moving = inVehicle and speed >= VEHICLE_STOP_SPEED
            if not moving then
                lastVehiclePos[i] = nil
            end
            if inVehicle and moving then
                wasMoving[i] = true
            elseif wasMoving[i] then
                wasMoving[i] = nil
                buildRestoreQueue(i, player)
            end
            local mode = indoor and "indoor"
                or (inVehicle and (config.drive and "vehicle" or "idle")
                    or (config.walk and "walk" or "idle"))
            if mode ~= indoorState[i] then
                indoorState[i] = mode
                restoreTrees(i)
                hiddenTrees[i] = hiddenTrees[i] or {}
                if mode == "indoor" then
                    surveyTrees(player)
                    hideTreesAround(i, player, SCAN_RADIUS, isJumboTree)
                    if math.floor(player:getZ()) == 0 and isInSteamboatRoom(player) then
                        hideSteamboatAround(i, player, SCAN_RADIUS)
                    end
                elseif mode == "vehicle" then
                    if moving then
                        local fx, fy, flen = vehicleForward(i, player, speed)
                        hideTreesAlongPath(i, player, fx, fy, flen)
                    end
                elseif mode == "walk" then
                    hideTreesWalk(i, player)
                end
            else
                if mode == "vehicle" and not moving then
                    if tickCounter % RESTORE_INTERVAL == 0 then
                        restoreSomeTrees(i, RESTORE_PER_STEP)
                    end
                elseif mode == "vehicle" and tickCounter % VEHICLE_RESCAN_INTERVAL == 0 then
                    local fx, fy, flen = vehicleForward(i, player, speed)
                    hideTreesAlongPath(i, player, fx, fy, flen)
                elseif mode == "indoor" and tickCounter % RESCAN_INTERVAL == 0 then
                    hideTreesAround(i, player, SCAN_RADIUS, isJumboTree)
                    if math.floor(player:getZ()) == 0 and isInSteamboatRoom(player) then
                        hideSteamboatAround(i, player, SCAN_RADIUS)
                    else
                        restoreSteamboat(i)
                    end
                elseif mode == "walk" and tickCounter % WALK_RESCAN_INTERVAL == 0 then
                    hideTreesWalk(i, player)
                end
            end
        elseif indoorState[i] then
            restoreTrees(i)
            indoorState[i] = nil
        end
    end
end


local function onGameStart()
    hiddenTrees = {}
    indoorState = {}
    tickCounter = 0
end

local function onSaveGuard()
    for i = 0, getNumActivePlayers() - 1 do
        restoreTrees(i)
    end
end

Events.OnGameStart.Add(onGameStart)
Events.OnTick.Add(onTick)
Events.OnSave.Add(onSaveGuard)
Events.OnMainMenuEnter.Add(function() options:apply() end)

require "ExtractionMode/Config"
require "ExtractionMode/Util"
require "ExtractionMode/Garage"

ExtractionMode = ExtractionMode or {}

local Config = ExtractionMode.Config
local Util = ExtractionMode.Util
local Garage = ExtractionMode.Garage
local Authority = {}

local lastDoor = nil
local lastUnlocked = nil
local loggedMissing = false
local loggedMissingGarageMap = false

local GATE_KIND_KEY = "ExtractionModeGarageGateKind"
local GATE_WALL = "wall"
local GATE_DOOR = "door"
local GATE_FRAME = "frame"
local GATE_FRAME_VERSION_KEY = "ExtractionModeGarageFrameVersion"
local GATE_FRAME_VERSION = 1
local GATE_DOOR_SPRITE_VERSION_KEY = "ExtractionModeGarageDoorSpriteVersion"
local GATE_DOOR_SPRITE_VERSION = 2

local function garageDoorOnSquare(square)
    if square == nil then return nil end
    local objects = square:getObjects()
    if objects == nil then return nil end
    for index = 0, objects:size() - 1 do
        local object = objects:get(index)
        if object ~= nil and instanceof ~= nil then
            if instanceof(object, "IsoDoor") then return object end
            if instanceof(object, "IsoThumpable") then
                local door = false
                pcall(function() door = object:isDoor() == true end)
                if door then return object end
            end
        end
    end
    return nil
end

local function objectGateKind(object)
    if object == nil then return nil end
    local value = nil
    pcall(function() value = object:getModData()[GATE_KIND_KEY] end)
    return value
end

local function objectsOnSquare(square)
    local result = {}
    local objects = square and square:getObjects() or nil
    if objects == nil then return result end
    for index = 0, objects:size() - 1 do result[#result + 1] = objects:get(index) end
    return result
end

local function loadedGarageDoor()
    local cell = getCell and getCell() or nil
    if cell == nil then return nil end
    local point = Config.hideoutGarageDoor()
    local x, y, z = math.floor(point.x), math.floor(point.y), math.floor(point.z)
    -- The authored edge joins (131,137) to (132,137), and PZ stores the
    -- IsoDoor on the east square. Exact lookup avoids accidentally selecting
    -- another nearby door in the garage's dense 3x3 doorway area.
    local square = cell:getGridSquare(x, y, z)
    return garageDoorOnSquare(square), square
end

local function doorOpen(door)
    local open = false
    pcall(function() open = door:IsOpen() == true end)
    return open
end

local function removeObject(square, object)
    if square == nil or object == nil then return false end
    pcall(function() square:transmitRemoveItemFromSquare(object) end)
    pcall(function()
        if object:getSquare() ~= nil then object:removeFromWorld() end
    end)
    pcall(function()
        if object:getSquare() ~= nil then object:removeFromSquare() end
    end)
    return true
end

local function isDoorOrWindow(object)
    local excluded = false
    pcall(function()
        excluded = instanceof(object, "IsoDoor") or instanceof(object, "IsoWindow")
            or (instanceof(object, "IsoThumpable") and object:isDoor())
    end)
    return excluded
end

-- Only match a genuine wall on the doorway's own edge. The old fallback also
-- accepted collideW/collideN and searched neighboring squares, which allowed
-- furniture (including an industry_trucks tile) to become the garage wall.
local function authoredWallOnSquare(square, north)
    for _, object in ipairs(objectsOnSquare(square)) do
        if objectGateKind(object) == nil and not isDoorOrWindow(object) then
            local matches = false
            pcall(function()
                local sprite = object:getSprite()
                local properties = sprite and sprite:getProperties() or nil
                if properties ~= nil then
                    if north then
                        matches = properties:has(IsoFlagType.WallN)
                            or properties:has(IsoFlagType.WallNW)
                    else
                        matches = properties:has(IsoFlagType.WallW)
                            or properties:has(IsoFlagType.WallNW)
                    end
                end
            end)
            if matches then return object end
        end
    end
    return nil
end

local function objectSpriteName(object)
    local spriteName = nil
    pcall(function() spriteName = object and object:getSpriteName() or nil end)
    if spriteName == "" then return nil end
    return spriteName
end

local function loadedGarageMapMarker()
    local cell = getCell and getCell() or nil
    if cell == nil then return false, nil end
    local point = Config.hideoutGarageMarker()
    local square = cell:getGridSquare(math.floor(point.x), math.floor(point.y),
        math.floor(point.z))
    if square == nil then return false, nil end
    for _, object in ipairs(objectsOnSquare(square)) do
        if objectSpriteName(object) == Config.HIDEOUT_GARAGE_MARKER_SPRITE then
            return true, square
        end
    end
    return false, square
end

local function neighboringWallSprite(square, north)
    if square == nil then return nil end
    local cell = square:getCell()
    local x, y, z = square:getX(), square:getY(), square:getZ()
    -- Search along the same wall edge, never across the doorway. Matching uses
    -- the strict WallN/WallW flags above, so furniture cannot be selected.
    local offsets = north and {
        { -1, 0 }, { 1, 0 }, { -2, 0 }, { 2, 0 },
    } or {
        { 0, -1 }, { 0, 1 }, { 0, -2 }, { 0, 2 },
    }
    for _, offset in ipairs(offsets) do
        local candidate = authoredWallOnSquare(
            cell:getGridSquare(x + offset[1], y + offset[2], z), north)
        local spriteName = objectSpriteName(candidate)
        if spriteName ~= nil then return spriteName end
    end
    return nil
end

local function gateFrameOnSquare(square)
    for _, object in ipairs(objectsOnSquare(square)) do
        if objectGateKind(object) == GATE_FRAME then return object end
    end
    return nil
end

local function doorFrameSpriteForWall(wallSpriteName, north)
    if wallSpriteName == nil or getSprite == nil then return nil end
    local prefix, numberText = string.match(wallSpriteName, "^(.*_)(%d+)$")
    local number = tonumber(numberText)
    if prefix == nil or number == nil then return nil end

    -- Build 42 wall sheets place the matching doorway ten indices after each
    -- straight W/N wall variant (0->10, 1->11, 4->14, 5->15, and so on).
    local candidateName = prefix .. tostring(number + 10)
    local valid = false
    pcall(function()
        local sprite = getSprite(candidateName)
        local properties = sprite and sprite:getProperties() or nil
        valid = properties ~= nil and properties:has(
            north and "DoorWallN" or "DoorWallW")
    end)
    return valid and candidateName or nil
end

local function spawnDoorFrame(square, frameSpriteName)
    if square == nil or IsoThumpable == nil then return nil end
    if frameSpriteName == nil then return nil end
    local frame = nil
    pcall(function()
        local north = Config.HIDEOUT_GARAGE_DOOR_NORTH == true
        -- Match ISWoodenDoorFrame:create(): constructed door frames are
        -- pass-through IsoThumpables in the special-object wall layer. A plain
        -- IsoObject appended as a tile renders over characters and furniture.
        frame = IsoThumpable.new(square:getCell(), square,
            frameSpriteName, north, {})
        frame:getModData()[GATE_KIND_KEY] = GATE_FRAME
        frame:getModData()[GATE_FRAME_VERSION_KEY] = GATE_FRAME_VERSION
        frame:setName("Garage Door Frame")
        frame:setCanPassThrough(true)
        frame:setIsDoorFrame(true)
        frame:setIsThumpable(false)
        frame:setIsDismantable(false)
        frame:setCanBarricade(false)
        frame:setMaxHealth(1000000)
        frame:setHealth(1000000)
        square:AddSpecialObject(frame)
        square:RecalcAllWithNeighbours(true)
        frame:transmitCompleteItemToClients()
    end)
    return frame
end

local function spawnOpenDoor(square)
    if square == nil or IsoDoor == nil then return nil end
    local door = nil
    pcall(function()
        door = IsoDoor.new(square:getCell(), square,
            Config.HIDEOUT_GARAGE_DOOR_CLOSED_SPRITE,
            Config.HIDEOUT_GARAGE_DOOR_NORTH == true)
        local openSprite = getSprite and getSprite(Config.HIDEOUT_GARAGE_DOOR_OPEN_SPRITE) or nil
        if openSprite ~= nil then door:setOpenSprite(openSprite) end
        door:getModData()[GATE_KIND_KEY] = GATE_DOOR
        door:getModData()[GATE_DOOR_SPRITE_VERSION_KEY] = GATE_DOOR_SPRITE_VERSION
        door:setIsLocked(false)
        door:setLockedByKey(false)
        square:AddSpecialObject(door)
        -- IsoDoor:setOpen(true) only changes the state flag in this path; it
        -- can leave the closed sprite on screen. Use the normal silent toggle
        -- so state, sprite, collision, and pathing all transition together.
        door:ToggleDoorSilent()
        square:RecalcAllWithNeighbours(true)
        door:transmitCompleteItemToClients()
    end)
    return door
end

local function ensureLocked(square)
    local changed = false
    for _, object in ipairs(objectsOnSquare(square)) do
        local gateKind = objectGateKind(object)
        if garageDoorOnSquare(square) == object or gateKind == GATE_FRAME
            or gateKind == GATE_WALL then
            changed = removeObject(square, object) or changed
        end
    end
    -- Before the one-time unlock, the authored map wall is the complete gate.
    -- Do not manufacture a locked door or a thumpable blocker over it.
    local authoredWall = authoredWallOnSquare(square,
        Config.HIDEOUT_GARAGE_DOOR_NORTH == true)
    return changed, authoredWall
end

local function removeManagedGate(square)
    local changed = false
    for _, object in ipairs(objectsOnSquare(square)) do
        local gateKind = objectGateKind(object)
        if gateKind == GATE_DOOR or gateKind == GATE_FRAME or gateKind == GATE_WALL then
            changed = removeObject(square, object) or changed
        end
    end
    return changed
end

local function ensureUnlocked(square)
    local changed = false
    for _, object in ipairs(objectsOnSquare(square)) do
        if objectGateKind(object) == GATE_WALL then
            changed = removeObject(square, object) or changed
        end
    end
    -- Replace the map-authored wall segment with a real door frame before the
    -- IsoDoor is installed. This is what creates a navigable doorway rather
    -- than drawing a door entity over an intact collision wall.
    local north = Config.HIDEOUT_GARAGE_DOOR_NORTH == true
    local authoredWall = authoredWallOnSquare(square, north)
    local wallSpriteName = objectSpriteName(authoredWall)
        or neighboringWallSprite(square, north)
    local frameSpriteName = doorFrameSpriteForWall(wallSpriteName, north)
    if authoredWall ~= nil then
        changed = removeObject(square, authoredWall) or changed
    end
    local frame = gateFrameOnSquare(square)
    local properFrame = false
    if frame ~= nil then
        pcall(function()
            properFrame = instanceof(frame, "IsoThumpable")
                and frame:isDoorFrame()
                and frame:getModData()[GATE_FRAME_VERSION_KEY] == GATE_FRAME_VERSION
        end)
    end
    if frame ~= nil and (not properFrame or (frameSpriteName ~= nil
        and objectSpriteName(frame) ~= frameSpriteName)) then
        changed = removeObject(square, frame) or changed
        frame = nil
    end
    if frame == nil then
        frame = spawnDoorFrame(square, frameSpriteName)
        changed = frame ~= nil or changed
    end
    local door = garageDoorOnSquare(square)
    if door == nil then
        door = spawnOpenDoor(square)
        changed = door ~= nil or changed
    else
        local firstInstall = objectGateKind(door) ~= GATE_DOOR
        local spriteMigration = false
        pcall(function()
            spriteMigration = door:getModData()[GATE_DOOR_SPRITE_VERSION_KEY]
                ~= GATE_DOOR_SPRITE_VERSION
        end)
        pcall(function()
            door:getModData()[GATE_KIND_KEY] = GATE_DOOR
            door:getModData()[GATE_DOOR_SPRITE_VERSION_KEY] = GATE_DOOR_SPRITE_VERSION
            door:setIsLocked(false)
            door:setLockedByKey(false)
            local openSprite = getSprite and getSprite(
                Config.HIDEOUT_GARAGE_DOOR_OPEN_SPRITE) or nil
            if openSprite ~= nil then door:setOpenSprite(openSprite) end
            if (firstInstall or spriteMigration) and not doorOpen(door) then
                door:ToggleDoorSilent()
            end
            if firstInstall or spriteMigration then door:transmitCompleteItemToClients() end
        end)
        changed = firstInstall or spriteMigration or changed
    end
    return changed, door
end

function Authority.tick(root)
    if root == nil then return false end
    Garage.ensureState(root)
    local _, doorSquare = loadedGarageDoor()
    if doorSquare == nil then
        if not loggedMissing then
            loggedMissing = true
            local point = Config.hideoutGarageDoor()
            Util.log("Garage progression door is not loaded at " .. tostring(point.x)
                .. "," .. tostring(point.y) .. "," .. tostring(point.z))
        end
        lastDoor = nil
        return false
    end
    loggedMissing = false
    local garageMapPresent, markerSquare = loadedGarageMapMarker()
    if markerSquare == nil then
        -- The sentinel chunk is not loaded yet. Leave any existing doorway
        -- untouched and retry on the next authority tick.
        lastDoor = nil
        return false
    end
    if not garageMapPresent then
        if not loggedMissingGarageMap then
            loggedMissingGarageMap = true
            local point = Config.hideoutGarageMarker()
            Util.log("Garage progression door deferred: authored marker "
                .. tostring(Config.HIDEOUT_GARAGE_MARKER_SPRITE) .. " is missing at "
                .. tostring(point.x) .. "," .. tostring(point.y) .. "," .. tostring(point.z))
        end
        -- Clean up a door installed by an older version, but never remove an
        -- untagged map/user door merely because the garage sentinel is absent.
        local changed = removeManagedGate(doorSquare)
        lastDoor = nil
        lastUnlocked = nil
        return changed
    end
    if loggedMissingGarageMap then
        Util.log("Garage map marker detected; garage door installation is available")
    end
    loggedMissingGarageMap = false
    local unlocked = root.garageDoorUnlocked == true
    local changed, gate
    if unlocked then
        changed, gate = ensureUnlocked(doorSquare)
    else
        changed, gate = ensureLocked(doorSquare)
    end
    if lastDoor ~= gate or lastUnlocked ~= unlocked then
        lastDoor = gate
        lastUnlocked = unlocked
        local resolvedX = doorSquare ~= nil and doorSquare:getX() or "?"
        local resolvedY = doorSquare ~= nil and doorSquare:getY() or "?"
        Util.log("Garage access " .. (unlocked and "door installed and available" or "wall installed and sealed")
            .. " at resolved world square " .. tostring(resolvedX) .. "," .. tostring(resolvedY)
            .. " (authored edge 131,137-132,137)")
    end
    return changed
end

ExtractionMode.GarageDoorAuthority = Authority
return Authority

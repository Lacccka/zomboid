require "Moveables/ISMoveableSpriteProps"
require "TimedActions/ISDropWorldItemAction"
require "ExtractionMode/Config"

local Config = ExtractionMode.Config
local DESTRUCTIBLE_HIDEOUT_TILESET_PREFIX = "location_shop_mall_01_"

local function destructibleHideoutTile(object)
    if object == nil then return false end
    local spriteName = object:getSpriteName()
    return spriteName ~= nil and tostring(spriteName):sub(
        1, #DESTRUCTIBLE_HIDEOUT_TILESET_PREFIX) == DESTRUCTIBLE_HIDEOUT_TILESET_PREFIX
end

local function authoredHideoutWall(object)
    if object == nil or not object.isWall or not object:isWall() then return false end
    -- This tileset contains the hideout's intentional player-breakable wall.
    if destructibleHideoutTile(object) then return false end
    -- Doors and windows also report isWall() because their sprites carry edge
    -- collision flags. They must remain ordinary interactive special objects.
    -- Player-built walls are IsoThumpable and remain removable as before.
    if instanceof and (instanceof(object, "IsoDoor")
        or instanceof(object, "IsoWindow")
        or instanceof(object, "IsoThumpable")) then return false end

    local square = object:getSquare()
    local cell = getCell and getCell()
    if square == nil or cell == nil then return false end
    local hideout = Config.hideout()
    local anchor = cell:getGridSquare(math.floor(hideout.x), math.floor(hideout.y),
        math.floor(tonumber(hideout.z) or 0))
    local building = anchor and anchor:getBuilding()
    return building ~= nil and square:getBuilding() == building
end

local function protected(object)
    if object == nil then return false end
    if Config.isHideoutGarageDoorSquare(object:getSquare()) then return true end
    if Config.isHideoutGarageProtectedSquare(object:getSquare()) then return true end
    local modData = object:getModData()
    -- Old saves may already carry the protection marker. Treat the tileset
    -- exception as authoritative while the server removes those stale flags.
    if modData.ExtractionModeHideoutWall == true and destructibleHideoutTile(object) then return false end
    return modData.ExtractionModeIndestructible == true or authoredHideoutWall(object)
end

-- Every member of a multi-tile moveable passes through this internal check, so
-- an object anchored outside the bay cannot overlap the protected rectangle.
if not ISMoveableSpriteProps.ExtractionModeOriginalCanPlaceMoveableInternal then
    ISMoveableSpriteProps.ExtractionModeOriginalCanPlaceMoveableInternal =
        ISMoveableSpriteProps.canPlaceMoveableInternal
    function ISMoveableSpriteProps:canPlaceMoveableInternal(character, square, item, forceTypeObject)
        if Config.isHideoutGarageProtectedSquare(square) then return false end
        return self:ExtractionModeOriginalCanPlaceMoveableInternal(
            character, square, item, forceTypeObject)
    end
end

if ISDropWorldItemAction and not ISDropWorldItemAction.ExtractionModeOriginalIsValid then
    ISDropWorldItemAction.ExtractionModeOriginalIsValid = ISDropWorldItemAction.isValid
    function ISDropWorldItemAction:isValid()
        if Config.isHideoutGarageProtectedSquare(self.sq) then return false end
        return self:ExtractionModeOriginalIsValid()
    end
end

if not ISMoveableSpriteProps.ExtractionModeOriginalCanPickUp then
    ISMoveableSpriteProps.ExtractionModeOriginalCanPickUp = ISMoveableSpriteProps.canPickUpMoveable
    function ISMoveableSpriteProps:canPickUpMoveable(character, square, object)
        if protected(object) then return false end
        return self:ExtractionModeOriginalCanPickUp(character, square, object)
    end
end

if not ISMoveableSpriteProps.ExtractionModeOriginalCanScrapInternal then
    ISMoveableSpriteProps.ExtractionModeOriginalCanScrapInternal = ISMoveableSpriteProps.canScrapObjectInternal
    function ISMoveableSpriteProps:canScrapObjectInternal(result, object)
        if protected(object) then
            if result then result.containerFull = true end
            return false
        end
        return self:ExtractionModeOriginalCanScrapInternal(result, object)
    end
end

local function installDestroyGuard()
    -- ISDestroyCursor is a server-source class that Build 42 exposes to the
    -- client later in startup; requiring its server path from client Lua fails.
    if ISDestroyCursor and not ISDestroyCursor.ExtractionModeOriginalCanDestroy then
        ISDestroyCursor.ExtractionModeOriginalCanDestroy = ISDestroyCursor.canDestroy
        function ISDestroyCursor:canDestroy(object)
            if protected(object) then return false end
            return self:ExtractionModeOriginalCanDestroy(object)
        end
    end
end

local function installBuildingGuards()
    -- Build classes live under PZ's server-source tree but are exposed to the
    -- client later in startup. Guard both preview validity and the final build
    -- call; the latter also catches subclasses with custom isValid methods.
    if buildUtil and buildUtil.canBePlace
        and not buildUtil.ExtractionModeOriginalCanBePlace then
        buildUtil.ExtractionModeOriginalCanBePlace = buildUtil.canBePlace
        buildUtil.canBePlace = function(buildingObject, square)
            if Config.isHideoutGarageProtectedSquare(square) then return false end
            return buildUtil.ExtractionModeOriginalCanBePlace(buildingObject, square)
        end
    end
    if ISBuildingObject and ISBuildingObject.tryBuild
        and not ISBuildingObject.ExtractionModeOriginalTryBuild then
        ISBuildingObject.ExtractionModeOriginalTryBuild = ISBuildingObject.tryBuild
        function ISBuildingObject:tryBuild(x, y, z)
            local square = getCell and getCell():getGridSquare(x, y, z) or nil
            if Config.isHideoutGarageProtectedSquare(square) then
                self.canBeBuild = false
                self.build = false
                return nil
            end
            return self:ExtractionModeOriginalTryBuild(x, y, z)
        end
    end
end

installDestroyGuard()
installBuildingGuards()
Events.OnGameStart.Add(installDestroyGuard)
Events.OnGameStart.Add(installBuildingGuards)
Events.OnFillWorldObjectContextMenu.Add(installDestroyGuard)
Events.OnFillWorldObjectContextMenu.Add(installBuildingGuards)

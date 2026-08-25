local ZONE_MIN_X = 20389
local ZONE_MAX_X = 20398
local ZONE_MIN_Y = 0
local ZONE_MAX_Y = 14

local function isInsideTargetCoordinateRangeXY(x, y)
    if x == nil or y == nil then
        return false
    end
    return x >= ZONE_MIN_X
        and x <= ZONE_MAX_X
        and y >= ZONE_MIN_Y
        and y <= ZONE_MAX_Y
end

local function isInsideTargetCoordinateRange(square)
    local x = square:getX()
    local y = square:getY()
    return isInsideTargetCoordinateRangeXY(x, y)
end

local function isInsideTargetBuildingZone(square)
    if not isInsideTargetCoordinateRange(square) then
        return false
    end

    if square:getBuilding() ~= nil or square:getRoom() ~= nil then
        return true
    end

    local z = square:getZ()
    local adjacent = {
        { x = square:getX() + 1, y = square:getY() },
        { x = square:getX() - 1, y = square:getY() },
        { x = square:getX(), y = square:getY() + 1 },
        { x = square:getX(), y = square:getY() - 1 },
    }
    for i = 1, #adjacent do
        local sq = getCell():getGridSquare(adjacent[i].x, adjacent[i].y, z)
        if sq and (sq:getBuilding() ~= nil or sq:getRoom() ~= nil) then
            return true
        end
    end

    return false
end

local function isInsideTargetFloorZone(square)
    return square ~= nil and isInsideTargetCoordinateRange(square)
end

local function canOverlayExistingFloor(square)
    if not square or not isInsideTargetFloorZone(square) then
        return false
    end
    if square:has(IsoFlagType.water) then
        return false
    end
    return square:getFloor() ~= nil
end

local function isSolidFloorTileInfo(tileInfo)
    if not tileInfo or not tileInfo:getSpriteName() then
        return false
    end

    local sprite = getSprite(tileInfo:getSpriteName())
    local props = sprite and sprite:getProperties()
    return props ~= nil and props:has(IsoFlagType.solidfloor)
end

local function isValidOverlappingFloorBuildSquare(self, square, tileInfo, _requiresFloor, _extendsN, _extendsW)
    if not _requiresFloor or not canOverlayExistingFloor(square) or not isSolidFloorTileInfo(tileInfo) then
        return false
    end

    local tileInfoSprite = getSprite(tileInfo:getSpriteName())
    local tileInfoSpriteProps = tileInfoSprite and tileInfoSprite:getProperties()
    local facing = self:getFace() and self:getFace():getFaceName() or nil
    local testCollisions = true
    local canBuildOverWater = false

    if self.objectInfo:getScript():getOnIsValid() then
        local func = self.objectInfo:getScript():getOnIsValid()
        local params = {
            square = square,
            tileInfo = tileInfo,
            north = self.north,
            canBuildOverWater = canBuildOverWater,
            testCollisions = testCollisions,
            facing = facing,
        }

        if not BaseCraftingLogic.callLuaBool(func, params) then
            return false
        end

        canBuildOverWater = params.canBuildOverWater
        testCollisions = params.testCollisions
    end

    if testCollisions then
        local squareProps = square:getProperties()

        if square:has(IsoPropertyType.GARAGE_DOOR) then
            return false
        end
        if _extendsN and (
            squareProps:has(IsoFlagType.collideN)
            or squareProps:has(IsoFlagType.WallN)
            or squareProps:has(IsoFlagType.WallNW)
            or squareProps:has(IsoFlagType.WindowN)
            or squareProps:has(IsoFlagType.DoorWallN)
            or squareProps:has(IsoFlagType.HoppableN)
        ) then
            return false
        end
        if _extendsW and (
            squareProps:has(IsoFlagType.collideW)
            or squareProps:has(IsoFlagType.WallW)
            or squareProps:has(IsoFlagType.WallNW)
            or squareProps:has(IsoFlagType.WindowW)
            or squareProps:has(IsoFlagType.DoorWallW)
            or squareProps:has(IsoFlagType.HoppableW)
        ) then
            return false
        end
        if square:isVehicleIntersecting() then
            return false
        end
        if buildUtil.stairIsBlockingPlacement(square, true) then
            return false
        end
        if canBuildOverWater then
            local floor = square:getFloor()
            local floorProps = floor and floor:getSprite() and floor:getSprite():getProperties()
            local isWater = floorProps and floorProps:has(IsoFlagType.water)
                or (square:getObjects():size() == 2 and squareProps:has(IsoFlagType.taintedWater))
            if isWater then
                return true
            end
        end
        if tileInfoSpriteProps and tileInfoSpriteProps:has("IsStackable") and square:getFloor() == nil then
            return false
        end
    end

    if self.previousStages:size() > 0 then
        return false
    end

    return true
end

local function ensureWallCoveringDependencies()
    if not ISPaintMenu then
        require "BuildingObjects/ISPaintMenu"
    end
    if not ISPaintCursor then
        require "BuildingObjects/ISPaintCursor"
    end
    if not ISPaperCursor then
        require "BuildingObjects/ISPaperCursor"
    end
    if not ISPlasterAction then
        require "BuildingObjects/TimedActions/ISPlasterAction"
    end
    if not ISPaintAction then
        require "BuildingObjects/TimedActions/ISPaintAction"
    end
    if not ISBuildIsoEntity then
        require "BuildingObjects/ISBuildIsoEntity"
    end
    if not ISMoveableSpriteProps then
        require "Moveables/ISMoveableSpriteProps"
    end

    return ISPaintMenu ~= nil
        and ISPaintCursor ~= nil
        and ISPaperCursor ~= nil
        and ISPlasterAction ~= nil
        and ISPaintAction ~= nil
        and ISBuildIsoEntity ~= nil
        and ISMoveableSpriteProps ~= nil
        and Painting ~= nil
        and OtherPainting ~= nil
        and WallPaper ~= nil
end

local function getPlasterNorthSuffix(object)
    if instanceof(object, "IsoThumpable") then
        if object:getNorth() then
            return "North"
        end
        return ""
    end

    local props = object:getProperties()
    if props:has("WallN") or props:has("DoorWallN") or props:has("WindowN") then
        return "North"
    end
    return ""
end

local function getPaintSuffix(object)
    if instanceof(object, "IsoThumpable") then
        if object:getNorth() then
            return "North"
        end
        return ""
    end

    local props = object:getProperties()
    if props:has("WallNW") then
        return "Corner"
    end
    if props:has("WallN") or props:has("DoorWallN") or props:has("WindowN") then
        return "North"
    end
    return ""
end

local overridesApplied = false
local lastStairBlockLogKey = nil

local stairOverrideStatus = {
    woodenIsValid = false,
    woodenCreate = false,
    buildActionIsValid = false,
    buildActionPerform = false,
    buildIsoEntityIsValid = false,
    buildIsoEntityCreate = false,
    buildRampIsValid = false,
    buildRampCreate = false,
    buildingObjectTryBuild = false,
}

local function containsKeyword(value, keyword)
    if value == nil then
        return false
    end
    return string.find(string.lower(tostring(value)), keyword, 1, true) ~= nil
end

local function isStairBuildingItem(item)
    return containsKeyword(item and item.Type, "stair")
        or containsKeyword(item and item.Type, "ramp")
        or containsKeyword(item and item.sprite, "stair")
        or containsKeyword(item and item.sprite, "ramp")
        or containsKeyword(item and item.name, "stair")
        or containsKeyword(item and item.name, "ramp")
end

local function isStairBuildObject(object)
    return object ~= nil and (
        object.isStairs == true
            or containsKeyword(object.Type, "stair")
            or containsKeyword(object.Type, "ramp")
            or containsKeyword(object.which, "stair")
            or containsKeyword(object.which, "ramp")
            or containsKeyword(object.name, "stair")
            or containsKeyword(object.name, "ramp")
            or containsKeyword(object.sprite, "stair")
            or containsKeyword(object.sprite, "ramp")
            or containsKeyword(object.spriteName, "stair")
            or containsKeyword(object.spriteName, "ramp")
            or containsKeyword(object.chosenSprite, "stair")
            or containsKeyword(object.chosenSprite, "ramp")
    )
end

local function isStairBuildAction(action)
    return isStairBuildingItem(action and action.item)
        or isStairBuildObject(action and action.item)
        or containsKeyword(action and action.spriteName, "stair")
        or containsKeyword(action and action.spriteName, "ramp")
end

local function getBuildActionPosition(action)
    local x = action.x
    local y = action.y
    local z = action.z

    if action.square then
        x = action.square:getX()
        y = action.square:getY()
        z = action.square:getZ()
    end

    return x, y, z
end

local function logBlockedStairBuild(source, x, y, z, itemType)
    local key = string.format("%s:%s:%s:%s:%s", source, tostring(x), tostring(y), tostring(z), tostring(itemType))
    if key == lastStairBlockLogKey then
        return
    end

    lastStairBlockLogKey = key
    print(string.format(
        "[EFZ_Maps] Blocked stair construction via %s at (%s, %s, %s), type=%s",
        source,
        tostring(x),
        tostring(y),
        tostring(z),
        tostring(itemType)
    ))
end

local function tryApplyStairBuildOverrides()
    if not stairOverrideStatus.woodenIsValid and ISWoodenStairs and ISWoodenStairs.isValid then
        local vanillaWoodenStairsIsValid = ISWoodenStairs.isValid
        function ISWoodenStairs:isValid(square)
            if isInsideTargetCoordinateRange(square) then
                logBlockedStairBuild("ISWoodenStairs:isValid", square:getX(), square:getY(), square:getZ(), self.Type)
                return false
            end
            return vanillaWoodenStairsIsValid(self, square)
        end
        stairOverrideStatus.woodenIsValid = true
        print("[EFZ_Maps] Stair block hook applied: ISWoodenStairs.isValid")
    end

    if not stairOverrideStatus.woodenCreate and ISWoodenStairs and ISWoodenStairs.create then
        local vanillaWoodenStairsCreate = ISWoodenStairs.create
        function ISWoodenStairs:create(x, y, z, north, sprite)
            if isInsideTargetCoordinateRangeXY(x, y) then
                logBlockedStairBuild("ISWoodenStairs:create", x, y, z, self.Type)
                return
            end
            return vanillaWoodenStairsCreate(self, x, y, z, north, sprite)
        end
        stairOverrideStatus.woodenCreate = true
        print("[EFZ_Maps] Stair block hook applied: ISWoodenStairs.create")
    end

    if not stairOverrideStatus.buildActionIsValid and ISBuildAction and ISBuildAction.isValid then
        local vanillaBuildActionIsValid = ISBuildAction.isValid
        function ISBuildAction:isValid()
            local x, y, z = getBuildActionPosition(self)
            if isStairBuildAction(self) and isInsideTargetCoordinateRangeXY(x, y) then
                local itemType = self.item and self.item.Type
                logBlockedStairBuild("ISBuildAction:isValid", x, y, z, itemType)
                return false
            end
            return vanillaBuildActionIsValid(self)
        end
        stairOverrideStatus.buildActionIsValid = true
        print("[EFZ_Maps] Stair block hook applied: ISBuildAction.isValid")
    end

    if not stairOverrideStatus.buildActionPerform and ISBuildAction and ISBuildAction.perform then
        local vanillaBuildActionPerform = ISBuildAction.perform
        function ISBuildAction:perform()
            local x, y, z = getBuildActionPosition(self)
            if isStairBuildAction(self) and isInsideTargetCoordinateRangeXY(x, y) then
                local itemType = self.item and self.item.Type
                logBlockedStairBuild("ISBuildAction:perform", x, y, z, itemType)
                self:forceCancel()
                return
            end
            return vanillaBuildActionPerform(self)
        end
        stairOverrideStatus.buildActionPerform = true
        print("[EFZ_Maps] Stair block hook applied: ISBuildAction.perform")
    end

    if not stairOverrideStatus.buildIsoEntityIsValid and ISBuildIsoEntity and ISBuildIsoEntity.isValid then
        local vanillaBuildIsoEntityIsValid = ISBuildIsoEntity.isValid
        function ISBuildIsoEntity:isValid(square)
            if isStairBuildObject(self) and isInsideTargetCoordinateRange(square) then
                logBlockedStairBuild("ISBuildIsoEntity:isValid", square:getX(), square:getY(), square:getZ(), self.name)
                return false
            end
            return vanillaBuildIsoEntityIsValid(self, square)
        end
        stairOverrideStatus.buildIsoEntityIsValid = true
        print("[EFZ_Maps] Stair block hook applied: ISBuildIsoEntity.isValid")
    end

    if not stairOverrideStatus.buildIsoEntityCreate and ISBuildIsoEntity and ISBuildIsoEntity.create then
        local vanillaBuildIsoEntityCreate = ISBuildIsoEntity.create
        function ISBuildIsoEntity:create(x, y, z, north, sprite)
            if isStairBuildObject(self) and isInsideTargetCoordinateRangeXY(x, y) then
                logBlockedStairBuild("ISBuildIsoEntity:create", x, y, z, self.name)
                return false
            end
            return vanillaBuildIsoEntityCreate(self, x, y, z, north, sprite)
        end
        stairOverrideStatus.buildIsoEntityCreate = true
        print("[EFZ_Maps] Stair block hook applied: ISBuildIsoEntity.create")
    end

    if not stairOverrideStatus.buildRampIsValid and ISBuildRampCursor and ISBuildRampCursor.isValid then
        local vanillaBuildRampIsValid = ISBuildRampCursor.isValid
        function ISBuildRampCursor:isValid(square)
            if isInsideTargetCoordinateRange(square) then
                logBlockedStairBuild("ISBuildRampCursor:isValid", square:getX(), square:getY(), square:getZ(), self.which)
                return false
            end
            return vanillaBuildRampIsValid(self, square)
        end
        stairOverrideStatus.buildRampIsValid = true
        print("[EFZ_Maps] Stair block hook applied: ISBuildRampCursor.isValid")
    end

    if not stairOverrideStatus.buildRampCreate and ISBuildRampCursor and ISBuildRampCursor.create then
        local vanillaBuildRampCreate = ISBuildRampCursor.create
        function ISBuildRampCursor:create(x, y, z, north, sprite)
            if isInsideTargetCoordinateRangeXY(x, y) then
                logBlockedStairBuild("ISBuildRampCursor:create", x, y, z, self.which)
                return
            end
            return vanillaBuildRampCreate(self, x, y, z, north, sprite)
        end
        stairOverrideStatus.buildRampCreate = true
        print("[EFZ_Maps] Stair block hook applied: ISBuildRampCursor.create")
    end

    if not stairOverrideStatus.buildingObjectTryBuild and ISBuildingObject and ISBuildingObject.tryBuild then
        local vanillaBuildingObjectTryBuild = ISBuildingObject.tryBuild
        function ISBuildingObject:tryBuild(x, y, z)
            if isStairBuildObject(self) and isInsideTargetCoordinateRangeXY(x, y) then
                logBlockedStairBuild("ISBuildingObject:tryBuild", x, y, z, self.Type or self.name)
                return nil
            end
            return vanillaBuildingObjectTryBuild(self, x, y, z)
        end
        stairOverrideStatus.buildingObjectTryBuild = true
        print("[EFZ_Maps] Stair block hook applied: ISBuildingObject.tryBuild")
    end
end

local function applyZoneWallCoveringOverrides()
    if overridesApplied then
        tryApplyStairBuildOverrides()
        return
    end

    if not ensureWallCoveringDependencies() then
        print("[EFZ_Maps] Wall covering overrides not ready yet. Waiting for script load.")
        return
    end

    local vanillaCanPaint = ISPaintCursor.canPaint
    function ISPaintCursor:canPaint(object)
        if vanillaCanPaint(self, object) then
            return true
        end

        if not object or not object:getSquare() or not object:getSprite() then
            return false
        end
        if not object:getSquare():isCouldSee(self.player) then
            return false
        end
        if not self:hasItems() then
            return false
        end
        if not isInsideTargetBuildingZone(object:getSquare()) then
            return false
        end

        if self.action == "paintThump" then
            local props = object:getProperties()
            local wallType = props:get("PaintingType")
            if wallType and Painting[wallType] and Painting[wallType][self.args.paintType] ~= nil then
                return true
            end
            if wallType and OtherPainting[wallType] and OtherPainting[wallType][self.args.paintType] ~= nil then
                return true
            end

            wallType = ISPaintMenu.getWallType(object)
            if wallType and Painting[wallType] and Painting[wallType][self.args.paintType] ~= nil then
                return true
            end
        end

        if self.action == "plaster" then
            local wallType = ISPaintMenu.getWallType(object)
            if not wallType or not Painting[wallType] then
                return false
            end
            local north = getPlasterNorthSuffix(object)
            return Painting[wallType]["plasterTile" .. north] ~= nil
        end

        return false
    end

    local vanillaPaintCursorRender = ISPaintCursor.render
    function ISPaintCursor:render(x, y, z, square)
        if self.action ~= "plaster" then
            return vanillaPaintCursorRender(self, x, y, z, square)
        end

        local hc = getCore():getGoodHighlitedColor()
        if not self:isValid(square) then
            hc = getCore():getBadHighlitedColor()
        end
        self:getFloorCursorSprite():RenderGhostTileColor(x, y, z, hc:getR(), hc:getG(), hc:getB(), 0.8)

        if self.currentSquare ~= square then
            self.objectIndex = 1
            self.currentSquare = square
        end

        self.renderX = x
        self.renderY = y
        self.renderZ = z

        local objects = self:getObjectList()
        if self.objectIndex < 1 or self.objectIndex > #objects then
            return
        end

        local object = objects[self.objectIndex]
        local wallType = ISPaintMenu.getWallType(object)
        if not wallType or not Painting[wallType] then
            return
        end

        local north = getPlasterNorthSuffix(object)
        local spriteName = Painting[wallType]["plasterTile" .. north]
        if not spriteName then
            return
        end

        self.plasterSprite = IsoSprite.new()
        self.plasterSprite:LoadSingleTexture(spriteName)
        self.plasterSprite:RenderGhostTile(x, y, z)
    end

    local vanillaCanPaper = ISPaperCursor.canPaper
    function ISPaperCursor:canPaper(object)
        if vanillaCanPaper(self, object) then
            return true
        end

        if not object or not object:getSquare() or not object:getSprite() then
            return false
        end
        if not object:getSquare():isCouldSee(self.player) then
            return false
        end
        if not self:hasItems() then
            return false
        end
        if not isInsideTargetBuildingZone(object:getSquare()) then
            return false
        end

        local props = object:getProperties()
        local wallType = props:get("PaintingType")
        return wallType ~= nil and WallPaper[wallType] ~= nil and WallPaper[wallType][self.paperType] ~= nil
    end

    local vanillaPlasterComplete = ISPlasterAction.complete
    function ISPlasterAction:complete()
        if self.thumpable
            and not instanceof(self.thumpable, "IsoThumpable")
            and isInsideTargetBuildingZone(self.thumpable:getSquare()) then
            local wallType = ISPaintMenu.getWallType(self.thumpable)
            if wallType and Painting[wallType] then
                local north = getPlasterNorthSuffix(self.thumpable)
                local sprite = Painting[wallType]["plasterTile" .. north]
                if sprite then
                    self.thumpable:setSpriteFromName(sprite)
                    self.thumpable:transmitUpdatedSpriteToClients()
                    if not self.character:isBuildCheat() and self.plasterBucket then
                        self.plasterBucket:UseAndSync()
                    end
                    return true
                end
            end
        end

        return vanillaPlasterComplete(self)
    end

    local vanillaPaintComplete = ISPaintAction.complete
    function ISPaintAction:complete()
        if self.thumpable
            and not instanceof(self.thumpable, "IsoThumpable")
            and isInsideTargetBuildingZone(self.thumpable:getSquare()) then
            local object = self.thumpable
            object:cleanWallBlood()

            local sprite = nil
            local paintingType = nil
            local spriteObject = object:getSprite()
            if spriteObject and spriteObject:getProperties() then
                paintingType = spriteObject:getProperties():get("PaintingType")
            end

            local suffix = getPaintSuffix(object)
            if paintingType and Painting[paintingType] then
                sprite = Painting[paintingType][self.painting .. suffix]
                if not sprite and suffix ~= "" then
                    sprite = Painting[paintingType][self.painting]
                end
            end

            if not sprite then
                local wallType = ISPaintMenu.getWallType(object)
                if wallType and Painting[wallType] then
                    sprite = Painting[wallType][self.painting .. suffix]
                    if not sprite and suffix ~= "" then
                        sprite = Painting[wallType][self.painting]
                    end
                end
            end

            if sprite then
                object:setSpriteFromName(sprite)
                object:transmitUpdatedSpriteToClients()
            elseif paintingType
                and OtherPainting[paintingType]
                and OtherPainting[paintingType][self.painting] then
                local color = OtherPainting[paintingType][self.painting]
                object:setCustomColor(ColorInfo.new(color.r, color.g, color.b, 1))
                object:transmitCustomColorToClients()
            else
                self.character:Say(getText("IGUI_EFZ_NeedPlasterFirst"))
                print(string.format(
                    "[EFZ_Maps] Paint mapping missing at (%d, %d, %d), paint=%s, paintingType=%s, wallType=%s",
                    object:getX(),
                    object:getY(),
                    object:getZ(),
                    tostring(self.painting),
                    tostring(paintingType),
                    tostring(ISPaintMenu.getWallType(object))
                ))
                return false
            end

            if isServer() or not ISBuildMenu.cheat then
                if self.paintPot then
                    self.paintPot:UseAndSync()
                end
            end

            return true
        end

        return vanillaPaintComplete(self)
    end

    local vanillaBuildIsoEntityIsValidPerSquare = ISBuildIsoEntity.isValidPerSquare
    function ISBuildIsoEntity:isValidPerSquare(square, tileInfo, _requiresFloor, _extendsN, _extendsW)
        if vanillaBuildIsoEntityIsValidPerSquare(self, square, tileInfo, _requiresFloor, _extendsN, _extendsW) then
            return true
        end

        return isValidOverlappingFloorBuildSquare(self, square, tileInfo, _requiresFloor, _extendsN, _extendsW)
    end

    local vanillaCanPlaceMoveableInternal = ISMoveableSpriteProps.canPlaceMoveableInternal
    function ISMoveableSpriteProps:canPlaceMoveableInternal(_character, _square, _item, _forceTypeObject)
        if vanillaCanPlaceMoveableInternal(self, _character, _square, _item, _forceTypeObject) then
            return true
        end

        if _forceTypeObject or not self.isMoveable or self.type ~= "FloorTile" then
            return false
        end

        return canOverlayExistingFloor(_square)
    end

    tryApplyStairBuildOverrides()
    overridesApplied = true
    print("[EFZ_Maps] Wall covering zone overrides applied.")
end

Events.OnGameBoot.Add(applyZoneWallCoveringOverrides)
Events.OnGameStart.Add(applyZoneWallCoveringOverrides)
Events.OnCreatePlayer.Add(applyZoneWallCoveringOverrides)
Events.OnGameStart.Add(tryApplyStairBuildOverrides)
Events.OnCreatePlayer.Add(tryApplyStairBuildOverrides)
Events.OnTick.Add(tryApplyStairBuildOverrides)


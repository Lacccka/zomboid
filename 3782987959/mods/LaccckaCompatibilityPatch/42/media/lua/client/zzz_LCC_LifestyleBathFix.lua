local Guard = require "LCC/Guard"
local FEATURE = "lifestyle.bathtub-west-entry"

local function patchWalkToFront(player, object, startX, startY)
    if startX or startY or not player or not object or not object:getSprite() then
        return false
    end

    local spriteName = object:getSprite():getName()
    local square = object:getSquare()
    if not square or (spriteName ~= "fixtures_bathroom_01_25"
            and spriteName ~= "fixtures_bathroom_01_52") then
        return false
    end

    local east = square:getE()
    local west = square:getW()
    local eastFree = east and AdjacentFreeTileFinder.privTrySquare(square, east)
    local westFree = west and AdjacentFreeTileFinder.privTrySquare(square, west)
    local originalWouldUseEast = eastFree and player:getX() >= east:getX()

    if westFree and player:getX() >= west:getX() and not originalWouldUseEast then
        player:setX(west:getX())
        player:setY(west:getY())
        return true
    end

    return false
end

local function installBathFix()
    -- Lifestyle may publish this client helper after shared Lua has loaded.
    -- Missing API here is not a failure yet; retry at OnGameStart.
    if type(BathTubFunctions) ~= "table" or type(BathTubFunctions.walkToFront) ~= "function" then
        return
    end
    if BathTubFunctions.__LCCWestFix then return end

    Guard.install {
        id = FEATURE,
        reinstall = true,
        validate = function()
            if type(BathTubFunctions) ~= "table" or type(BathTubFunctions.walkToFront) ~= "function" then
                return false, "BathTubFunctions.walkToFront is unavailable"
            end
            return true
        end,
        install = function()
            local originalWalkToFront = BathTubFunctions.walkToFront

            BathTubFunctions.walkToFront = function(player, object, startX, startY)
                if Guard.isEnabled(FEATURE) then
                    local ok, handled = Guard.protect(
                        FEATURE,
                        "walkToFront compatibility path",
                        patchWalkToFront,
                        player,
                        object,
                        startX,
                        startY
                    )
                    if ok and handled then
                        return true
                    end
                end

                -- Keep Lifestyle's own failure behavior observable.
                return originalWalkToFront(player, object, startX, startY)
            end

            BathTubFunctions.__LCCWestFix = true
        end,
    }
end

installBathFix()
Events.OnGameStart.Add(installBathFix)

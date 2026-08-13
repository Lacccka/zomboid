local function installBathFix()
    if not BathTubFunctions or not BathTubFunctions.walkToFront then return end
    if BathTubFunctions.__LCCWestFix then return end

    local originalWalkToFront = BathTubFunctions.walkToFront

    BathTubFunctions.walkToFront = function(player, object, startX, startY)
        if not startX and not startY and player and object and object:getSprite() then
            local spriteName = object:getSprite():getName()
            local square = object:getSquare()
            if square and (spriteName == "fixtures_bathroom_01_25"
                    or spriteName == "fixtures_bathroom_01_52") then
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
            end
        end
        return originalWalkToFront(player, object, startX, startY)
    end

    BathTubFunctions.__LCCWestFix = true
end

installBathFix()
Events.OnGameStart.Add(installBathFix)


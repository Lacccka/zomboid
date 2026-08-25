
-- ====================================================================
-- oZumbiAnalitico : 
-- 1. youtube ; https://www.youtube.com/@oZumbiAnalitico
-- 2. steam workshop ; https://steamcommunity.com/id/ozumbianalitico/myworkshopfiles/
-- 3. email ; ericyuta@gmail.com
-- ====================================================================
-- File [ 11_checkers.lua ] : Utilities to import libraries

local Check = {}

-- Inventory [ .../11_checkers.lua ]
-- --------------------------- LIB | VANILLA --------------------------
-- local PARENT_DIR = 
-- local Check = require(PARENT_DIR.."11_checkers")

--
-- ====================================================================
-- Utilities

-- Check.FAdjacentSquare(inner_function, [ isoplayer ]) o (square, isoplayer)
function Check.FAdjacentSquare(inner_function, isoplayer) -- [ok]
    if not isoplayer then isoplayer = getSpecificPlayer(0) end
    if not isoplayer then return nil end
    local X = isoplayer:getX()
    local Y = isoplayer:getY()
    local Z = isoplayer:getZ()
    if not X or not Y or not Z then return nil end
    local CELL = getCell()
    if not CELL then return nil end
    local square = nil
    local inner_function_ret = nil
    --
    square = CELL:getGridSquare(X,Y,Z)
    inner_function_ret = inner_function(square, isoplayer)
    if inner_function_ret == nil then 
    else 
        if inner_function_ret == true then
            return 
        else 
            return inner_function_ret 
        end
    end
    --
    square = CELL:getGridSquare(X-1,Y,Z)
    inner_function_ret = inner_function(square, isoplayer)
    if inner_function_ret == nil then 
    else 
        if inner_function_ret == true then
            return 
        else 
            return inner_function_ret 
        end
    end
    --
    square = CELL:getGridSquare(X-1,Y+1,Z)
    inner_function_ret = inner_function(square, isoplayer)
    if inner_function_ret == nil then 
    else 
        if inner_function_ret == true then
            return 
        else 
            return inner_function_ret 
        end
    end
    --
    square = CELL:getGridSquare(X,Y+1,Z)
    inner_function_ret = inner_function(square, isoplayer)
    if inner_function_ret == nil then 
    else 
        if inner_function_ret == true then
            return 
        else 
            return inner_function_ret 
        end
    end
    --
    square = CELL:getGridSquare(X+1,Y+1,Z)
    inner_function_ret = inner_function(square, isoplayer)
    if inner_function_ret == nil then 
    else 
        if inner_function_ret == true then
            return 
        else 
            return inner_function_ret 
        end
    end
    --
    square = CELL:getGridSquare(X+1,Y,Z)
    inner_function_ret = inner_function(square, isoplayer)
    if inner_function_ret == nil then 
    else 
        if inner_function_ret == true then
            return 
        else 
            return inner_function_ret 
        end
    end
    --
    square = CELL:getGridSquare(X+1,Y-1,Z)
    inner_function_ret = inner_function(square, isoplayer)
    if inner_function_ret == nil then 
    else 
        if inner_function_ret == true then
            return 
        else 
            return inner_function_ret 
        end
    end
    --
    square = CELL:getGridSquare(X,Y-1,Z)
    inner_function_ret = inner_function(square, isoplayer)
    if inner_function_ret == nil then 
    else 
        if inner_function_ret == true then
            return 
        else 
            return inner_function_ret 
        end
    end
    --
    square = CELL:getGridSquare(X-1,Y-1,Z)
    inner_function_ret = inner_function(square, isoplayer)
    if inner_function_ret == nil then 
    else 
        if inner_function_ret == true then
            return 
        else 
            return inner_function_ret 
        end
    end
end

function Check.FAdjacentMovingObjects(inner_function,isoplayer) -- [ok]
    if not isoplayer then isoplayer = getSpecificPlayer(0) end
    return Check.FAdjacentSquare( function(square) 
        if not square then return nil end -- continue
        if square:isBlockedTo(isoplayer:getCurrentSquare()) then return nil end -- continue
        local array = square:getMovingObjects()
        for i=0,array:size()-1 do 
            local obj = array:get(i)
            local inner_function_ret = inner_function(obj)
            if inner_function_ret == nil then 
            else 
                return inner_function_ret 
            end
        end
        return nil -- end of iteration
    end , isoplayer)
end

--
-- ====================================================================
--

-- Check.isAsleep([ isoplayer ])
function Check.isAsleep(isoplayer) -- [testing]
    if not isoplayer then isoplayer = getSpecificPlayer(0) end
    if not isoplayer then return true end
    return isoplayer:isAsleep()
end

-- Check.adjVehicle([ isoplayer ])
function Check.adjVehicle(isoplayer) -- [ok]
    local b_ret = false
    Check.FAdjacentMovingObjects( function(obj) 
        if not obj then return nil end -- continue
        if string.find( tostring(obj),"vehicle" ) then  
            b_ret = true
            return true -- break
        end
        return nil -- continue
    end ,isoplayer)
    return b_ret
end

-- Check.isInHotbar(item, [ playerNum ])
function Check.isInHotbar(item, playerNum) -- [ok]
	if not item then return nil end
    if not playerNum then playerNum = 0 end
    local HOTBAR = getPlayerHotbar(playerNum)
    return HOTBAR:isInHotbar(item)
end

function Check.isWalkietalkie(item) -- [ok]
	if not item then return nil end
    if item.getType and item:getType() and string.find( item:getType(), "WalkieTalkie" ) then return true end
    return false
end

-- Check.has_item(name,[ isoplayer ])
function Check.has_item(name,isoplayer) -- [testing]
    -- optional args
    if not isoplayer then isoplayer = getSpecificPlayer(0) end
    --
    local b_ret = false
    if not isoplayer then return nil end
    local INVENTORY = isoplayer:getInventory()
    if not INVENTORY then return nil end
    local items = INVENTORY:getItems()
    for i = 0, items:size()-1 do 
        local ITEM = items:get(i)
        b_ret = ITEM:getName()==name
        if b_ret then break end
    end
	return b_ret
end

-- Check.isOutside([ isoplayer ]) 
function Check.isOutside(isoplayer) -- [ok]
    -- optional args
    if not isoplayer then isoplayer = getSpecificPlayer(0) end
    --
    return isoplayer:isOutside()
end

-- Check.foragingLevel([ isoplayer ]) 
function Check.foragingLevel(isoplayer) -- [testing]
    if not isoplayer then isoplayer = getSpecificPlayer(0) end
    return isoplayer:getPerkLevel(Perks.PlantScavenging)
end

-- Check.level(perk, [ isoplayer ])
function Check.level(perk, isoplayer) -- [testing]
    if type(perk)=="string" then perk = Perks.FromString(perk) end
    if not isoplayer then isoplayer = getSpecificPlayer(0) end
    return isoplayer:getPerkLevel(perk)
end

-- getPlayer():isSneaking()

function Check.isInSearchMode(playerNum) -- [testing]
    if not playerNum then playerNum = 0 end
    return getSearchMode():isEnabled(playerNum)
end

--
-- ====================================================================
--

return Check 







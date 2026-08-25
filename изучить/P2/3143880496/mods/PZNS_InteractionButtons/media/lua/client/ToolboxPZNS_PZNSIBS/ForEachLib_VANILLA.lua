-- ------------------------------------
-- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
-- ====================================================================

-- oZumbiAnalitico : 
-- 1. ytb link - https://www.youtube.com/@oZumbiAnalitico
-- 2. email - ericyuta@gmail.com

-- Modified|Verified: 09112023:1704
--                    16112023:1956
--                    21112023:0150

--
-- ====================================================================
--           >>> THEORY : should read first before use it <<<

-- Necessary Properties of Inner Function:
-- 1. Inner Functions MUST RETURN nil to continue the loop. So nil means success or continue the loop. True means break of the loop and anything else means break return which return the object found.
-- 2. The ret argument is used for tables or return objects. But is almost unnecessary.
-- 3. If the Inner Function return something DIFFERENT from nil them the loop stops. 
-- 4. The true value returned from Inner Function means a failure, assertion failure, of some event that makes the next loop operations meaningless.
-- 5. You can use less variables in functions for lua, so you can use a function inner_function(index) for a place that expect inner_function(index, value, ret), the remaining variables will be assigned nil.
-- 6. inner_function is a function that encapsulate the iteration of a loop.
-- 7. when an inner_function return true then it will emulate the "break" statement
-- 8. when an inner_function return nil earlier it will emulate the "continue" statement which don't exists in lua
-- --------------------------------------------------------------------
-- For Short: 
-- 1. "return true" means break failure, 
-- 2. "return object" means break success and return the object
-- 3. "return nil" means continue the loop or end of the iteration. 

--
-- ====================================================================
-- >>> This part is optional, or on demand, to understand the annotations <<<

-- Definition [ Path-Logic Notation ] : A logic is a list of linear statements "logical-paths", each statement represent an execution path restricted to an condition. Using the notation below:

-- Example:
-- "for i in iterator() do if condition() then function_call() end end" in path-logic notation will be:
-- & i in iterator() || % condition() || function_call()

-- 1. A || B means that B is inside A, or A calls B, B is inside A structure. Example is a function A() calling a subfunction B()
-- 2. A | B means that B is executed after A, but in same context level of A. Example let C be a function that calls A() and in the next line calls B()
-- 3. % is a conditional control structure
-- 4. & is a loop structure
-- 5. $ is a variable or object construction
-- 6. *% is a conditional checkpoint, often in beginning of function
-- 7. ? A, B, C means a random choice between A, B, C, ...
-- 8. { A, B, C } in the end of statements means that the end part of statement use A, B, C functions or operations in a way decided when implemented.
-- 9. { A, B, C } in beginning of statements means that the statement follows when conditions A, B, C are met. 
-- 10. _function() is not a reference to a real function, is a reference to a concept that could be used in an actual function. That name becomes a suffix to an implemented function.
-- 11. <- is a binary operator that encapsulate the return of a function. A <- B means that A will return the return of B. A <- B || C means is the way to continue the path to B context, C is called in B definition.
-- 12. <custom_operator> is the notation for an binary operator, for example, function_1 <definition> function_2 could mean that function_1 defines function_2, function_1 <callback> function_2 could mean that function_1 register function_2 as a callback for some event. A <operator> B || C is some how equivalent to 1. A(B, ...) 2. B || C

-- Path-Logic Notation for ForEach functions:
-- 1. calling "ForEach(inner_function)" will be conceptually the same as "& variables in container defined in ForEach function || inner_function(variables)"
-- 2. ForEach o inner_function := & variables in container defined in ForEach function || inner_function()
-- 3. ForEach o inner_function || another_function() will be the same as ... 
-- ... & "ForEach" || inner_function() || another_function() ... so the another_function is called inside inner_function definition
-- 4.  A o B is the binary operator foreach. To be similar to function composition.

local ForEach = {}

--
-- ====================================================================

--
-- ====================================================================
-- Foreach for Tables

-- Note: There is a foreach for tables in lua language
-- Syntax: foreach(table_reference, function_reference)
-- The foreach defined here are customized for specific purposes.

-- Logic [ ForEach Basic Definition ]
-- 1. Let F the for each function and I the inner function
-- ----------------------------------------------
-- 1. F(I,...) || & || $ R || R <assigned as return of> I
-- 2. F(I,...) || & || $ R | % not R || "continue"
-- 3. F(I,...) || & || $ R | % not R | % "else" || % R == true || "break"
-- 4. F(I,...) || & || $ R | % not R | % "else" || % R == true | % R is object || F <- R

-- inner_function(index, value, array_table, ret) 
function ForEach.arrayTable(inner_function, array_table) -- [ok]
    if type(array_table) ~= "table" then return nil end
    if #array_table == 0 then return nil end
    local ret = nil
    for index, value in ipairs(array_table) do
        local inner_function_ret = inner_function(index,value, array_table, ret)
        if inner_function_ret == nil then -- continue
        else -- break or break return
            if inner_function_ret == true then -- break 
                break 
            else -- if false the return false, if any other thing then return this thing
                return inner_function_ret
            end
        end
    end
    return ret
end

function ForEach.genericTable(inner_function, generic_table) -- [ok]
    if type(generic_table) ~= "table" then return nil end
    if #generic_table == 0 then return nil end
    local ret = nil
    for index, value in pairs(generic_table) do
        local inner_function_ret = inner_function(index,value, array_table, ret)
        if inner_function_ret == nil then -- continue
        else -- break or break return
            if inner_function_ret == true then -- break 
                break 
            else -- if false the return false, if any other thing then return this thing
                return inner_function_ret
            end
        end
    end
    return ret
end

-- Logic [ ForEach.nestedTable_endPoints ]
-- 1. F := ForEach.nestedTable_endPoints
-- 2. I := inner_function
-- 3. G := ForEach.genericTable
-- ----------------------------------------
-- 1. F(I, ...) := G o inner || % "is table" || %* size zero | F o I
-- 2. F(I, ...) := G o inner || % "is table" | inner <- I

-- inner_function(index,value, array_table)
function ForEach.nestedTable_endPoints(inner_function, nested_table) -- [ok]
    if type(nested_table) ~= "table" then return nil end
    if #nested_table == 0 then return nil end
    local function inner(index,value, array_table)
        if type(value) == 'table' then 
            if #value==0 then return nil end
            ForEach.nestedTable_endPoints(inner_function, value) 
        end
        return inner_function(index,value, array_table)
    end
    ForEach.genericTable(inner, nested_table)
end

--
-- ====================================================================
-- Foreach ArrayList

-- inner_function(i, arrayList , size, ret)
function ForEach.arrayList(inner_function, arrayList)
    local size = arrayList:size()
    if size == 0 then return nil end
    local ret = nil
    for i=0, size-1 do
        -- INNER_FUNCTION_COMPONENT
        local inner_function_return = inner_function(i, arrayList , size, ret)
        -- INNER_FUNCTION_COMPONENT || inner_function return properties
        if inner_function_return == nil then 
            -- nothing : continue the loop
        else
            if inner_function_return == true then
                break
            else
                return inner_function_return
            end
        end
    end
    return ret
end

--
-- ====================================================================
-- Foreach IsoPlayer Related

-- Logic [ ForEach.inventoryItem ]
-- 1. F := ForEach.inventoryItem
-- 2. I := inner_function
-- 3. A := ForEach.arrayList
-- 4. i := inner2_function
-- ----------------------------------------
-- 1. F(I, ...) := A o i || $ item | i <- I(item, i, size, items, inventory, isoplayer, ret)
function ForEach.inventoryItem(inner_function, isoplayer) 
    if not isoplayer then return nil end
    local INVENTORY = isoplayer:getInventory()
    if not INVENTORY then return nil end
    local ARRAY = INVENTORY:getItems()
    if not ARRAY then return nil end
    --
    local function inner2_function(i, arrayList , size, ret)
        local item = arrayList:get(i)
        return inner_function(item, i, size, arrayList, INVENTORY, isoplayer, ret)
    end
    return ForEach.arrayList(inner2_function, ARRAY)
end

-- inner_function(item)
function ForEach.hotbarItem(inner_function) -- should not use it yet, use attached items instead
    local PLAYER = getSpecificPlayer(0)
    local HOTBAR = getPlayerHotbar(0)
    local function inner2_function(item, i, size, arrayList, inventory, isoplayer, ret)
        if not HOTBAR:isInHotbar(item) then return nil end
        return inner_function(item)
    end
    return ForEach.inventoryItem(inner2_function, PLAYER) 
end

-- inner_function(item, size, i, arrayList, ret)
function ForEach.attachedItems(inner_function, isochar)
    local AttachedItems = isochar:getAttachedItems()
    local function inner2_function(i, arrayList , size, ret)
        local item = arrayList:get(i)
        return inner_function(item, size, i, arrayList, ret)
    end
    return ForEach.arrayList(inner2_function, AttachedItems)
end

-- inner_function(item, index)
function ForEach.handItem(inner_function, isoplayer) 
    -- primary
    local item = isoplayer:getPrimaryHandItem()
    local inner_function_return = inner_function(item, 1)
    if inner_function_return == true then return end
    if inner_function_return ~= nil then
        if inner_function_return == false then
            return 
        else
            return inner_function_return
        end
    end
    -- secondary
    item = isoplayer:getSecondaryHandItem()
    inner_function_return = inner_function(item , 2)
    if inner_function_return == true then return end
    if inner_function_return ~= nil then
        if inner_function_return == false then
            return 
        else
            return inner_function_return
        end
    end
end

-- inner_function(item)
function ForEach.clothingItem(inner_function, isoplayer) 
    
    local item = isoplayer:getClothingItem_Hands()
    local inner_function_return = inner_function(item)
    if inner_function_return ~= nil then
        if inner_function_return == false then
            return 
        else
            return inner_function_return
        end
    end
    
    item = isoplayer:getClothingItem_Legs()
    inner_function_return = inner_function(item)
    if inner_function_return ~= nil then
        if inner_function_return == false then
            return 
        else
            return inner_function_return
        end
    end
    
    item = isoplayer:getClothingItem_Head()
    inner_function_return = inner_function(item)
    if inner_function_return ~= nil then
        if inner_function_return == false then
            return 
        else
            return inner_function_return
        end
    end
    
    item = isoplayer:getClothingItem_Back()
    inner_function_return = inner_function(item)
    if inner_function_return ~= nil then
        if inner_function_return == false then
            return 
        else
            return inner_function_return
        end
    end
    
    item = isoplayer:getClothingItem_Feet()
    inner_function_return = inner_function(item)
    if inner_function_return ~= nil then
        if inner_function_return == false then
            return 
        else
            return inner_function_return
        end
    end
    
    item = isoplayer:getClothingItem_Torso()
    inner_function_return = inner_function(item)
    if inner_function_return ~= nil then
        if inner_function_return == false then
            return 
        else
            return inner_function_return
        end
    end
    
end

-- inner_function(bodyPart, i, bodyParts, bodydamage, ret)
function ForEach.bodyPart(inner_function, isoplayer)    
    local BODYDAMAGE = isoplayer:getBodyDamage()
    local arrayList = BODYDAMAGE:getBodyParts()
    local function inner2_function(i, arrayList , size, ret)
        local bodyPart = arrayList:get(i)
        return inner_function(bodyPart, i, arrayList, BODYDAMAGE, ret)
    end
    return ForEach.arrayList(inner2_function, arrayList)
end

-- inner_function(bodyPart, i, bodyParts, bodydamage, ret)
function ForEach.bleedingBodyPary(inner_function, isoplayer) 
    local function inner2_function(bodyPart, i, bodyParts, bodydamage, ret)
        if not bodyPart:bleeding() then return nil end
        return inner_function(bodyPart, i, bodyParts, bodydamage, ret)
    end
    return ForEach.bodyPart(inner2_function, isoplayer)
end

-- inner_function(bodyPart, i, bodyParts, bodydamage, ret)
function ForEach.untreatedBodyPart(inner_function, isoplayer) 
    local function inner2_function(bodyPart, i, bodyParts, bodydamage, ret)
        if bodyPart:bandaged() then return nil end
        return inner_function(bodyPart, i, bodyParts, bodydamage, ret)
    end
    return ForEach.bodyPart(inner2_function, isoplayer)
end

-- inner_function(bodyPart, i, bodyParts, bodydamage, ret)
function ForEach.treatedBodyPart(inner_function, isoplayer) 
    local function inner2_function(bodyPart, i, bodyParts, bodydamage, ret)
        if not bodyPart:bandaged() then return nil end
        return inner_function(bodyPart, i, bodyParts, bodydamage, ret)
    end
    return ForEach.bodyPart(inner2_function, isoplayer)
end

-- inner_function(weapon, i, size, items, inventory, isoplayer, ret)
function ForEach.inventoryWeapon(inner_function, isoplayer) 
    local function inner2_function(item, i, size, items, inventory, isoplayer, ret)
        if not item:IsWeapon() then return nil end
        return inner_function(item, i, size, items, inventory, isoplayer, ret)
    end
    return ForEach.inventoryItem(inner2_function, isoplayer) 
end

-- inner_function(weapon, i, size, items, inventory, isoplayer, ret)
function ForEach.rangedInventoryWeapon(inner_function, isoplayer)
    local function inner2_function(weapon, i, size, items, inventory, isoplayer, ret)
        if not weapon:isRanged() then return nil end
        return inner_function(weapon, i, size, items, inventory, isoplayer, ret)
    end
    return ForEach.inventoryWeapon(inner2_function, isoplayer) 
end

-- inner_function(part,i, arrayList , size, ret)
function ForEach.weaponPart(inner_function, weapon) 
    local ARRAY = weapon:getAllWeaponParts()
    local function inner2_function(i, arrayList , size, ret)
        local part = arrayList:get(i)
        return inner_function(part,i, arrayList , size, ret)
    end
    return ForEach.arrayList(inner2_function, ARRAY)
end

function ForEach.inventoryLiterature(inner_function, isoplayer) end
function ForEach.journalPage(inner_function, item) end
function ForEach.inventoryBag(inner_function, isoplayer) end
function ForEach.equipedItem(inner_function, isoplayer) end
function ForEach.statsValues(inner_function, isoplayer) end
function ForEach.isoplayerPerk(inner_function, isoplayer) end

--
-- ====================================================================
-- Foreach Cell Related

-- inner_function(obj, i objects, size, ret)
function ForEach.cellObject(inner_function, cell)
    local cell_obj_list = cell:getObjectList()
    local function inner2_function(i, arrayList , size, ret)
        local obj = arrayList:get(i)
        return inner_function(obj, i, arrayList, size, ret)
    end
    return ForEach.arrayList(inner2_function, cell_obj_list)
end

-- inner_function(vehicle, i, vehicles, size, ret)
function ForEach.cellVehicle(inner_function, cell) 
    local cell_vehicle_list = cell:getVehicles()
    local function inner2_function(i, arrayList , size, ret)
        local obj = arrayList:get(i)
        return inner_function(obj, i, arrayList, size, ret)
    end
    return ForEach.arrayList(inner2_function, cell_vehicle_list)
end

-- inner_function(isozombie, i, zombies, size, ret)
function ForEach.cellZombie(inner_function, cell) 
    local ARRAY = cell:getZombieList()
    local function inner2_function(i, arrayList , size, ret)
        local isozombie = arrayList:get(i)
        return inner_function(isozombie, i, arrayList, size, ret)
    end
    return ForEach.arrayList(inner2_function, ARRAY)
end

-- inner_function(obj, i objects, size, ret)
function ForEach.cellIsoPlayer(inner_function, cell) 
    local function inner2_function(obj, i, objects, size, ret)
        if not instanceof(obj, "IsoPlayer") then return nil end
        return inner_function(obj, i, objects, size, ret)
    end
    return ForEach.cellObject(inner2_function, cell)
end

-- inner_function(room, i, rooms, size, ret)
function ForEach.cellRoom(inner_function, cell) 
    local ARRAY = cell:getRoomList()
    if ARRAY:size() == 0 then return nil end
    local function inner2_function(i, arrayList , size, ret)
        local room = arrayList:get(i)
        return inner_function(room, i, arrayList, size, ret)
    end
    return ForEach.arrayList(inner2_function, ARRAY)
end

-- inner_function(building, i, buildings, size, ret)
function ForEach.cellBuilding(inner_function, cell) 
    local ARRAY = cell:getBuildingList()
    if ARRAY:size() == 0 then return nil end
    local function inner2_function(i, arrayList , size, ret)
        local building = arrayList:get(i)
        return inner_function(building, i, arrayList, size, ret)
    end
    return ForEach.arrayList(inner2_function, ARRAY)
end

--
-- ====================================================================
-- Foreach Room Related

--
-- ====================================================================
-- Foreach Building Related

function ForEach.buildingRoom(inner_function, building) end
function ForEach.buildingDoor(inner_function, building) end
function ForEach.buildingWindow(inner_function, building) end

--
-- ====================================================================
--

--[[
function ForEach.forageIcon(inner_function) 
    return ForEach.genericTable( function () 
    
    
    end , nil )
end
--]]


--
-- ====================================================================
-- 
return ForEach

















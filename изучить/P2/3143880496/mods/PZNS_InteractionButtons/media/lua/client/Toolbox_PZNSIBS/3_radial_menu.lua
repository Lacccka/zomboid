
-- ====================================================================
-- oZumbiAnalitico : 
-- 1. youtube ; https://www.youtube.com/@oZumbiAnalitico
-- 2. steam workshop ; https://steamcommunity.com/id/ozumbianalitico/myworkshopfiles/
-- 3. email ; ericyuta@gmail.com
-- ====================================================================
-- File [ 3_radial_menu.lua ] : Radial Menu

-- Inventory [ .../3_radial_menu.lua ]
-- ---------------------- VARIABLES | FUNCTIONS -----------------------
-- 1. RadialMenu.FAdjacentSquare(inner_function, [ isoplayer ]) o (square, isoplayer)
-- 2. RadialMenu.CKeyPress(inner_function, [ isoplayer ]) o (key,isoplayer, Adjacent)
-- 3. RadialMenu.FInventoryItem(inner_function,isoplayer) o (item, isoplayer, inv_array)
-- 4. RadialMenu.FAdjacentBodies(inner_function, isoplayer) o (body)
-- 5. RadialMenu.FAdjacentObjects(inner_function, [ isoplayer ]) o (object)
-- 6. RadialMenu.hasBodies( [ isoplayer ] )
-- 7. RadialMenu.FAdjBodyItem(inner_function, isoplayer) o (item,body)
-- 8. RadialMenu.FAdjContainer(inner_function, [ isoplayer ]) o (container, items)
-- 9. RadialMenu.hasContainers( [ isoplayer ] )
-- 10. RadialMenu.FAdjContainerItem(inner_function, [ isoplayer ]) o (item, container)
-- 11. RadialMenu.CInGame(inner_function) o (playerObj)
-- 12. RadialMenu.CVehicle(condition, inner_function) o (menu,playerObj,condition)
-- 13. RadialMenu.CustomRadialMenuKeyKeepPress(trigger_key, inner_function, [ isoplayer ]) o (menu)
-- 14. callback <- RadialMenu.custom( inner_function, [ isoplayer ])
-- --------------------------- LIB | VANILLA --------------------------
-- 1. MENU:addSlice(text, texture, callback, isoplayer)
-- local PARENT_DIR = 
-- local RadialMenu = require(PARENT_DIR.."3_radial_menu")

local RadialMenu = {}

--
-- ====================================================================
-- requires

require "ISUI/ISRadialMenu"

--
-- ====================================================================
-- Utilities

-- RadialMenu.FAdjacentSquare(inner_function, [ isoplayer ]) o (square, isoplayer)
function RadialMenu.FAdjacentSquare(inner_function, isoplayer) -- [ok]
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

-- RadialMenu.CKeyPress(inner_function, [ isoplayer ]) o (key,isoplayer, Adjacent)
-- 1. Adjacent ; RadialMenu.FAdjacentSquare(inner_function, [ isoplayer ]) o (square, isoplayer)
function RadialMenu.CKeyPress(inner_function, isoplayer) -- [ok]
    if not isoplayer then isoplayer = getSpecificPlayer(0) end
    local function Adjacent(inner_function_2)
        return RadialMenu.FAdjacentSquare(inner_function_2,isoplayer)
    end   
    Events.OnKeyPressed.Add( function(key) 
        inner_function(key,isoplayer, Adjacent)
    end )
end

-- RadialMenu.FInventoryItem(inner_function,isoplayer) o (item, isoplayer, inv_array) 
function RadialMenu.FInventoryItem(inner_function,isoplayer) -- [ok]
    local inv_array = isoplayer:getInventory():getItems()
    if inv_array:size()==0 then return nil end    
    for i=0, inv_array:size()-1 do
        local item = inv_array:get(i)
        local ret_inner_function = inner_function(item, isoplayer, inv_array) 
        if ret_inner_function == nil then
        else
            if ret_inner_function == true then 
                break 
            else 
                return ret_inner_function
            end
        end
    end
end 

-- RadialMenu.FAdjacentBodies(inner_function, isoplayer) o (body)
function RadialMenu.FAdjacentBodies(inner_function, isoplayer) -- [ok]
    if not isoplayer then isoplayer = getSpecificPlayer(0) end
    return RadialMenu.FAdjacentSquare( function(square) 
        if not square then return nil end -- continue
        if square:isBlockedTo(isoplayer:getCurrentSquare()) then return nil end -- continue
        local bodies_array = square:getDeadBodys()
        for i=0,bodies_array:size()-1 do 
            local body = bodies_array:get(i)
            local inner_function_ret = inner_function(body)
            if inner_function_ret == nil then 
            else 
                return inner_function_ret 
            end
        end
        return nil -- end of iteration
    end , isoplayer)
end

-- RadialMenu.FAdjacentObjects(inner_function, [ isoplayer ]) o (object)
function RadialMenu.FAdjacentObjects(inner_function, isoplayer) -- [ok]
    if not isoplayer then isoplayer = getSpecificPlayer(0) end
    return RadialMenu.FAdjacentSquare( function(square) 
        if not square then return nil end -- continue
        if square:isBlockedTo(isoplayer:getCurrentSquare()) then return nil end -- continue
        local array = square:getObjects()
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

function RadialMenu.FAdjacentMovingObjects(inner_function,isoplayer) -- [ok]
    if not isoplayer then isoplayer = getSpecificPlayer(0) end
    return RadialMenu.FAdjacentSquare( function(square) 
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

--[[ Example ; Check Name of Moving Objects
RadialMenu.CInGame( function(isoplayer)
    Events.EveryOneMinute.Add( function()     
        RadialMenu.FAdjacentMovingObjects( function(object) 
            if not object then return nil end -- continue
            isoplayer:Say( tostring( object ) )
            return nil -- end of iteration
        end )
    end )
end )
--]]

-- RadialMenu.hasBodies( [ isoplayer ] )
function RadialMenu.hasBodies(isoplayer) -- [ok]
    local b_ret = false
    RadialMenu.FAdjacentBodies( function(body) 
        if not body then return nil end -- continue
        b_ret = true
        return true
    end , isoplayer)
    return b_ret
end

-- RadialMenu.FAdjBodyItem(inner_function, isoplayer) o (item,body)
function RadialMenu.FAdjBodyItem(inner_function, isoplayer) -- [ok]
    return RadialMenu.FAdjacentBodies( function(body)
        if not body then return nil end -- continue
        local inventory = body:getContainer()
        if not inventory then return nil end -- continue 
        local items = inventory:getItems()
        if not items or items:size()==0 then return nil end -- continue
        local item = nil
        for i=0, items:size()-1 do 
            item = items:get(i)
            local inner_function_ret = inner_function(item, body)
            if inner_function_ret == nil then 
            else 
                return inner_function_ret
            end
        end
        return nil -- end of iteration
    end, isoplayer)
end

-- RadialMenu.FAdjContainer(inner_function, [ isoplayer ]) o (container, items)
function RadialMenu.FAdjContainer(inner_function, isoplayer) -- [ok]
    if not isoplayer then isoplayer = getSpecificPlayer(0) end
    return RadialMenu.FAdjacentObjects( function(object) 
        if not object then return nil end -- continue
        local container = object:getContainer()
        if not container then return nil end -- continue
        local items_array = container:getItems()
        local ret = inner_function(container, items_array)
        if ret == nil then return nil end -- continue
        return ret
    end, isoplayer )
end

-- RadialMenu.hasContainers( [ isoplayer ] )
function RadialMenu.hasContainers(isoplayer) -- [ok]
    local b_ret = false
    RadialMenu.FAdjContainer( function(container) 
        if not container then return nil end -- continue
        b_ret = true
        return true
    end , isoplayer)
    return b_ret
end

-- RadialMenu.FAdjContainerItem(inner_function, [ isoplayer ]) o (item, container)
function RadialMenu.FAdjContainerItem(inner_function, isoplayer) -- [ok]
    return RadialMenu.FAdjContainer( function(container, items) 
        if not items then return nil end -- continue
        if items:size() == 0 then return nil end -- continue
        for i=0,items:size()-1 do 
            local item = items:get(i)
            local ret = inner_function(item, container)
            if ret == nil then
            else 
                return ret
            end
        end
        return nil -- end of iteration
    end , isoplayer)
end

-- RadialMenu.CInGame(inner_function) o (playerObj)
function RadialMenu.CInGame(inner_function) -- [ok]
    Events.OnGameStart.Add( function() 
        local playerObj = getPlayer(0)
        inner_function(playerObj)
    end )
end

--[[ Example ; Body Counter
Events.OnGameStart.Add( function() 
    RadialMenu.CKeyPress( function(key,isoplayer) 
        local count = 0
        RadialMenu.FAdjacentBodies( function(body) 
            if not body then return nil end -- continue
            count = count + 1
            return nil -- continue
        end )
        isoplayer:Say( "Body Count: "..tostring(count) )
    end )
end )
--]]

--[[ Example ; Has Bodies
Events.OnGameStart.Add( function() 
    RadialMenu.CKeyPress( function(key,isoplayer) 
        local b_ret = RadialMenu.hasBodies()
        isoplayer:Say( "Has Bodies: "..tostring(b_ret) )
    end )
end )
--]]

--[[ Example ; Body Items
Events.OnGameStart.Add( function() 
    RadialMenu.CKeyPress( function(key,isoplayer) 
        RadialMenu.FAdjBodyItem( function(item) 
            isoplayer:Say(tostring( item:getFullType() ))
        end )
    end )
end )
--]]

--[[ Example ; Adjacent Objects
RadialMenu.CInGame( function(isoplayer) 
    RadialMenu.CKeyPress( function(key,isoplayer) 
        RadialMenu.FAdjacentObjects( function(object) 
            if not object then return nil end -- continue
            isoplayer:Say( object:getTextureName() )
            return nil -- end of iteration
        end )
    end )
end )
--]]

--[[ Example ; Adjacent Containers
RadialMenu.CInGame( function(isoplayer) 
    RadialMenu.CKeyPress( function(key,isoplayer) 
        RadialMenu.FAdjContainer( function( container ) 
            isoplayer:Say( tostring( container:getItems():size() ) )
            return nil -- end of iteration
        end )
    end )
end )
--]]

function RadialMenu.hasVehicle(isoplayer) -- [ok]
    local b_ret = false
    RadialMenu.FAdjacentMovingObjects( function(obj) 
        if not obj then return nil end -- continue
        if string.find( tostring(obj),"vehicle" ) then  
            b_ret = true
            return true -- break
        end
        return nil -- continue
    end ,isoplayer)
    return b_ret
end

-- RadialMenu.getAdjMovingObject(string_in_name, [ isoplayer ])
function RadialMenu.getAdjMovingObject(string_in_name, isoplayer) -- [testing]
    local b_ret = nil
    RadialMenu.FAdjacentMovingObjects( function(obj) 
        if not obj then return nil end -- continue
        if string.find( tostring(obj),string_in_name ) then  
            b_ret = obj
            return true -- break
        end
        return nil -- continue
    end ,isoplayer)
    return b_ret
end

-- RadialMenu.printAdjMovingObject([ isoplayer ])
function RadialMenu.printAdjMovingObject(isoplayer) -- [testing]
    RadialMenu.FAdjacentMovingObjects( function(obj) 
        if not obj then return nil end -- continue
        isoplayer:Say( tostring(obj) )
        return nil -- continue
    end ,isoplayer)
end

-- RadialMenu.getAdjObject(string_in_name, [ isoplayer ])
function RadialMenu.getAdjObject(string_in_name, isoplayer) -- [testing]
    local b_ret = nil
    RadialMenu.FAdjacentObjects( function(obj) 
        if not obj then return nil end -- continue
        if string.find( tostring(obj),string_in_name ) then  
            b_ret = obj
            return true -- break
        end
        return nil -- continue
    end ,isoplayer)
    return b_ret
end

-- RadialMenu.printAdjObject([ isoplayer ]) 
function RadialMenu.printAdjObject(isoplayer) -- [testing]
    RadialMenu.FAdjacentObjects( function(obj) 
        if not obj then return nil end -- continue
        isoplayer:Say( tostring(obj) )
        return nil -- continue
    end ,isoplayer)
end

--
-- ====================================================================
-- Wrapping Timed Actions

function RadialMenu.grab(item, isoplayer) -- [testing]
    if not isoplayer then isoplayer = getSpecificPlayer(0) end
    if not isoplayer then return nil end
    if not item then return nil end
    ISTimedActionQueue.add(
        ISInventoryTransferAction:new(isoplayer, item, item:getContainer(), isoplayer:getInventory())
    )
end

function RadialMenu.ripclothing(item, isoplayer) -- [testing]
    if not isoplayer then isoplayer = getSpecificPlayer(0) end
    if not isoplayer then return nil end
    if not item then return nil end
    ISTimedActionQueue.add( ISRipClothing:new(isoplayer, item) )
end

function RadialMenu.harvest(plant, isoplayer) -- [testing]
    if not isoplayer then isoplayer = getSpecificPlayer(0) end
    if not isoplayer then return nil end
    if not plant then return nil end
    ISTimedActionQueue.add( ISHarvestPlantAction:new(isoplayer, plant, 100) )
end

--
-- ====================================================================
-- Radial Menus Overrides

-- RadialMenu.CVehicle(condition, inner_function) o (menu,playerObj,ptr_boolean)
-- ... a ptr_boolean must be a table with ptr_boolean:get() returning a boolean. It's a workaround to pass by reference.
function RadialMenu.CVehicle(ptr_boolean, inner_function) -- [ok]
    local old_radial_function = ISVehicleMenu.showRadialMenuOutside
    ISVehicleMenu.showRadialMenuOutside = function(playerObj)
        if not ptr_boolean:get() then 
            return old_radial_function(playerObj)
        else
            if playerObj:getVehicle() then return old_radial_function(playerObj) end
            local playerIndex = playerObj:getPlayerNum()
            local menu = getPlayerRadialMenu(playerIndex)
            -- For keyboard, toggle visibility
            if menu:isReallyVisible() then
                if menu.joyfocus then
                    setJoypadFocus(playerIndex, nil)
                end
                menu:undisplay()
                return
            end
            menu:clear()
            if inner_function(menu,playerObj,ptr_boolean) == true then return true end
            menu:setX(getPlayerScreenLeft(playerIndex) + getPlayerScreenWidth(playerIndex) / 2 - menu:getWidth() / 2)
            menu:setY(getPlayerScreenTop(playerIndex) + getPlayerScreenHeight(playerIndex) / 2 - menu:getHeight() / 2)
            menu:addToUIManager()
            if JoypadState.players[playerObj:getPlayerNum()+1] then
                menu:setHideWhenButtonReleased(Joypad.DPadUp)
                setJoypadFocus(playerObj:getPlayerNum(), menu)
                playerObj:setJoypadIgnoreAimUntilCentered(true)
            end
        end
    end
end

--[[ Example ; Quick Grab Mod 
local ptr_has_body = {}
function ptr_has_body:get() 
    if RadialMenu.hasVehicle() then return false end -- priority
    if RadialMenu.hasContainers() then return false end -- priority
    return RadialMenu.hasBodies()
end
--
local ptr_has_container = {}
function ptr_has_container:get() 
    if RadialMenu.hasVehicle() then return false end -- priority
    return RadialMenu.hasContainers()
end

RadialMenu.CInGame( function(isoplayer) 
    RadialMenu.CVehicle(ptr_has_body, function(menu) -- radial menu for adjacent bodies items
        local b_repeated = nil
        local old_item = nil
        RadialMenu.FAdjBodyItem( function(item, body) 
            if not item then return nil end -- continue
            if old_item and item:getFullType() == old_item:getFullType() then 
                b_repeated = true
            else 
                b_repeated = false
            end
            if b_repeated then return nil end -- continue
            menu:addSlice( "Grab: "..item:getName(), item:getTex(), function() 
                RadialMenu.grab(item)
            end , isoplayer )
            old_item = item
            return nil -- end of iteration
        end , isoplayer)
    end )
    RadialMenu.CVehicle(ptr_has_container, function(menu) -- radial menu for adjacent container items
        local b_repeated = nil
        local old_item = nil
        RadialMenu.FAdjContainerItem( function(item, container) 
            if not item then return nil end -- continue
            if old_item and item:getFullType() == old_item:getFullType() then 
                b_repeated = true
            else 
                b_repeated = false
            end
            if b_repeated then return nil end -- continuedddf
            menu:addSlice( "Grab: "..item:getName(), item:getTex(), function() 
                RadialMenu.grab(item)
            end , isoplayer )
            old_item = item
            return nil -- end of iteration
        end , isoplayer)
    end )
end )
--]]

-- RadialMenu.CHotbar(inner_function) o (menu, playerObj)
function RadialMenu.CHotbar(inner_function) -- [testing]
    local old_onKeyKeepPressed = ISHotbar.onKeyKeepPressed
    ISHotbar.onKeyKeepPressed = function(key) 
        old_onKeyKeepPressed(key)
        local playerObj = getSpecificPlayer(0)
        if not getPlayerHotbar(0) or not playerObj or playerObj:isDead() then
            return
        end
        if UIManager.getSpeedControls() and (UIManager.getSpeedControls():getCurrentGameSpeed() == 0) then
            return
        end
        if JoypadState.players[1] then
            return
        end
        if playerObj:isAttacking() then
            return
        end
        local queue = ISTimedActionQueue.queues[playerObj]
        if queue and #queue.queue > 0 then
            return
        end
        if getPlayerHotbar(0).radialWasVisible then
            return
        end
        local self = getPlayerHotbar(0);
        local radialMenu = getPlayerRadialMenu(0)
        local timestampsMS = getTimestampMs()
        if not timestampsMS then return nil end
        if not self.keyPressedMS then return nil end
        if (timestampsMS - self.keyPressedMS > 500) and not radialMenu:isReallyVisible() then
            inner_function(radialMenu, playerObj)
        end
    end
end

-- RadialMenu.CFirearm(inner_function) o (menu)
function RadialMenu.CFirearm(inner_function) -- [testing]
    local old_radial_fillMenu = ISFirearmRadialMenu.fillMenu
    function ISFirearmRadialMenu:fillMenu()
        old_radial_fillMenu(self)
        local menu = getPlayerRadialMenu(self.playerNum)
        inner_function(menu)
    end
end

--
-- ====================================================================
-- Custom On Key Keep Press Radial Menu

-- RadialMenu.CustomRadialMenuKeyKeepPress(trigger_key, inner_function, [ isoplayer ]) o (menu)
function RadialMenu.CustomRadialMenuKeyKeepPress(trigger_key, inner_function, isoplayer) -- [ok]
    local STATE = {}
    if not isoplayer then isoplayer = getSpecificPlayer(0) end
    local playerNum = isoplayer:getPlayerNum()
    local function checkKey(key) -- [testing]
        if key ~= trigger_key then
            return false
        end
        if isGamePaused() then
            return false
        end
        local playerObj = getSpecificPlayer(0)
        if not playerObj or playerObj:isDead() then
            return false
        end
        local queue = ISTimedActionQueue.queues[playerObj]
        if queue and #queue.queue > 0 then
            return false
        end
        if getCell():getDrag(0) then
            return false
        end
        return true
    end
    --     
    Events.OnKeyStartPressed.Add( function(key)
        if not checkKey(key) then return end
        local radialMenu = getPlayerRadialMenu(0)
        if getCore():getOptionRadialMenuKeyToggle() and radialMenu:isReallyVisible() then
            STATE.radialWasVisible = true
            radialMenu:removeFromUIManager()
            return
        end
        STATE.keyPressedMS = getTimestampMs()
        STATE.radialWasVisible = false
    end )    
    Events.OnKeyKeepPressed.Add( function(key) 
        if not checkKey(key) then return end
        if STATE.radialWasVisible then return end
        if not STATE.keyPressedMS then return end
        local radialMenu = getPlayerRadialMenu(0)
        local delay = 500
        if (getTimestampMs() - STATE.keyPressedMS >= delay) and not radialMenu:isReallyVisible() then
            -- show radial || fillmenu | display
            local menu = getPlayerRadialMenu(0)
            do -- fillmenu
                -- For keyboard, toggle visibility
                if menu:isReallyVisible() then
                    if menu.joyfocus then
                        setJoypadFocus(playerNum, nil)
                    end
                    menu:undisplay()
                    return
                end
                menu:clear()
                inner_function(menu)
            end
            do -- display
                menu:setX(getPlayerScreenLeft(playerNum) + getPlayerScreenWidth(playerNum) / 2 - menu:getWidth() / 2)
                menu:setY(getPlayerScreenTop(playerNum) + getPlayerScreenHeight(playerNum) / 2 - menu:getHeight() / 2)
                menu:addToUIManager()
                if JoypadState.players[playerNum + 1] then
                    menu:setHideWhenButtonReleased(Joypad.DPadDown)
                    setJoypadFocus(0, menu)
                    self.character:setJoypadIgnoreAimUntilCentered(true)
                end
            end
        end
    end )
    Events.OnKeyPressed.Add( function() 
        if not checkKey(key) then
            return
        end
        if not STATE.keyPressedMS then
            return
        end
        local radialMenu = getPlayerRadialMenu(0)
        if radialMenu:isReallyVisible() or STATE.radialWasVisible then
            if not getCore():getOptionRadialMenuKeyToggle() then
                radialMenu:removeFromUIManager()
            end
            return
        end
        if getTimestampMs() - STATE.keyPressedMS < 500 then
            ItemBindingHandler.toggleLight(key)
        end
        STATE.keyPressedMS = nil
    end )
end

--[[ Example ; KeyPress button N
RadialMenu.CInGame( function(isoplayer) 
    RadialMenu.CustomRadialMenuKeyPress(Keyboard.KEY_N, function(menu) 
        RadialMenu.FAdjBodyItem( function(item, body) 
            if not item then return nil end -- continue
            if old_item and item:getFullType() == old_item:getFullType() then 
                b_repeated = true
            else 
                b_repeated = false
            end
            if b_repeated then return nil end -- continue
            menu:addSlice( "Grab: "..item:getName(), item:getTex(), function() 
                RadialMenu.grab(item)
            end , isoplayer )
            old_item = item
            return nil -- end of iteration
        end , isoplayer)
    end )
end )
--]]

--
-- ====================================================================
-- Custom Radial Menu 

-- callback <- RadialMenu.custom( inner_function, [ isoplayer ])
function RadialMenu.custom( inner_function, isoplayer ) -- [testing]
    return function()
        if not isoplayer then isoplayer = getSpecificPlayer(0) end
        -- 
        local playerIndex = isoplayer:getPlayerNum()
        local menu = getPlayerRadialMenu(playerIndex)
        -- For keyboard, toggle visibility
        if menu:isReallyVisible() then
            if menu.joyfocus then
                setJoypadFocus(playerIndex, nil)
            end
            menu:undisplay()
            return
        end
        menu:clear()
        if inner_function(menu,isoplayer,ptr_boolean) == true then return true end
        menu:setX(getPlayerScreenLeft(playerIndex) + getPlayerScreenWidth(playerIndex) / 2 - menu:getWidth() / 2)
        menu:setY(getPlayerScreenTop(playerIndex) + getPlayerScreenHeight(playerIndex) / 2 - menu:getHeight() / 2)
        menu:addToUIManager()
        if JoypadState.players[isoplayer:getPlayerNum()+1] then
            menu:setHideWhenButtonReleased(Joypad.DPadUp)
            setJoypadFocus(isoplayer:getPlayerNum(), menu)
            isoplayer:setJoypadIgnoreAimUntilCentered(true)
        end
    end
end

--[[ Example ; Radial Menu Asigned with Keyboard.KEY_1 
local Radial_Menu_Key1 = nil
-- build the function Radial_Menu_Key1
RadialMenu.CInGame( function(isoplayer) 
    Radial_Menu_Key1 = RadialMenu.custom( function(menu) 
        RadialMenu.FInventoryItem( function(item) 
            menu:addSlice("display test message", item:getTex(), function() 
                isoplayer:Say("Custom Radial Menu:"..item:getFullType() )
            end )
        end, isoplayer )
    end )
end )
-- Register callback for Radial_Menu_Key1 on key press
Events.OnKeyPressed.Add( function(key) 
    if key ~= Keyboard.KEY_1 then return nil end
    Radial_Menu_Key1()
end )
--]]

--
-- ====================================================================
--

return RadialMenu























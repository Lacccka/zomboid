-- From "More Damaged Objects [B42]" mod -- Author = carlesturo

local MDO_Utils = {}

-- ------------------------------------------------------------------------------------------------

-- **************** MORE DAMAGED OBJECTS - UTILS ****************

function MDO_Utils.replaceAllObjectsBySprite(square, oldSpriteName, newSpriteName) -- Replace All Objects by Sprite
    local objects = square:getObjects()
    local cell = getCell()
    local replacedAny = false

    local objectsToReplace = {}

    for i = 0, objects:size() - 1 do
        local obj = objects:get(i)
        if obj and obj:getSprite() and obj:getSprite():getName() == oldSpriteName then
            table.insert(objectsToReplace, obj)
        end
    end

    for _, obj in ipairs(objectsToReplace) do
        obj:setSpriteFromName(newSpriteName)
		obj:transmitUpdatedSpriteToServer()
        replacedAny = true
    end

    if replacedAny then
        local lightSource = cell:getLightSourceAt(square:getX(), square:getY(), square:getZ())
        if lightSource then
            cell:removeLamppost(lightSource)

			--local player = getPlayer()
			--local args = { x = square:getX(), y = square:getY(), z = square:getZ() }
			--sendClientCommand(player, "MDO", "RemoveLampPost", args)
        end
    end

    return replacedAny
end

function MDO_Utils.removeAllObjectsBySprite(square, spriteName) -- Remove All Objects by Sprite
    local objects = square:getObjects()
    local objectsToRemove = {}

    for i = 0, objects:size() - 1 do
        local obj = objects:get(i)
        if obj and obj:getSprite() and obj:getSprite():getName() == spriteName then
            table.insert(objectsToRemove, obj)
        end
    end

    for _, obj in ipairs(objectsToRemove) do
        square:transmitRemoveItemFromSquare(obj)
    end

    return #objectsToRemove > 0
end

function MDO_Utils.addObjectToSquare(square, spriteName) -- Add Object to Square
    local newObj = IsoObject.new(square, spriteName, nil, false)
    square:AddTileObject(newObj)
	newObj:transmitCompleteItemToClients()
end

function MDO_Utils.removeAllAndAddObjectInSquare(square, oldSpriteName, newSpriteName) -- Remove All and Add Object in Square
    if MDO_Utils.removeAllObjectsBySprite(square, oldSpriteName) then
        MDO_Utils.addObjectToSquare(square, newSpriteName)
    end
end

function MDO_Utils.removeAllObjectsBySpriteList(square, spriteList) -- Remove All Objects by Sprite List
    if not square then return false end
	local objects = square:getObjects()
    local objectsToRemove = {}

    local spriteSet = {}
    for _, spriteName in ipairs(spriteList) do
        spriteSet[spriteName] = true
    end

    for i = 0, objects:size() - 1 do
        local obj = objects:get(i)
        if obj and obj:getSprite() then
            local spriteName = obj:getSprite():getName()
            if spriteSet[spriteName] then
                table.insert(objectsToRemove, obj)
            end
        end
    end

    for _, obj in ipairs(objectsToRemove) do
        square:transmitRemoveItemFromSquare(obj)
    end

    return #objectsToRemove > 0
end

-- ------------------------------------------------------------------------------------------------
-- Author = albion

-- **************** MORE DAMAGED OBJECTS - UTILS - WATER SHUT OFF ****************

function MDO_Utils.isWaterShutOff()
    local shutOffDate = SandboxVars.WaterShutModifier
    shutOffDate = shutOffDate >= 0 and shutOffDate or 0

    local gameTime = getGameTime()
    local worldAgeDays = gameTime:getWorldAgeHours() / 24
    worldAgeDays = math.floor(worldAgeDays + 0.5)

    return (worldAgeDays - shutOffDate) >= 0
end

-- ------------------------------------------------------------------------------------------------
-- From "Zomboid Forge [B42]" mod -- Author = Sir Doggy Jvla
-- Modified by carlesturo

-- **************** MORE DAMAGED OBJECTS - UTILS - DELAY FUNCTION ****************

MDO_Utils.DelayedActions = {ActionList = {},}
local DelayedActions = MDO_Utils.DelayedActions



DelayedActions.AddNewAction = function(newAction)
    table.insert(DelayedActions.ActionList, newAction)
    
    if #DelayedActions.ActionList == 1 then
        Events.OnTick.Add(DelayedActions.UpdateDelayedActions)
    end
end

DelayedActions.UpdateDelayedActions = function()
    local ActionList = DelayedActions.ActionList
    for i = #ActionList,1,-1 do
        local action = ActionList[i]

        --- VERIFY IF SHOULD RUN ACTION ---
        local pass

        repeat -- used to be able to skip other checks if one passes
            -- check tick delay passes
            local ticks = action._ticksDelay
            if ticks then
                action._ticksDelay = ticks - 1
                if action._ticksDelay <= 0 then
                    pass = true
                    break
                end
            end

            -- cache check function
            local _shouldRun = action._shouldRun
            if _shouldRun then
                local fct = _shouldRun.fct
                if fct and fct(action) then
                    -- reduce validation function ticks if present
                    local ticks = _shouldRun.ticks
                    if ticks then
                        _shouldRun.ticks = ticks - 1
                    end

                    -- if uses delay per validation, verify it passes
                    if not ticks or _shouldRun.ticks <= 0 then
                        pass = true
                    end
                end
            end
        until true

        -- check if action is valid to be ran
        if pass then
            action.fct(unpack(action.args)) -- unpack variables to be read by function
            table.remove(ActionList,i) -- remove action from the list
        end
    end
	
	if #DelayedActions.ActionList == 0 then
		Events.OnTick.Remove(DelayedActions.UpdateDelayedActions)
	end
end






--- _shouldRun FUNCTIONS ---

---Function used to verify if the zombie had its model loaded in to set visuals.
---@param action table
---@return boolean
MDO_Utils.IsValidForInitialization = function(action)
    -- action.args[1] needs to be IsoZombie
    return action.args[1]:hasActiveModel()
end



function MDO_Utils.DelayFunction(fct, ticks)
    ticks = ticks or 1
    DelayedActions.AddNewAction({
        fct = fct,
        _ticksDelay = ticks,
        args = {}
    })
end

-- ------------------------------------------------------------------------------------------------

return MDO_Utils
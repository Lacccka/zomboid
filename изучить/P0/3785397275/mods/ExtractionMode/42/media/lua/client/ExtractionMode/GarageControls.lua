require "ExtractionMode/Config"
require "ExtractionMode/Util"
require "ExtractionMode/Localization"
require "ExtractionMode/GaragePanel"
require "ISUI/ISToolTip"
require "TimedActions/ISTimedActionQueue"
require "Vehicles/TimedActions/ISPathFindAction"
require "ISUI/ISWorldObjectContextMenu"
require "ISUI/ISButtonPrompt"

ExtractionMode = ExtractionMode or {}

local Config = ExtractionMode.Config
local Util = ExtractionMode.Util
local Localization = ExtractionMode.Localization
local controlMarker = nil
local lastRefreshAt = 0
local interactHeldByPlayer = {}
local lastControlOpenAt = {}
local playerWasOnControlTile = {}

local function controlPoint()
    return Config.hideoutGarageControl()
end

local function clearControlHighlight()
    if controlMarker ~= nil then
        pcall(function() controlMarker:remove() end)
    end
    controlMarker = nil
end

local function localPlayersNearControl()
    local point = controlPoint()
    local hideout = Config.hideout()
    local playerNums = {}
    local playerCount = 0
    for _, player in ipairs(Util.players()) do
        local localPlayer = false
        pcall(function() localPlayer = player:isLocalPlayer() == true end)
        if localPlayer and Util.playerNear(player, hideout,
            math.max(8, tonumber(hideout.radius) or 14) + 12) then
            local playerNum = 0
            pcall(function() playerNum = player:getPlayerNum() end)
            if not playerNums[playerNum] then
                playerNums[playerNum] = true
                playerCount = playerCount + 1
            end
        end
    end
    return playerNums, point, playerCount
end

local function refreshControlHighlight()
    local playerNums, point, playerCount = localPlayersNearControl()
    local cell = getCell and getCell() or nil
    -- Kahlua does not expose Lua's global next() in every runtime context.
    -- Keep an explicit count so this startup/tick path cannot call a nil global.
    if playerCount == 0 or cell == nil then
        clearControlHighlight()
        return
    end
    local x, y, z = math.floor(point.x), math.floor(point.y), math.floor(point.z)
    local square = cell:getGridSquare(x, y, z)
    if square == nil or square:getFloor() == nil then
        clearControlHighlight()
        return
    end

    -- Grid-square markers are intentionally visible through geometry. Only
    -- keep this one alive when at least one nearby local player currently has
    -- line of sight to the control square, so it cannot reveal the tile
    -- through an intact wall.
    local visible = false
    for playerNum = 0, 3 do
        if playerNums[playerNum] then
            pcall(function()
                visible = visible or square:getCanSee(playerNum) == true
            end)
        end
    end
    if not visible then
        clearControlHighlight()
        return
    end

    if controlMarker == nil and getWorldMarkers ~= nil then
        pcall(function()
            -- Grid-square markers are rendered in the world floor pass. Unlike
            -- IsoObject outline highlights, they do not x-ray through walls or
            -- draw over furniture and characters in front of the tile.
            controlMarker = getWorldMarkers():addGridSquareMarker(
                square, 0.22, 0.55, 1.0, false, 0.85)
        end)
    end
end

local function objectSquare(worldObject)
    if worldObject == nil then return nil end
    local square = nil
    pcall(function() square = worldObject:getSquare() end)
    return square
end

local function clickedControlTile(worldObjects)
    local point = controlPoint()
    local x, y, z = math.floor(point.x), math.floor(point.y), math.floor(point.z)
    for _, worldObject in ipairs(worldObjects or {}) do
        local square = objectSquare(worldObject)
        if square ~= nil and square:getX() == x and square:getY() == y
            and square:getZ() == z then return true end
    end
    return false
end

local function stateFor(playerNum)
    if ExtractionMode.Client and ExtractionMode.Client.stateFor then
        return ExtractionMode.Client.stateFor(playerNum)
    end
    return ExtractionMode.ClientState or {}
end

local function openControls(player)
    local playerNum = 0
    pcall(function() playerNum = player and player:getPlayerNum() or 0 end)
    lastControlOpenAt[playerNum] = Util.nowMs()
    if ExtractionMode.Client and ExtractionMode.Client.sendCommand then
        ExtractionMode.Client.sendCommand(player, "RequestState", {})
    end
    if ExtractionMode.openGaragePanel then ExtractionMode.openGaragePanel() end
end

local function playerOnControlTile(player)
    local point = controlPoint()
    return player ~= nil
        and math.floor(player:getX()) == math.floor(point.x)
        and math.floor(player:getY()) == math.floor(point.y)
        and math.floor(player:getZ()) == math.floor(point.z)
end

local function finishControlWalk(player)
    if playerOnControlTile(player) then openControls(player) end
end

local function walkToControls(player)
    if playerOnControlTile(player) then
        openControls(player)
        return
    end
    local point = controlPoint()
    local square = getCell() and getCell():getGridSquare(
        math.floor(point.x), math.floor(point.y), math.floor(point.z)) or nil
    if square == nil then return end
    local action = ISPathFindAction:pathToLocationF(player,
        math.floor(point.x) + 0.5, math.floor(point.y) + 0.5, math.floor(point.z))
    action:setOnComplete(finishControlWalk, player)
    ISTimedActionQueue.add(action)
end

local function onWorldContext(playerNum, context, worldObjects, test)
    if test or not clickedControlTile(worldObjects) then return end
    local player = getSpecificPlayer and getSpecificPlayer(playerNum) or nil
    if player == nil or player:isDead() then return end
    if stateFor(playerNum).state ~= Config.STATE_HIDEOUT then return end

    local option = context:addOption(Localization.get(
        "ContextMenu_ExtractionMode_GarageControls", "Garage Controls..."),
        player, walkToControls)
    option.toolTip = ISToolTip:new()
    option.toolTip.description = playerOnControlTile(player)
        and "Open the garage controls."
        or "Walk to the outlined tile and use the garage controls."
end

local function playerCanUseControls(player)
    if player == nil or player:isDead() or not playerOnControlTile(player) then
        return false
    end
    local playerNum = 0
    pcall(function() playerNum = player:getPlayerNum() end)
    return stateFor(playerNum).state == Config.STATE_HIDEOUT
end

local function showControlHint(player)
    local message = Localization.get("IGUI_ExtractionMode_GarageControlHint",
        "Interact to open the Garage Controls")
    local shown = pcall(function()
        player:setHaloNote(message, 137, 196, 255, 128)
    end)
    if not shown and HaloTextHelper then
        pcall(function() HaloTextHelper.addGoodText(player, message) end)
    end
end

local function onKeyPressed(key)
    if getCore == nil or not getCore():isKey("Interact", key) then return end
    -- Keyboard input belongs to player 0; additional local players use their
    -- individual controller bindings handled below.
    local player = getSpecificPlayer and getSpecificPlayer(0) or nil
    if playerCanUseControls(player) then openControls(player) end
end

local function onTick()
    for _, player in ipairs(Util.players()) do
        local localPlayer = false
        pcall(function() localPlayer = player:isLocalPlayer() == true end)
        if localPlayer then
            local playerNum = 0
            local joypadID = -1
            pcall(function()
                playerNum = player:getPlayerNum()
                joypadID = player:getJoypadBind()
            end)
            local onControlTile = playerCanUseControls(player)
            if onControlTile and not playerWasOnControlTile[playerNum] then
                showControlHint(player)
            end
            playerWasOnControlTile[playerNum] = onControlTile
            local interactDown = false
            if joypadID ~= -1 and CharacterJoypadButtonBinding
                and CharacterJoypadButtonBinding.Interact then
                pcall(function()
                    interactDown = CharacterJoypadButtonBinding.Interact:isDown(joypadID) == true
                end)
            end
            if interactDown and not interactHeldByPlayer[playerNum]
                and onControlTile then
                local lastOpen = tonumber(lastControlOpenAt[playerNum]) or 0
                if Util.nowMs() - lastOpen > 150 then openControls(player) end
            end
            interactHeldByPlayer[playerNum] = interactDown
        end
    end

    local now = Util.nowMs()
    if now - lastRefreshAt < 100 then return end
    lastRefreshAt = now
    refreshControlHighlight()
end

local function clearControlState()
    clearControlHighlight()
    interactHeldByPlayer = {}
    lastControlOpenAt = {}
    playerWasOnControlTile = {}
end

-- Build 42 regenerates prompts once for every controller binding. Hook the
-- outer pass so later prompt calculations cannot overwrite the garage action.
if ISButtonPrompt ~= nil and not ExtractionMode.GarageControlBindingInstalled then
    ExtractionMode.GarageControlBindingInstalled = true
    local originalBestBinding = ISButtonPrompt.getBestButtonBindingAction
    function ISButtonPrompt:getBestButtonBindingAction(buttonBinding, dir)
        local player = getSpecificPlayer and getSpecificPlayer(self.player) or nil
        if playerCanUseControls(player) and player:getVehicle() == nil then
            self:clearButtonBindings()
            self:setButtonBindingPrompt(CharacterJoypadButtonBinding.Interact,
                Localization.get("ContextMenu_ExtractionMode_GarageControls",
                    "Garage Controls..."), openControls, player)
            return
        end
        return originalBestBinding(self, buttonBinding, dir)
    end
end

Events.OnTick.Add(onTick)
Events.OnKeyPressed.Add(onKeyPressed)
Events.OnFillWorldObjectContextMenu.Add(onWorldContext)
Events.OnDisconnect.Add(clearControlState)
Events.OnMainMenuEnter.Add(clearControlState)

ExtractionMode.GarageControls = {
    refreshHighlight = refreshControlHighlight,
    clearHighlight = clearControlHighlight,
}

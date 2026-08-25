require "ExtractionMode/Config"
require "ExtractionMode/Upgrades"
require "ExtractionMode/Localization"
require "TimedActions/ISToggleLightAction"
require "ISUI/ISWorldObjectContextMenu"
require "ISUI/ISButtonPrompt"

ExtractionMode = ExtractionMode or {}
local Config = ExtractionMode.Config
local Upgrades = ExtractionMode.Upgrades
local Localization = ExtractionMode.Localization
local Lighting = {}
local lastLockedMessageAt = 0

function Lighting.switchInsideHideout(object)
    local square = object and object:getSquare()
    if square == nil then return false end
    local bounds = Config.hideoutCellBounds()
    return square:getX() >= bounds.minX and square:getX() < bounds.maxXExclusive
        and square:getY() >= bounds.minY and square:getY() < bounds.maxYExclusive
end

-- Build 42 represents both fixed room switches and individual lamps as
-- IsoLightSwitch. Some placed lamp sprites lose the IsMoveAble/customizable
-- marker after a save/load, so use every stable distinction exposed by the
-- object instead of relying on getCanBeModified() alone.
function Lighting.isIndividualLamp(object)
    if object == nil or not instanceof(object, "IsoLightSwitch") then return false end
    local lamp = false
    pcall(function() if object:getCanBeModified() or object:getUseBattery() then lamp = true end end)
    pcall(function()
        local lightRoom = object.lightRoom
        if lightRoom == false then lamp = true end
    end)
    pcall(function()
        local sprite = object:getSprite()
        local properties = sprite and sprite:getProperties()
        if properties and (properties:has(IsoPropertyType.RED_LIGHT)
            or properties:has("lightR") or properties:has("LightR")) then
            lamp = true
        end
    end)
    pcall(function()
        local lights = object:getLights()
        if lights and lights:size() > 0 then lamp = true end
    end)
    return lamp
end

function Lighting.upgradeControlsSwitch(object)
    return object ~= nil and instanceof(object, "IsoLightSwitch")
        and not Lighting.isIndividualLamp(object)
end

function Lighting.lockReason(object)
    if not Lighting.switchInsideHideout(object) then return nil end
    -- Movable lamps use vanilla power/battery validation. The mod only gates
    -- fixed building lighting behind the upgrade and pseudo-generator.
    if not Lighting.upgradeControlsSwitch(object) then return nil end
    local state = ExtractionMode.ClientState or {}
    if not Upgrades.isInstalled(state.upgrades, "lighting") then return "upgrade" end
    local generator = state.generator or {}
    if generator.running ~= true or (tonumber(generator.fuel) or 0) <= 0.0001 then return "generator" end
    return nil
end

function Lighting.switchLocked(object)
    return Lighting.lockReason(object) ~= nil
end

function Lighting.forceSwitchState(object, activated)
    if object == nil or not instanceof(object, "IsoLightSwitch") then return false end
    local changed = false
    local ok = pcall(function()
        local current = object:isActivated()
        local correctionNeeded = current ~= activated
        local square = object:getSquare()
        local room = square and square:getRoom()
        local definition = room and room:getRoomDef()
        if definition and definition.lightsActive ~= activated then correctionNeeded = true end
        local lights = object:getLights()
        if lights then
            for index = 0, lights:size() - 1 do
                if lights:get(index):isActive() ~= activated then
                    correctionNeeded = true
                    break
                end
            end
        end
        if correctionNeeded then
            if current ~= activated then object:setActive(activated, false, true) end
            object:switchLight(object:isActivated())
            changed = true
        end
    end)
    return ok and changed
end

local function blockedTurnOn(object)
    if not Lighting.switchLocked(object) then return false end
    local activated = false
    pcall(function() activated = object:isActivated() end)
    return not activated
end

local function showLockedMessage(object)
    local now = getTimestampMs and getTimestampMs() or 0
    if now - lastLockedMessageAt < 750 then return end
    lastLockedMessageAt = now
    local message
    if Lighting.lockReason(object) == "generator" then
        message = Localization.get("IGUI_ExtractionMode_HideoutPowerOffline",
            "Hideout power is offline. Start and fuel the generator from Hideout Upgrades.")
    else
        message = Localization.get("IGUI_ExtractionMode_HideoutLightingOffline",
            "Hideout lighting is offline. Install the Hideout Lighting upgrade.")
    end
    if ExtractionMode.Client and ExtractionMode.Client.showMessage then
        ExtractionMode.Client.showMessage(message, true)
        return
    end
    local player = getPlayer and getPlayer()
    if player and HaloTextHelper then pcall(function() HaloTextHelper.addBadText(player, message) end) end
end

if not ExtractionMode.HideoutLightingGuardInstalled then
    ExtractionMode.HideoutLightingGuardInstalled = true

    local originalIsValid = ISToggleLightAction.isValid
    function ISToggleLightAction:isValid()
        if blockedTurnOn(self.object) then
            showLockedMessage(self.object)
            return false
        end
        return originalIsValid(self)
    end

    local originalContextToggle = ISWorldObjectContextMenu.onToggleLight
    ISWorldObjectContextMenu.onToggleLight = function(worldobjects, light, player)
        if blockedTurnOn(light) then
            showLockedMessage(light)
            return
        end
        return originalContextToggle(worldobjects, light, player)
    end

    local originalButtonToggle = ISButtonPrompt.cmdToggleLight
    function ISButtonPrompt:cmdToggleLight(light)
        if blockedTurnOn(light) then
            showLockedMessage(light)
            return
        end
        return originalButtonToggle(self, light)
    end
end

ExtractionMode.HideoutLighting = Lighting
return Lighting

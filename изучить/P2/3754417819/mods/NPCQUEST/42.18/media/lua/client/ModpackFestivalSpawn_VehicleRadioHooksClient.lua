-- Vehicle radio: Tune In -> random Restless segment; one-time auto-tune on enter if radio already on.

if isServer() and not isClient() then
    return
end

local MOD_ID = "ModpackFestivalSpawn"
local VR = ModpackFestivalVehicleRadio
local Restless = ModpackFestivalRestlessRadio

local lastVehicleByPlayer = {}

local function getVehicleFromRadioAction(action)
    if not action or not action.deviceData then
        return nil
    end
    local deviceData = action.deviceData
    if not deviceData.isVehicleDevice or not deviceData:isVehicleDevice() then
        return nil
    end
    local part = deviceData.getParent and deviceData:getParent()
    if not part or not part.getVehicle then
        return nil
    end
    return part:getVehicle()
end

local function playRandomRestlessSegment(vehicle, forceNew)
    if Restless and Restless.isSilenceTestMode and Restless.isSilenceTestMode() then
        if vehicle and Restless.silenceVehicleRadioIfOn then
            Restless.silenceVehicleRadioIfOn(vehicle)
        end
        return false
    end
    if not vehicle or not Restless or not Restless.playOnVehicle then
        return false
    end
    if Restless.silenceVehicleRadioIfOn then
        Restless.silenceVehicleRadioIfOn(vehicle)
    end
    if not forceNew and Restless.isAnySegmentPlaying
        and Restless.isAnySegmentPlaying(vehicle) then
        return true
    end
    return Restless.playOnVehicle(vehicle, forceNew == true) == true
end

local function onTuneInChannel(action, channel)
    if not Restless or not Restless.isRestlessChannel then
        return
    end
    if not Restless.isRestlessChannel(channel) then
        return
    end
    local vehicle = getVehicleFromRadioAction(action)
    if vehicle then
        playRandomRestlessSegment(vehicle, true)
    end
end

local function silenceRadioOnAction(action)
    local vehicle = getVehicleFromRadioAction(action)
    if vehicle and Restless and Restless.silenceVehicleRadioIfOn then
        Restless.silenceVehicleRadioIfOn(vehicle)
    end
end

local function hookRadioTuneIn()
    if not ISRadioAction or ISRadioAction._modpackFestivalTuneHooked then
        return
    end
    local origPerform = ISRadioAction.performSetChannel
    local origStart = ISRadioAction.startSetChannel
    function ISRadioAction:performSetChannel()
        local restless = self.mode == "SetChannel" and type(self.secondaryItem) == "number"
            and Restless and Restless.isRestlessChannel(self.secondaryItem)
        if restless and self.deviceData and self.deviceData.isVehicleDevice
            and self.deviceData:isVehicleDevice() then
            if self:isValidSetChannel() then
                self.deviceData:setChannel(self.secondaryItem)
            end
            if Restless and Restless.stopVanillaRadioStatic then
                Restless.stopVanillaRadioStatic(getVehicleFromRadioAction(self), nil)
            end
            onTuneInChannel(self, self.secondaryItem)
        else
            origPerform(self)
            if restless then
                onTuneInChannel(self, self.secondaryItem)
            end
        end
        silenceRadioOnAction(self)
    end
    function ISRadioAction:startSetChannel()
        local restless = self.mode == "SetChannel" and type(self.secondaryItem) == "number"
            and Restless and Restless.isRestlessChannel(self.secondaryItem)
        if restless and self.deviceData and self.deviceData.isVehicleDevice
            and self.deviceData:isVehicleDevice() then
            return
        end
        origStart(self)
    end
    local origToggle = ISRadioAction.performToggleOnOff
    function ISRadioAction:performToggleOnOff()
        origToggle(self)
        silenceRadioOnAction(self)
        if not self.deviceData or not self.deviceData.isVehicleDevice
            or not self.deviceData:isVehicleDevice() then
            return
        end
        if not self.deviceData:getIsTurnedOn() then
            return
        end
        if not Restless or not Restless.isRestlessChannel
            or not Restless.isRestlessChannel(self.deviceData:getChannel()) then
            return
        end
        local vehicle = getVehicleFromRadioAction(self)
        silenceRadioOnAction(self)
        if vehicle and Restless.playOnVehicle then
            Restless.playOnVehicle(vehicle, true)
        end
    end
    ISRadioAction._modpackFestivalTuneHooked = true
end

local function onEnterVehicle(character)
    if not character or not instanceof(character, "IsoPlayer") or not character:isLocalPlayer() then
        return
    end
    local vehicle = character:getVehicle()
    if not vehicle then
        return
    end
    local pn = character.getPlayerNum and character:getPlayerNum() or 0
    lastVehicleByPlayer[pn] = vehicle
    if VR and VR.ensureRestlessRadioOnEnter then
        VR.ensureRestlessRadioOnEnter(vehicle)
    elseif VR and VR.autoTuneOnEnter then
        VR.autoTuneOnEnter(vehicle)
    end
    playRandomRestlessSegment(vehicle, true)
end

local function onExitVehicle(character)
    if not character or not instanceof(character, "IsoPlayer") or not character:isLocalPlayer() then
        return
    end
    local pn = character.getPlayerNum and character:getPlayerNum() or 0
    lastVehicleByPlayer[pn] = nil
end

local function tryHook()
    if not ISRadioAction then
        require "RadioCom/ISRadioAction"
    end
    hookRadioTuneIn()
end

Events.OnGameStart.Add(tryHook)
Events.OnEnterVehicle.Add(onEnterVehicle)
Events.OnExitVehicle.Add(onExitVehicle)
tryHook()

print("[" .. MOD_ID .. "] vehicle radio hooks (all cars: on + 88.880 FM on enter/spawn)")

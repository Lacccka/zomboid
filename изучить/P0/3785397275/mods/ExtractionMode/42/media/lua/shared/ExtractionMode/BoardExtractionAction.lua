require "TimedActions/ISBaseTimedAction"
require "ExtractionMode/Config"
require "ExtractionMode/Util"

ExtractionMode = ExtractionMode or {}
local Config = ExtractionMode.Config
local Util = ExtractionMode.Util

-- Build 42 resolves a multiplayer timed action by its derived class name on
-- both peers. The class must therefore be global and loaded from shared Lua.
ISBoardExtractionAction = ISBaseTimedAction:derive("ISBoardExtractionAction")

local function clientState(character)
    if ExtractionMode.Client and ExtractionMode.Client.stateFor then
        return ExtractionMode.Client.stateFor(character)
    end
    return ExtractionMode.ClientState or {}
end

function ISBoardExtractionAction:isValid()
    local data = clientState(self.character)
    if isServer and isServer() then
        -- The server performs the authoritative proximity/state validation in
        -- Authority.handleCommand after the replicated action completes.
        return self.character ~= nil and not self.character:isDead()
            and self.character:getVehicle() == nil
    end
    return self.character ~= nil and not self.character:isDead()
        and self.character:getVehicle() == nil
        and data.state == Config.STATE_BOARDING
        and data.isParticipant == true
        and data.boardingPendingSelf ~= true
        and data.extractionRope ~= nil
        and Util.playerNear(self.character, data.extractionRope,
            tonumber(data.extractionRope.radius) or 3)
end

function ISBoardExtractionAction:waitToStart()
    self.character:faceLocation(self.rope.x, self.rope.y)
    return self.character:shouldBeTurning()
end

function ISBoardExtractionAction:update()
    self.character:faceLocation(self.rope.x, self.rope.y)
    self.character:setMetabolicTarget(Metabolics.LightWork)
end

function ISBoardExtractionAction:start()
    self:setActionAnim("Loot")
    self.character:SetVariable("LootPosition", "Mid")
end

function ISBoardExtractionAction:stop()
    ISBaseTimedAction.stop(self)
end

function ISBoardExtractionAction:perform()
    ISBaseTimedAction.perform(self)
end

function ISBoardExtractionAction:complete()
    if isServer and isServer() then
        if ExtractionMode.Server and ExtractionMode.Server.handleCommand then
            ExtractionMode.Server.handleCommand(self.character, "BoardExtraction", {})
        end
    elseif ExtractionMode.Client then
        ExtractionMode.Client.sendCommand(self.character, "BoardExtraction", {})
    end
    return true
end

function ISBoardExtractionAction:getDuration()
    if self.character:isTimedActionInstant() then return 1 end
    return 60
end

function ISBoardExtractionAction:new(character, rope)
    local object = ISBaseTimedAction.new(self, character)
    object.rope = rope
    object.maxTime = object:getDuration()
    return object
end

ExtractionMode.BoardExtractionAction = ISBoardExtractionAction
return ISBoardExtractionAction

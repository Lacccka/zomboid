require "TimedActions/ISBaseTimedAction"
require "ExtractionMode/Config"

ExtractionMode = ExtractionMode or {}
local Config = ExtractionMode.Config

ISUseExtractionModeInfectionCure = ISBaseTimedAction:derive("ISUseExtractionModeInfectionCure")

function ISUseExtractionModeInfectionCure:isValid()
    if isServer and isServer() then
        return self.character ~= nil and not self.character:isDead()
    end
    return self.character ~= nil and not self.character:isDead()
        and self.item ~= nil
        and self.item:getFullType() == Config.INFECTION_CURE_TYPE
        and self.character:getInventory():containsID(self.item:getID())
end

function ISUseExtractionModeInfectionCure:update()
    self.character:setMetabolicTarget(Metabolics.LightDomestic)
end

function ISUseExtractionModeInfectionCure:start()
    self:setActionAnim("Loot")
    self.character:SetVariable("LootPosition", "Mid")
    self:setOverrideHandModels(nil, self.item)
end

function ISUseExtractionModeInfectionCure:stop()
    ISBaseTimedAction.stop(self)
end

function ISUseExtractionModeInfectionCure:perform()
    ISBaseTimedAction.perform(self)
end

function ISUseExtractionModeInfectionCure:complete()
    local args = { itemId = self.item and self.item:getID() or nil }
    if isServer and isServer() then
        if ExtractionMode.Server and ExtractionMode.Server.handleCommand then
            ExtractionMode.Server.handleCommand(self.character, "UseInfectionCure", args)
        end
    elseif ExtractionMode.Client then
        ExtractionMode.Client.sendCommand(self.character, "UseInfectionCure", args)
    end
    return true
end

function ISUseExtractionModeInfectionCure:getDuration()
    if self.character:isTimedActionInstant() then return 1 end
    return 120
end

function ISUseExtractionModeInfectionCure:new(character, item)
    local object = ISBaseTimedAction.new(self, character)
    object.item = item
    object.maxTime = object:getDuration()
    return object
end

ExtractionMode.UseInfectionCureAction = ISUseExtractionModeInfectionCure
return ISUseExtractionModeInfectionCure

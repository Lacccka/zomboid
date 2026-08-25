require "TimedActions/ISBaseTimedAction"

EFZ_ReadSkillBookAction = ISBaseTimedAction:derive("EFZ_ReadSkillBookAction")

local PerkXP = require "EFZ_PerkXP"

local BOOK_CONFIG = {
    ["EFZ.ReloadingBook"] = {
        perk = function() return Perks and Perks.Reloading end,
        jobTextKey = "ContextMenu_Use_ReloadingBook",
    },
    ["EFZ.AimingBook"] = {
        perk = function() return Perks and Perks.Aiming end,
        jobTextKey = "ContextMenu_Use_AimingBook",
    },
    ["EFZ.NimbleBook"] = {
        perk = function() return Perks and Perks.Nimble end,
        jobTextKey = "ContextMenu_Use_NimbleBook",
    },
    ["EFZ.FirstAidBook"] = {
        perk = function() return (Perks and (Perks.Doctor or Perks.FirstAid)) end,
        jobTextKey = "ContextMenu_Use_FirstAidBook",
    },
}

local function getFullTypeSafe(item)
    if not item or not item.getFullType then
        return nil
    end

    return item:getFullType()
end

local function getBookConfig(item)
    local fullType = getFullTypeSafe(item)
    if not fullType then
        return nil
    end
    return BOOK_CONFIG[fullType]
end

local function getPerkLevelSafe(playerObj, perk)
    if not playerObj or not perk or not playerObj.getPerkLevel then
        return nil
    end

    return tonumber(playerObj:getPerkLevel(perk))
end

local function levelUpPerkOnce(playerObj, perk)
    if not playerObj or not perk then
        return false
    end

    return PerkXP.levelUpPerkKeepingProgress(playerObj, perk)
end

local function removeItemFromItsContainer(character, item)
    if not item then
        return false
    end

    if character and character.removeFromHands then
        character:removeFromHands(item)
    end

    local container = item.getContainer and item:getContainer() or nil
    if container and container.Remove then
        container:Remove(item)
        if sendRemoveItemFromContainer then
            sendRemoveItemFromContainer(container, item)
        end
        return true
    end

    local inv = character and character.getInventory and character:getInventory() or nil
    if inv and inv.Remove then
        inv:Remove(item)
        if sendRemoveItemFromContainer then
            sendRemoveItemFromContainer(inv, item)
        end
        return true
    end

    return false
end

function EFZ_ReadSkillBookAction:isValid()
    if not self.character or not self.item then
        return false
    end

    -- Don't allow reading while driving/moving.
    local vehicle = self.character.getVehicle and self.character:getVehicle() or nil
    if vehicle and vehicle.isDriver and vehicle:isDriver(self.character) then
        if vehicle.isEngineRunning and vehicle.getSpeed2D then
            return (not vehicle:isEngineRunning()) or vehicle:getSpeed2D() == 0
        end
    end

    local inv = self.character.getInventory and self.character:getInventory() or nil
    if not (inv and inv.contains and inv:contains(self.item) == true) then
        return false
    end

    local cfg = getBookConfig(self.item)
    if not cfg or not cfg.perk then
        return false
    end

    local perk = cfg.perk()
    if not perk then
        return false
    end

    local before = getPerkLevelSafe(self.character, perk) or 0
    return before < 10
end

function EFZ_ReadSkillBookAction:getDuration()
    -- B42+: duration is calculated on server to prevent cheating.
    return 200
end

function EFZ_ReadSkillBookAction:start()
    if self.item and self.item.setJobType then
        local job = self.jobType
        if not job and getText then
            local cfg = getBookConfig(self.item)
            local key = cfg and cfg.jobTextKey
            if key then
                local text = getText(key)
                if type(text) == "string" and text ~= key then
                    job = text
                end
            end
        end
        self.item:setJobType(job or "Read")
        self.item:setJobDelta(0.0)
    end

    self:setAnimVariable("ReadType", self.readType or "book")
    self:setActionAnim(CharacterActionAnims.Read)
    self:setOverrideHandModels(nil, self.item)

    if self.character and self.character.setReading then
        self.character:setReading(true)
    end
    if self.character and self.character.reportEvent then
        self.character:reportEvent("EventRead")
    end

    if self.openSound and self.openSound ~= "" and self.character and self.character.playSound then
        self.character:playSound(self.openSound)
    end
end

function EFZ_ReadSkillBookAction:update()
    if self.item and self.item.setJobDelta then
        self.item:setJobDelta(self:getJobDelta())
    end
end

function EFZ_ReadSkillBookAction:stop()
    if self.character and self.character.setReading then
        self.character:setReading(false)
    end
    if self.item and self.item.setJobDelta then
        self.item:setJobDelta(0.0)
    end

    if self.closeSound and self.closeSound ~= "" and self.character and self.character.playSound then
        self.character:playSound(self.closeSound)
    end

    ISBaseTimedAction.stop(self)
end

-- Client-only: animations/sounds. No item/stat manipulation here.
function EFZ_ReadSkillBookAction:perform()
    if self.character and self.character.setReading then
        self.character:setReading(false)
    end
    if self.item and self.item.setJobDelta then
        self.item:setJobDelta(0.0)
    end

    if self.closeSound and self.closeSound ~= "" and self.character and self.character.playSound then
        self.character:playSound(self.closeSound)
    end

    ISBaseTimedAction.perform(self)
end

-- Server-only (and singleplayer): apply the perk-up + consume the item.
function EFZ_ReadSkillBookAction:complete()
    local cfg = getBookConfig(self.item)
    if not cfg or not cfg.perk then
        return true
    end

    local perk = cfg.perk()
    if not perk then
        return true
    end

    local before = getPerkLevelSafe(self.character, perk) or 0
    if before >= 10 then
        return true
    end

    local applied = levelUpPerkOnce(self.character, perk) == true
    if not applied then
        return true
    end

    removeItemFromItsContainer(self.character, self.item)
    return true
end

function EFZ_ReadSkillBookAction:new(character, item)
    local o = ISBaseTimedAction.new(self, character)
    o.item = item

    o.readType = "book"
    o.jobType = nil
    o.openSound = "OpenBook"
    o.closeSound = "CloseBook"

    o.stopOnWalk = true
    o.stopOnRun = true
    o.useProgressBar = true

    o.maxTime = o:getDuration()
    return o
end



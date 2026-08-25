require "TimedActions/ISBaseTimedAction"

EFZ_WoundTimeReducerAction = ISBaseTimedAction:derive("EFZ_WoundTimeReducerAction")


local DEFAULT_REDUCTION_FRACTION = 0.5
local DEFAULT_MIN_REDUCTION = 30
local DEFAULT_ACTION_TIME = 15 * 60

local DEFAULT_TIME_FIELDS = {
    { get = "getScratchTime", set = "setScratchTime" },
    { get = "getCutTime", set = "setCutTime" },
    { get = "getBurnTime", set = "setBurnTime" },
    { get = "getDeepWoundTime", set = "setDeepWoundTime" },
    { get = "getStitchTime", set = "setStitchTime" },
    { get = "getFractureTime", set = "setFractureTime" },
    -- Some builds expose bite time as a timer; keep it optional.
    { get = "getBiteTime", set = "setBiteTime" },
}

local function safeCall(obj, fnName, ...)
    if not obj or not fnName then
        return false, nil
    end
    local fn = obj[fnName]
    if not fn then
        return false, nil
    end
    return pcall(fn, obj, ...)
end

local function safeGetNumber(obj, fnName)
    local ok, res = safeCall(obj, fnName)
    if not ok then
        return 0
    end
    local n = tonumber(res) or 0
    if n ~= n then
        return 0
    end
    return n
end

local function computeReducedValue(value, reductionFraction, minReduction)
    if value <= 0 then
        return value
    end
    local frac = tonumber(reductionFraction) or DEFAULT_REDUCTION_FRACTION
    local minR = tonumber(minReduction) or DEFAULT_MIN_REDUCTION
    local reduceBy = math.max(value * frac, minR)
    local newValue = value - reduceBy
    if newValue < 0 then
        newValue = 0
    end
    return newValue
end

local function applyReductionToBodyPart(bodyPart, timeFields, reductionFraction, minReduction)
    if not bodyPart then
        return false
    end
    if type(timeFields) ~= "table" then
        return false
    end

    local changed = false
    for _, field in ipairs(timeFields) do
        local getFn = field and field.get or nil
        local setFn = field and field.set or nil
        if getFn and setFn then
            local cur = safeGetNumber(bodyPart, getFn)
            if cur > 0 then
                local newVal = computeReducedValue(cur, reductionFraction, minReduction)
                if newVal < cur then
                    safeCall(bodyPart, setFn, newVal)
                    changed = true
                end
            end
        end
    end

    return changed
end

local function findBodyPartByTypeString(playerObj, typeStr)
    if not playerObj or not typeStr then
        return nil
    end

    local bd = playerObj.getBodyDamage and playerObj:getBodyDamage() or nil
    if not bd or not bd.getBodyParts then
        return nil
    end

    local list = bd:getBodyParts()
    if not list then
        return nil
    end

    for i = 0, list:size() - 1 do
        local bp = list:get(i)
        local ok, res = safeCall(bp, "getType")
        if ok and res ~= nil and tostring(res) == tostring(typeStr) then
            return bp
        end
    end

    return nil
end

local function hasReducibleTime(bodyPart, timeFields)
    if not bodyPart or type(timeFields) ~= "table" then
        return false
    end
    for _, field in ipairs(timeFields) do
        local getFn = field and field.get or nil
        if getFn and safeGetNumber(bodyPart, getFn) > 0 then
            return true
        end
    end
    return false
end

local function hasBleeding(bodyPart)
    return bodyPart ~= nil and safeGetNumber(bodyPart, "getBleedingTime") > 0
end

local function hasWoundInfection(bodyPart)
    if not bodyPart then
        return false
    end
    do
        local ok, res = safeCall(bodyPart, "IsInfected")
        if ok and res == true then
            return true
        end
    end
    do
        local ok, res = safeCall(bodyPart, "isInfected")
        if ok and res == true then
            return true
        end
    end
    if safeGetNumber(bodyPart, "getWoundInfectionLevel") > 0 then
        return true
    end
    if safeGetNumber(bodyPart, "getInfectionLevel") > 0 then
        return true
    end
    return false
end

local function hasTreatableCondition(bodyPart, timeFields)
    return hasReducibleTime(bodyPart, timeFields) or hasBleeding(bodyPart) or hasWoundInfection(bodyPart)
end

local function cureBleedingAndWoundInfection(bodyPart)
    if not bodyPart then
        return false
    end

    local changed = false

    -- Bleeding
    if hasBleeding(bodyPart) then
        if safeCall(bodyPart, "setBleedingTime", 0) then changed = true end
        if safeCall(bodyPart, "SetBleedingTime", 0) then changed = true end
        if safeCall(bodyPart, "setBleeding", false) then changed = true end
        if safeCall(bodyPart, "SetBleeding", false) then changed = true end
        if safeCall(bodyPart, "setBleedingStemmed", true) then changed = true end
        if safeCall(bodyPart, "SetBleedingStemmed", true) then changed = true end
    end

    -- Bacterial wound infection
    if hasWoundInfection(bodyPart) then
        if safeCall(bodyPart, "SetInfected", false) then changed = true end
        if safeCall(bodyPart, "setInfected", false) then changed = true end
        if safeCall(bodyPart, "setInfectedWound", false) then changed = true end
        if safeCall(bodyPart, "SetFakeInfected", false) then changed = true end
        if safeCall(bodyPart, "DisableFakeInfection") then changed = true end
        if safeCall(bodyPart, "setWoundInfectionLevel", 0) then changed = true end
        if safeCall(bodyPart, "SetWoundInfectionLevel", 0) then changed = true end
        if safeCall(bodyPart, "setInfectionLevel", 0) then changed = true end
        if safeCall(bodyPart, "SetInfectionLevel", 0) then changed = true end
        if safeCall(bodyPart, "setInfectionTime", -1) then changed = true end
        if safeCall(bodyPart, "SetInfectionTime", -1) then changed = true end
    end

    return changed
end

local function fullyHealBodyPart(bodyPart)
    -- Clear leftover wound flags so the health panel does not keep a ghost injury entry.
    bodyPart:RestoreToFullHealth()

    bodyPart:setScratchTime(0)
    bodyPart:setScratched(false, true)
    bodyPart:SetScratchedWeapon(false)
    bodyPart:SetScratchedWindow(false)

    bodyPart:setCutTime(0)
    bodyPart:setCut(false, true)

    bodyPart:setBurnTime(0)
    bodyPart:setNeedBurnWash(false)
    bodyPart:setLastTimeBurnWash(0)

    bodyPart:setDeepWoundTime(0)
    bodyPart:setDeepWounded(false)

    bodyPart:setStitchTime(0)
    bodyPart:setStitched(false)

    bodyPart:setFractureTime(0)
    bodyPart:setSplint(false, 0)
    bodyPart:setSplintFactor(0)

    bodyPart:setBiteTime(0)
    bodyPart:SetBitten(false, false)

    bodyPart:setBleedingTime(0)
    bodyPart:setBleeding(false)
    bodyPart:SetBleedingStemmed(true)

    bodyPart:SetInfected(false)
    bodyPart:setInfectedWound(false)
    bodyPart:setWoundInfectionLevel(0)
    bodyPart:SetFakeInfected(false)
    bodyPart:DisableFakeInfection()

    bodyPart:setHaveGlass(false)
    bodyPart:setHaveBullet(false, 0)

    bodyPart:setBandaged(false, 0)
    bodyPart:setBandageLife(0)
    bodyPart:setAlcoholLevel(0)
    bodyPart:setAdditionalPain(0)
    bodyPart:setStiffness(0)
end

local function refreshCharacterHealth(character)
    if not character or not character.getBodyDamage then
        return
    end

    local bodyDamage = character:getBodyDamage()
    if not bodyDamage then
        return
    end

    if bodyDamage.calculateOverallHealth then
        bodyDamage:calculateOverallHealth()
    end
    if bodyDamage.setBodyPartsLastState then
        bodyDamage:setBodyPartsLastState()
    end
end

local function getInnerContainer(item)
    if not item then
        return nil
    end

    if item.getInventory then
        local ok, res = pcall(function()
            return item:getInventory()
        end)
        if ok and res and res.getItems then
            return res
        end
    end

    if item.getItemContainer then
        local ok, res = pcall(function()
            return item:getItemContainer()
        end)
        if ok and res and res.getItems then
            return res
        end
    end

    return nil
end

local function containsItemRecursive(container, targetItem, visited)
    if not container or not targetItem or not container.getItems then
        return false
    end

    visited = visited or {}
    if visited[container] then
        return false
    end
    visited[container] = true

    local items = container:getItems()
    if not items then
        return false
    end

    for i = 0, items:size() - 1 do
        local it = items:get(i)
        if it == targetItem then
            return true
        end

        local inner = getInnerContainer(it)
        if inner and containsItemRecursive(inner, targetItem, visited) then
            return true
        end
    end

    return false
end

local function equipItemBothHands(playerObj, item)
    if not playerObj or not item then
        return
    end

    -- Best-effort: if the item is in a nested container, try to "grab" it (works well with Inventory Tetris).
    local container = item.getContainer and item:getContainer() or nil
    local inv = playerObj.getInventory and playerObj:getInventory() or nil
    if container and inv and container ~= inv then
        local ok = pcall(require, "ISUI/ISInventoryPaneContextMenu")
        if ok and ISInventoryPaneContextMenu and ISInventoryPaneContextMenu.onGrabItems and playerObj.getPlayerNum then
            pcall(ISInventoryPaneContextMenu.onGrabItems, { item }, playerObj:getPlayerNum())
        end
    end

    if playerObj.setBothHandsItem then
        pcall(playerObj.setBothHandsItem, playerObj, item)
    end
    if playerObj.setPrimaryHandItem then
        pcall(playerObj.setPrimaryHandItem, playerObj, item)
    end
    if playerObj.setSecondaryHandItem then
        pcall(playerObj.setSecondaryHandItem, playerObj, item)
    end
end

local function restoreHands(playerObj, usedItem, prevPrimary, prevSecondary)
    if not playerObj then
        return
    end

    -- Clear used item if still in hands.
    if usedItem and playerObj.getPrimaryHandItem and playerObj.setPrimaryHandItem then
        local ok, cur = pcall(function()
            return playerObj:getPrimaryHandItem()
        end)
        if ok and cur == usedItem then
            pcall(playerObj.setPrimaryHandItem, playerObj, nil)
        end
    end
    if usedItem and playerObj.getSecondaryHandItem and playerObj.setSecondaryHandItem then
        local ok, cur = pcall(function()
            return playerObj:getSecondaryHandItem()
        end)
        if ok and cur == usedItem then
            pcall(playerObj.setSecondaryHandItem, playerObj, nil)
        end
    end

    -- Best-effort restore (only if still owned).
    local inv = playerObj.getInventory and playerObj:getInventory() or nil
    if inv and prevPrimary and containsItemRecursive(inv, prevPrimary, {}) and playerObj.setPrimaryHandItem then
        pcall(playerObj.setPrimaryHandItem, playerObj, prevPrimary)
    end
    if inv and prevSecondary and containsItemRecursive(inv, prevSecondary, {}) and playerObj.setSecondaryHandItem then
        pcall(playerObj.setSecondaryHandItem, playerObj, prevSecondary)
    end
end

local function removeItemFromItsContainer(character, item)
    if not item then
        return false
    end

    if character and character.removeFromHands then
        pcall(function()
            character:removeFromHands(item)
        end)
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

function EFZ_WoundTimeReducerAction:isValid()
    if not self.character or not self.item then
        return false
    end

    local inv = self.character.getInventory and self.character:getInventory() or nil
    if not inv or not containsItemRecursive(inv, self.item, {}) then
        return false
    end

    if not self.partTypeStr then
        return false
    end

    local bodyPart = findBodyPartByTypeString(self.character, self.partTypeStr)
    if not bodyPart then
        return false
    end

    return hasReducibleTime(bodyPart, self.timeFields) or hasBleeding(bodyPart) or hasWoundInfection(bodyPart)
end

function EFZ_WoundTimeReducerAction:getDuration()
    -- B42+: duration is calculated on server to prevent cheating.
    return DEFAULT_ACTION_TIME
end

function EFZ_WoundTimeReducerAction:start()
    -- Client/SP only: hands + animation setup.
    if isServer and isServer() then
        return
    end

    self.prevPrimary = nil
    self.prevSecondary = nil
    if self.character and self.character.getPrimaryHandItem then
        local ok, res = pcall(function()
            return self.character:getPrimaryHandItem()
        end)
        if ok then
            self.prevPrimary = res
        end
    end
    if self.character and self.character.getSecondaryHandItem then
        local ok, res = pcall(function()
            return self.character:getSecondaryHandItem()
        end)
        if ok then
            self.prevSecondary = res
        end
    end

    equipItemBothHands(self.character, self.item)

    if self.item and self.item.setJobType then
        local job = self.jobType or "Use"
        if getText then
            local ok, text = pcall(getText, "ContextMenu_Use_WoundTimeReducer")
            if ok and type(text) == "string" and text ~= "ContextMenu_Use_WoundTimeReducer" then
                job = text
            end
        end
        self.item:setJobType(job)
        self.item:setJobDelta(0.0)
    end

    if CharacterActionAnims and CharacterActionAnims.Bandage then
        self:setActionAnim(CharacterActionAnims.Bandage)
    else
        self:setActionAnim("Bandage")
    end

    local bodyPart = self.partTypeStr and findBodyPartByTypeString(self.character, self.partTypeStr) or nil
    if bodyPart and ISHealthPanel and ISHealthPanel.getBandageType and self.setAnimVariable then
        local ok, bandageType = pcall(ISHealthPanel.getBandageType, bodyPart)
        if ok and bandageType then
            self:setAnimVariable("BandageType", bandageType)
        end
    end

    if self.setOverrideHandModels then
        self:setOverrideHandModels(nil, nil)
    end

    if self.character and self.character.reportEvent then
        self.character:reportEvent("EventBandage")
    end
end

function EFZ_WoundTimeReducerAction:update()
    if isServer and isServer() then
        return
    end

    if self.item and self.item.setJobDelta then
        self.item:setJobDelta(self:getJobDelta())
    end

    local bodyPart = self.partTypeStr and findBodyPartByTypeString(self.character, self.partTypeStr) or nil
    if bodyPart and ISHealthPanel and ISHealthPanel.setBodyPartActionForPlayer then
        local jobType = self.jobType or "Use"
        pcall(ISHealthPanel.setBodyPartActionForPlayer, self.character, bodyPart, self, jobType, { efzWoundTimeReducer = true })
    end

    if self.character and self.character.setMetabolicTarget and Metabolics and Metabolics.LightDomestic then
        self.character:setMetabolicTarget(Metabolics.LightDomestic)
    end
end

function EFZ_WoundTimeReducerAction:stop()
    if isServer and isServer() then
        return
    end

    if self.item and self.item.setJobDelta then
        self.item:setJobDelta(0.0)
    end

    local bodyPart = self.partTypeStr and findBodyPartByTypeString(self.character, self.partTypeStr) or nil
    if bodyPart and ISHealthPanel and ISHealthPanel.setBodyPartActionForPlayer then
        pcall(ISHealthPanel.setBodyPartActionForPlayer, self.character, bodyPart, nil, nil, nil)
    end

    restoreHands(self.character, self.item, self.prevPrimary, self.prevSecondary)
    self.prevPrimary = nil
    self.prevSecondary = nil

    ISBaseTimedAction.stop(self)
end

-- Client-only: no item/stat manipulation here.
function EFZ_WoundTimeReducerAction:perform()
    if isServer and isServer() then
        return
    end

    if self.item and self.item.setJobDelta then
        self.item:setJobDelta(0.0)
    end

    local bodyPart = self.partTypeStr and findBodyPartByTypeString(self.character, self.partTypeStr) or nil
    if bodyPart and ISHealthPanel and ISHealthPanel.setBodyPartActionForPlayer then
        pcall(ISHealthPanel.setBodyPartActionForPlayer, self.character, bodyPart, nil, nil, nil)
    end

    restoreHands(self.character, self.item, self.prevPrimary, self.prevSecondary)
    self.prevPrimary = nil
    self.prevSecondary = nil

    ISBaseTimedAction.perform(self)
end

-- Server-only (and singleplayer): apply effect + consume item.
function EFZ_WoundTimeReducerAction:complete()
    local bodyPart = self.partTypeStr and findBodyPartByTypeString(self.character, self.partTypeStr) or nil
    if not bodyPart then
        return true
    end

    local appliedReduction = applyReductionToBodyPart(bodyPart, self.timeFields, self.reductionFraction, self.minReduction) == true
    local appliedCure = cureBleedingAndWoundInfection(bodyPart) == true
    local appliedFullHeal = false

    if (appliedReduction or appliedCure) and not hasTreatableCondition(bodyPart, self.timeFields) then
        fullyHealBodyPart(bodyPart)
        refreshCharacterHealth(self.character)
        appliedFullHeal = true
    end

    if appliedReduction or appliedCure or appliedFullHeal then
        removeItemFromItsContainer(self.character, self.item)
    end

    return true
end

function EFZ_WoundTimeReducerAction:new(character, item, partTypeStr)
    local o = ISBaseTimedAction.new(self, character)
    o.item = item
    o.partTypeStr = partTypeStr ~= nil and tostring(partTypeStr) or nil

    o.timeFields = DEFAULT_TIME_FIELDS
    o.reductionFraction = DEFAULT_REDUCTION_FRACTION
    o.minReduction = DEFAULT_MIN_REDUCTION
    o.jobType = "Use"

    o.stopOnWalk = true
    o.stopOnRun = true
    o.useProgressBar = true

    o.maxTime = o:getDuration()
    return o
end



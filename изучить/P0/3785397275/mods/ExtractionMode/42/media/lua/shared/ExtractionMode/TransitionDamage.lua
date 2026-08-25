ExtractionMode = ExtractionMode or {}

local TransitionDamage = {}

local function read(callback)
    local ok, value = pcall(callback)
    if ok then return value end
    return nil
end

local function restoreBoolean(getter, setter, desired)
    if desired == nil then return false end
    local current = read(getter)
    if current ~= nil and (current == true) == (desired == true) then return false end
    return pcall(function() setter(desired == true) end)
end

local function restoreNumber(getter, setter, desired)
    desired = tonumber(desired)
    if desired == nil then return false end
    local current = tonumber(read(getter))
    if current ~= nil and math.abs(current - desired) <= 0.0001 then return false end
    return pcall(function() setter(desired) end)
end

local function capturePart(part, index)
    return {
        index = index,
        health = read(function() return part:getHealth() end),
        bitten = read(function() return part:bitten() end),
        biteTime = read(function() return part:getBiteTime() end),
        bleeding = read(function() return part:bleeding() end),
        bleedingTime = read(function() return part:getBleedingTime() end),
        bleedingStemmed = read(function() return part:IsBleedingStemmed() end),
        infected = read(function() return part:IsInfected() end),
        fakeInfected = read(function() return part:IsFakeInfected() end),
        infectedWound = read(function() return part:isInfectedWound() end),
        woundInfectionLevel = read(function() return part:getWoundInfectionLevel() end),
        scratched = read(function() return part:scratched() end),
        scratchTime = read(function() return part:getScratchTime() end),
        cut = read(function() return part:isCut() end),
        cutTime = read(function() return part:getCutTime() end),
        deepWounded = read(function() return part:deepWounded() end),
        deepWoundTime = read(function() return part:getDeepWoundTime() end),
        additionalPain = read(function() return part:getAdditionalPain() end),
    }
end

function TransitionDamage.capture(player)
    if player == nil then return nil end
    local bodyDamage = read(function() return player:getBodyDamage() end)
    if bodyDamage == nil then return nil end
    local snapshot = {
        playerHealth = read(function() return player:getHealth() end),
        overallBodyHealth = read(function() return bodyDamage:getOverallBodyHealth() end),
        infected = read(function() return bodyDamage:isInfected() end),
        infectionTime = read(function() return bodyDamage:getInfectionTime() end),
        infectionMortalityDuration = read(function()
            return bodyDamage:getInfectionMortalityDuration()
        end),
        fakeInfected = read(function() return bodyDamage:isIsFakeInfected() end),
        zombieInfection = read(function()
            return player:getStats():get(CharacterStat.ZOMBIE_INFECTION)
        end),
        zombieFever = read(function()
            return player:getStats():get(CharacterStat.ZOMBIE_FEVER)
        end),
        parts = {},
    }
    local parts = read(function() return bodyDamage:getBodyParts() end)
    if parts ~= nil then
        for index = 0, parts:size() - 1 do
            local part = parts:get(index)
            if part ~= nil then snapshot.parts[#snapshot.parts + 1] = capturePart(part, index) end
        end
    end
    return snapshot
end

local function restorePart(part, snapshot)
    local changed = false
    local function changedIf(value) if value then changed = true end end
    changedIf(restoreBoolean(function() return part:bitten() end,
        function(value)
            local ok = pcall(function() part:SetBitten(value, false) end)
            if not ok then part:SetBitten(value) end
        end, snapshot.bitten))
    changedIf(restoreNumber(function() return part:getBiteTime() end,
        function(value) part:setBiteTime(value) end, snapshot.biteTime))
    changedIf(restoreBoolean(function() return part:bleeding() end,
        function(value) part:setBleeding(value) end, snapshot.bleeding))
    changedIf(restoreNumber(function() return part:getBleedingTime() end,
        function(value) part:setBleedingTime(value) end, snapshot.bleedingTime))
    changedIf(restoreBoolean(function() return part:IsBleedingStemmed() end,
        function(value) part:SetBleedingStemmed(value) end, snapshot.bleedingStemmed))
    changedIf(restoreBoolean(function() return part:IsInfected() end,
        function(value) part:SetInfected(value) end, snapshot.infected))
    changedIf(restoreBoolean(function() return part:IsFakeInfected() end,
        function(value) part:SetFakeInfected(value) end, snapshot.fakeInfected))
    changedIf(restoreBoolean(function() return part:isInfectedWound() end,
        function(value) part:setInfectedWound(value) end, snapshot.infectedWound))
    changedIf(restoreNumber(function() return part:getWoundInfectionLevel() end,
        function(value) part:setWoundInfectionLevel(value) end, snapshot.woundInfectionLevel))
    changedIf(restoreBoolean(function() return part:scratched() end,
        function(value) part:setScratched(value, false) end, snapshot.scratched))
    changedIf(restoreNumber(function() return part:getScratchTime() end,
        function(value) part:setScratchTime(value) end, snapshot.scratchTime))
    changedIf(restoreBoolean(function() return part:isCut() end,
        function(value) part:setCut(value, false) end, snapshot.cut))
    changedIf(restoreNumber(function() return part:getCutTime() end,
        function(value) part:setCutTime(value) end, snapshot.cutTime))
    changedIf(restoreBoolean(function() return part:deepWounded() end,
        function(value) part:setDeepWounded(value) end, snapshot.deepWounded))
    changedIf(restoreNumber(function() return part:getDeepWoundTime() end,
        function(value) part:setDeepWoundTime(value) end, snapshot.deepWoundTime))
    changedIf(restoreNumber(function() return part:getAdditionalPain() end,
        function(value) part:setAdditionalPain(value) end, snapshot.additionalPain))
    changedIf(restoreNumber(function() return part:getHealth() end,
        function(value) part:SetHealth(value) end, snapshot.health))
    return changed
end

function TransitionDamage.restore(player, snapshot)
    if player == nil or type(snapshot) ~= "table" then return false end
    local bodyDamage = read(function() return player:getBodyDamage() end)
    if bodyDamage == nil then return false end
    local changed = false
    local parts = read(function() return bodyDamage:getBodyParts() end)
    if parts ~= nil then
        for _, partSnapshot in ipairs(snapshot.parts or {}) do
            local index = math.floor(tonumber(partSnapshot.index) or -1)
            if index >= 0 and index < parts:size() then
                local part = parts:get(index)
                if part ~= nil and restorePart(part, partSnapshot) then changed = true end
            end
        end
    end
    if restoreBoolean(function() return bodyDamage:isInfected() end,
        function(value) bodyDamage:setInfected(value) end, snapshot.infected) then changed = true end
    if restoreNumber(function() return bodyDamage:getInfectionTime() end,
        function(value) bodyDamage:setInfectionTime(value) end, snapshot.infectionTime) then changed = true end
    if restoreNumber(function() return bodyDamage:getInfectionMortalityDuration() end,
        function(value) bodyDamage:setInfectionMortalityDuration(value) end,
        snapshot.infectionMortalityDuration) then changed = true end
    if restoreBoolean(function() return bodyDamage:isIsFakeInfected() end,
        function(value) bodyDamage:setIsFakeInfected(value) end, snapshot.fakeInfected) then changed = true end
    if restoreNumber(function() return bodyDamage:getOverallBodyHealth() end,
        function(value) bodyDamage:setOverallBodyHealth(value) end,
        snapshot.overallBodyHealth) then changed = true end
    if restoreNumber(function() return player:getHealth() end,
        function(value) player:setHealth(value) end, snapshot.playerHealth) then changed = true end
    local stats = read(function() return player:getStats() end)
    if stats ~= nil and CharacterStat ~= nil then
        if restoreNumber(function() return stats:get(CharacterStat.ZOMBIE_INFECTION) end,
            function(value) stats:set(CharacterStat.ZOMBIE_INFECTION, value) end,
            snapshot.zombieInfection) then changed = true end
        if restoreNumber(function() return stats:get(CharacterStat.ZOMBIE_FEVER) end,
            function(value) stats:set(CharacterStat.ZOMBIE_FEVER, value) end,
            snapshot.zombieFever) then changed = true end
    end
    if changed then
        pcall(function() bodyDamage:setBodyPartsLastState() end)
        -- A zombie bite is processed and synchronized by NetworkPlayerAI. Push
        -- the corrected body state through that same channel so a queued bite
        -- packet cannot leave the owner or observers with the rejected wound.
        pcall(function()
            local networkAI = player:getNetworkCharacterAI()
            if networkAI then networkAI:syncDamage() end
        end)
    end
    return changed
end

function TransitionDamage.reinforce(player, snapshot, keepZombieTargeting)
    if player == nil then return false end
    local changed = TransitionDamage.restore(player, snapshot)
    pcall(function() player:setAvoidDamage(true) end)
    if keepZombieTargeting ~= true then
        -- The two-argument overload bypasses Build 42's admin-capability gate.
        pcall(function() player:setInvisible(true, true) end)
        pcall(function() player:setAttackedBy(nil) end)
        pcall(function() player:setHitReaction("") end)
        pcall(function() player:setDeathDragDown(false) end)
    end
    return changed
end

ExtractionMode.TransitionDamage = TransitionDamage
return TransitionDamage

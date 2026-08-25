ExtractionMode = ExtractionMode or {}

local Integration = {}

function Integration.isAvailable()
    return type(CompanionDogs) == "table"
        and type(CompanionDogs.request) == "function"
end

-- Companion Dogs owns follower persistence and clone prevention. Its rventer
-- command is also the public, retrying path used after a distant teleport: it
-- relocates or respawns only the owner's active dog while that dog is in Follow.
-- Stay, Guard, passive dogs, carried dogs, and dogs stored by vehicle logic are
-- deliberately left to Companion Dogs' normal rules.
function Integration.onPlayerTeleported(player)
    if player == nil or not Integration.isAvailable() then return false end
    local ok = pcall(function()
        CompanionDogs.request("rventer", nil, {}, player)
    end)
    return ok
end

function Integration.isCompanionDogsAnimal(animal)
    if animal == nil then return false end
    local ok, result = pcall(function()
        local modData = animal:getModData()
        return type(modData) == "table" and type(modData.CompanionDogs) == "table"
    end)
    return ok and result == true
end

-- Only the owner's selected Follow dog is handled by rventer. Other Companion
-- Dogs animals near the extraction zone (passive, Stay, or Guard) may safely use
-- the generic tamed-animal transfer because no snapshot recovery races them.
function Integration.usesTeleportRecovery(animal)
    if not Integration.isCompanionDogsAnimal(animal) then return false end
    if type(CompanionDogs) ~= "table" then return true end
    local ok, result = pcall(function()
        if type(CompanionDogs.getState) ~= "function"
            or CompanionDogs.getState(animal) ~= CompanionDogs.STATE_FOLLOW then
            return false
        end
        if type(CompanionDogs.getOwnerPlayer) ~= "function"
            or type(CompanionDogs.playerData) ~= "function"
            or type(CompanionDogs.data) ~= "function" then
            return true
        end
        local owner = CompanionDogs.getOwnerPlayer(animal)
        if owner == nil then return false end
        return CompanionDogs.playerData(owner).token
            == CompanionDogs.data(animal).companionToken
    end)
    return not ok or result == true
end

ExtractionMode.CompanionDogsIntegration = Integration
return Integration

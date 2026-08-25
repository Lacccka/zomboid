local BLOCK_MIN_X = 19968
local BLOCK_MAX_X = 20479
local BLOCK_MIN_Y = 0
local BLOCK_MAX_Y = 767
local ALLOWED_LIVESTOCK_MOD_DATA_KEY = "EFZMapsAllowedLivestock"
local pendingAnimalRemovals = {}
local isRemovalQueueActive = false
local allowedAnimalIds = {}

local function isBlockedCoordinate(x, y)
    return x >= BLOCK_MIN_X and x <= BLOCK_MAX_X and y >= BLOCK_MIN_Y and y <= BLOCK_MAX_Y
end

local function rememberAllowedAnimal(character)
    local animalId = character:getAnimalID()

    if animalId > 0 then
        allowedAnimalIds[animalId] = true
    end
end

local function isPersistentlyAllowedAnimal(character)
    return character:getModData()[ALLOWED_LIVESTOCK_MOD_DATA_KEY] == true
end

local function markAnimalAllowed(character)
    local modData = character:getModData()

    rememberAllowedAnimal(character)

    if modData[ALLOWED_LIVESTOCK_MOD_DATA_KEY] == true then
        return
    end

    modData[ALLOWED_LIVESTOCK_MOD_DATA_KEY] = true
    print(string.format("[EFZ_Maps] Persisted allowed livestock at (%.2f, %.2f), id=%d, type=%s", character:getX(), character:getY(), character:getAnimalID(), tostring(character:getAnimalType())))
    character:transmitModData()
end

local function shouldAllowAnimal(character)
    local data = character:getData()
    local animalId = character:getAnimalID()

    if allowedAnimalIds[animalId] then
        return true
    end

    if isPersistentlyAllowedAnimal(character) then
        rememberAllowedAnimal(character)
        return true
    end

    -- Animals currently attached to a player are always allowed.
    if data:getAttachedPlayer() then
        markAnimalAllowed(character)
        return true
    end

    if not character:isWild() then
        markAnimalAllowed(character)
        return true
    end

    local mother = character:getMother()

    if mother then
        if allowedAnimalIds[mother:getAnimalID()] or isPersistentlyAllowedAnimal(mother) or not mother:isWild() then
            markAnimalAllowed(character)
            return true
        end
    end

    return false
end

local function processPendingAnimalRemovals()
    for i = #pendingAnimalRemovals, 1, -1 do
        local character = pendingAnimalRemovals[i]
        pendingAnimalRemovals[i] = nil
        local x = character:getX()
        local y = character:getY()
        local animalType = tostring(character:getAnimalType())
        local animalId = character:getAnimalID()
        local wild = character:isWild()

        if shouldAllowAnimal(character) then
            print(string.format("[EFZ_Maps] Allowed animal in blocked area at (%.2f, %.2f), id=%d, type=%s, wild=%s", x, y, animalId, animalType, tostring(wild)))
        else
            print(string.format("[EFZ_Maps] Removing blocked wild animal at (%.2f, %.2f), id=%d, type=%s, wild=%s", x, y, animalId, animalType, tostring(wild)))
            character:removeFromWorld()
            character:removeFromSquare()
        end
    end

    if #pendingAnimalRemovals == 0 then
        Events.OnTick.Remove(processPendingAnimalRemovals)
        isRemovalQueueActive = false
    end
end

local function trackAllowedAnimalsInBlockedArea()
    local objectList = getCell():getLuaObjectList()

    for _, object in ipairs(objectList) do

        if instanceof(object, "IsoAnimal") and isBlockedCoordinate(object:getX(), object:getY()) then
            shouldAllowAnimal(object)
        end
    end
end

local function queueAnimalRemoval(character)
    pendingAnimalRemovals[#pendingAnimalRemovals + 1] = character

    if isRemovalQueueActive then
        return
    end

    isRemovalQueueActive = true
    Events.OnTick.Add(processPendingAnimalRemovals)
end

local function onCreateLivingCharacter(character, desc)
    if not instanceof(character, "IsoAnimal") then
        return
    end

    local x = character:getX()
    local y = character:getY()

    if not isBlockedCoordinate(x, y) then
        return
    end

    -- OnCreateLivingCharacter fires before animal visuals are fully initialized in b42.
    print(string.format("[EFZ_Maps] Queued blocked animal spawn at (%.2f, %.2f), type=%s", x, y, tostring(character:getAnimalType())))
    queueAnimalRemoval(character)
end

Events.OnCreateLivingCharacter.Add(onCreateLivingCharacter)
Events.EveryOneMinute.Add(trackAllowedAnimalsInBlockedArea)

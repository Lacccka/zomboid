require "LCCQF/LCCQFConstants"

LCCQF = LCCQF or {}

local C = LCCQF.Constants
local CharacterIdentity = LCCQF.CharacterIdentity or {}

local function log(message)
    print(C.LOG_PREFIX .. "[IDENTITY:SERVER] " .. tostring(message))
end

local function worldHours()
    if not getGameTime then return 0 end
    local gameTime = getGameTime()
    if not gameTime or not gameTime.getWorldAgeHours then return 0 end
    return tonumber(gameTime:getWorldAgeHours()) or 0
end

local function isValidId(value)
    return type(value) == "string" and value ~= "" and #value <= C.MAX_IDENTIFIER_LENGTH
end

local function getRoot()
    if not ModData or not ModData.getOrCreate then return nil end

    local root = ModData.getOrCreate(C.PERSISTENCE_TAG)
    if not root then return nil end

    if tonumber(root.schemaVersion) == nil then
        root.schemaVersion = C.PERSISTENCE_SCHEMA_VERSION
    end
    if type(root.characters) ~= "table" then
        root.characters = {}
    end
    if type(root.retiredCharacterIds) ~= "table" then
        root.retiredCharacterIds = {}
    end

    return root
end

local function newCharacterRecord(characterId)
    local now = worldHours()
    return {
        schemaVersion = C.PERSISTENCE_SCHEMA_VERSION,
        characterId = characterId,
        status = "active",
        createdWorldHours = now,
        updatedWorldHours = now,
        quests = {
            schemaVersion = C.QUEST_PERSISTENCE_SCHEMA_VERSION,
            byInstanceId = {},
            byQuestId = {},
        },
    }
end

local function getRecordById(characterId, create)
    if not isValidId(characterId) then return nil end
    local root = getRoot()
    if not root then return nil end

    local record = root.characters[characterId]
    if type(record) ~= "table" and create then
        record = newCharacterRecord(characterId)
        root.characters[characterId] = record
    end
    return type(record) == "table" and record or nil
end

local function isRetired(characterId)
    local root = getRoot()
    if not root or not isValidId(characterId) then return false end
    if root.retiredCharacterIds[characterId] == true then return true end

    local record = root.characters[characterId]
    return type(record) == "table" and record.status == "retired"
end

function CharacterIdentity.Initialize()
    local root = getRoot()
    if not root then
        return false, "ModData persistence unavailable"
    end

    log("initialized tag=" .. tostring(C.PERSISTENCE_TAG)
        .. " schemaVersion=" .. tostring(root.schemaVersion))
    return true
end

function CharacterIdentity.GetRoot()
    return getRoot()
end

function CharacterIdentity.GetExistingCharacterId(player)
    if not player or not player.getModData then return nil end
    local modData = player:getModData()
    if not modData then return nil end

    local characterId = modData[C.CHARACTER_ID_MODDATA_KEY]
    if not isValidId(characterId) then return nil end
    return characterId
end

function CharacterIdentity.Resolve(player)
    if not player or not player.getModData then return nil, false end

    local modData = player:getModData()
    if not modData then return nil, false end

    local currentId = modData[C.CHARACTER_ID_MODDATA_KEY]
    if isValidId(currentId) and not isRetired(currentId) then
        local record = getRecordById(currentId, true)
        if record then
            record.status = "active"
            record.updatedWorldHours = worldHours()
            return currentId, false
        end
    end

    if player.isDead and player:isDead() then
        return nil, false
    end

    local characterId = tostring(getRandomUUID())
    modData[C.CHARACTER_ID_MODDATA_KEY] = characterId

    local record = getRecordById(characterId, true)
    if not record then return nil, false end

    record.status = "active"
    record.updatedWorldHours = worldHours()

    log("assigned characterId=" .. characterId
        .. " player=" .. tostring(player.getUsername and player:getUsername() or "unknown"))
    return characterId, true
end

function CharacterIdentity.GetRecordById(characterId, create)
    return getRecordById(characterId, create == true)
end

function CharacterIdentity.GetRecord(player, create)
    local characterId
    if create == false then
        characterId = CharacterIdentity.GetExistingCharacterId(player)
    else
        characterId = CharacterIdentity.Resolve(player)
    end
    if not characterId then return nil, nil end
    return getRecordById(characterId, create ~= false), characterId
end

function CharacterIdentity.Touch(characterId)
    local record = getRecordById(characterId, false)
    if not record then return false end
    record.updatedWorldHours = worldHours()
    return true
end

function CharacterIdentity.Retire(player, reason)
    local characterId = CharacterIdentity.GetExistingCharacterId(player)
    if not characterId then return false, nil end

    local root = getRoot()
    local record = getRecordById(characterId, true)
    if not root or not record then return false, characterId end
    if root.retiredCharacterIds[characterId] == true then return false, characterId end

    local now = worldHours()
    root.retiredCharacterIds[characterId] = true
    record.status = "retired"
    record.retiredWorldHours = now
    record.updatedWorldHours = now
    record.retireReason = tostring(reason or "death")

    log("retired characterId=" .. characterId
        .. " reason=" .. tostring(reason or "death"))
    return true, characterId
end

LCCQF.CharacterIdentity = CharacterIdentity

return CharacterIdentity
